package com.andresmarnez.pixelvault.blockchain;

import com.andresmarnez.pixelvault.model.NFT;
import com.andresmarnez.pixelvault.model.SmartContract;
import com.andresmarnez.pixelvault.model.Trait;
import com.andresmarnez.pixelvault.model.TraitId;
import io.ipfs.api.IPFS;

import io.ipfs.multihash.Multihash;
import org.json.JSONArray;
import org.json.JSONObject;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.http.HttpService;

import org.web3j.crypto.*;

import org.web3j.protocol.core.DefaultBlockParameterName;

import org.web3j.protocol.core.methods.response.EthGetBalance;

import org.web3j.utils.Numeric;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;

import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;
import java.util.stream.Collectors;

public class EthManager {

	private final Web3j WEB3J;
	private final IPFS IPFS;

	private final String SECRET_PASSWORD = "SECRET_PASSWORD";
	private final String DATA_FOLDER = System.getProperty("user.dir") + "/data/wallets/";

	private final String ALCHEMY_ENDPOINT = "https://eth-mainnet.g.alchemy.com/v2/i7HN0d8IWvc5xNzlkzB-NgTLVacBDCkT";
	private final String LOCAL_IPFS_CLIENT_IP = "/ip4/127.0.0.1/tcp/5001/";

	// Most RCP Nodes are capped
	private final Long MAX_BLOCK_SIZE = 2000L;

	public EthManager() {
		this.WEB3J = Web3j.build(new HttpService(ALCHEMY_ENDPOINT));
		this.IPFS = new IPFS(LOCAL_IPFS_CLIENT_IP);
	}

	public String createWallet(String username) throws IOException, CipherException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
		File walletDirectory = new File(DATA_FOLDER);
		return DATA_FOLDER + WalletUtils.generateFullNewWalletFile(SECRET_PASSWORD, walletDirectory);
	}

	public Credentials getCredentialsFrom(String url) throws CipherException, IOException {
		return WalletUtils.loadCredentials(SECRET_PASSWORD, url);
	}

	public BigInteger getBalance(String address) throws IOException {
		EthGetBalance ethGetBalance = WEB3J.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
		return ethGetBalance.getBalance();
	}


	public List<NFT> scanSmartContract(SmartContract smartContract, List<NFT> nfts) {

		List<BigInteger> nfts_ids = nfts.stream().map( o -> new BigInteger(o.getNftId())).collect(Collectors.toList());

		try {

			BigInteger latestBlockNumber = WEB3J.ethBlockNumber().send().getBlockNumber();
			BigInteger startBlock = latestBlockNumber.subtract(BigInteger.valueOf(MAX_BLOCK_SIZE));

			EthFilter filter = new EthFilter(
					new DefaultBlockParameterNumber(startBlock),
					new DefaultBlockParameterNumber(latestBlockNumber),
					smartContract.getAddress()
			);

			// Event we will look for in blocks.

			Event nftTransferEvent = new Event(
					"Transfer",
					List.of(new TypeReference<Address>() {
					}, new TypeReference<Address>() {
					}, new TypeReference<Uint256>() {
					})
			);

			filter.addSingleTopic(EventEncoder.encode(nftTransferEvent));

			EthLog ethLog = WEB3J.ethGetLogs(filter).send();

			List<EthLog.LogResult> logs = ethLog.getLogs();


			Map<BigInteger, String> id_urls = new HashMap<>();
			EthLog.LogObject logObject;
			List<String> topics;
			String uri;

			for (EthLog.LogResult logResult : logs) {

				if(id_urls.size() >=50) break;

				logObject = (EthLog.LogObject) logResult.get();
				topics = logObject.getTopics();

				//Saved the Token ID.
				BigInteger tokenId = Numeric.toBigInt(topics.get(3));

				//We want to save each only once.
				if (!id_urls.containsKey(tokenId) && !nfts_ids.contains(tokenId)) {

					uri = getTokenURI(smartContract.getAddress(), tokenId, new DefaultBlockParameterNumber(latestBlockNumber));

					if (uri == null) return null;
					else id_urls.put(tokenId, uri);

				} else {
					System.out.println("Element already saved.");
				}
			}

			nfts.addAll(generateNFTs(id_urls, smartContract));
			return nfts;

		} catch (IOException e) {
			System.out.println("Couldn't connect to Web3j node to retrieve block INFO.");
			return null;
		}
	}

	private String getTokenURI(String contractAddress, BigInteger tokenId, DefaultBlockParameterNumber latest) throws IOException {
		Function function = new Function(
				"tokenURI",
				List.of(new Uint256(tokenId)),
				List.of(new TypeReference<Utf8String>() {

				}));

		String encodedFunction = FunctionEncoder.encode(function);

		Transaction ethCallTransaction = Transaction.createEthCallTransaction(null, contractAddress, encodedFunction);

		EthCall response = WEB3J.ethCall(ethCallTransaction, latest).send();

		String encodedResult = response.getValue();
		
		byte[] decodedBytes = Numeric.hexStringToByteArray(encodedResult);

		String status = new String(decodedBytes);

		if (!status.contains("https://") && !status.contains("ipfs://")) return null;
		return status.trim();
	}

	private List<NFT> generateNFTs(Map<BigInteger, String> ids_uri, SmartContract smartContract) {

		List<NFT> NFTList = new ArrayList<>();

		try {

			for (BigInteger key :
					ids_uri.keySet()) {

				String url = ids_uri.get(key);
				JSONObject obj;

				if(url.contains("https://")){
					url = "https://" + url.split("https://")[1];
					obj = getJsonHttps(url.split("https://")[1]);

				} else if(url.contains("ipfs://")) {
					url =  "ipfs://" + url.split("ipfs://")[1];
					obj = getJsonIPFS(url.split("ipfs://")[1]);
				}
				else {
					return NFTList;
				}

				if (obj == null) continue;
				String jsonImgUrl = obj.getString("image");

				if (jsonImgUrl.contains("ipfs://")) jsonImgUrl = "https://ipfs.io/ipfs/" + jsonImgUrl.split("ipfs://")[1];
				else jsonImgUrl = "https://" + jsonImgUrl.split("https://")[1];

				NFT nft = new NFT();
				nft.setNftId(key.toString());
				nft.setMetadataUrl(url);
				nft.setImgUrl(jsonImgUrl);
				nft.setSmartContractsId(smartContract);

				Set<Trait> traits = nft.getTraits();
				JSONArray attributes = obj.getJSONArray("attributes");

				for (int i = 0; i < attributes.length(); i++) {
					JSONObject object = attributes.getJSONObject(i);
					Trait trait = new Trait();
					trait.setNfts(nft);
					trait.setId(new TraitId(object.getString("trait_type"), null));
					trait.setValue(object.getString("value"));
					traits.add(trait);
				}

				NFTList.add(nft);
			}

		} catch (IOException | IllegalStateException e) {
			System.out.println("ERROR LOADING DATA FROM NFTs");
			System.out.println(e.toString());
		}

		return NFTList;
	}


	private JSONObject getJsonHttps(String url) throws IOException {

		URL u = new URL("https://" + url);
		HttpsURLConnection conn = (HttpsURLConnection) u.openConnection();
		InputStream is = conn.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);

		String output= "";
		String inputLine;

		while ((inputLine = br.readLine()) != null) {
			output+=inputLine;
		}

		br.close();
		isr.close();
		is.close();
		conn.disconnect();

		return new JSONObject(output);
	}

	private JSONObject getJsonIPFS(String CID) throws IOException {
		if (!CID.contains("/")) return null;

		String[] urls = CID.split("/");
		String main = urls[0];
		StringBuilder subpath = new StringBuilder();
		for (int i = 1; i < urls.length; i++) {
			subpath.append("/").append(urls[i]);
		}

		if (main.length() != main.getBytes(StandardCharsets.UTF_8).length){
			System.out.println("Error on String :"+ main);
		}

		Multihash filePointer = Multihash.decode(main);

		byte[] fileContents = new byte[0];

		fileContents = IPFS.cat(filePointer, subpath.toString());

		String output = new String(fileContents);

		return new JSONObject(output);
	}
}