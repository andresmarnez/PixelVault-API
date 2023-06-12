package com.andresmarnez.pixelvault.service;

import com.andresmarnez.pixelvault.blockchain.EthManager;
import com.andresmarnez.pixelvault.model.NFT;
import com.andresmarnez.pixelvault.model.SmartContract;
import com.andresmarnez.pixelvault.repository.NFTRepository;
import com.andresmarnez.pixelvault.repository.SmartContractRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PixelVaultService {

	private final EthManager walletManager = new EthManager();
	private final NFTRepository NFTRepository;
	private final SmartContractRepository smartContractRepository;

	public PixelVaultService(NFTRepository NFTRepository, SmartContractRepository smartContractRepository) {
		this.NFTRepository = NFTRepository;
		this.smartContractRepository = smartContractRepository;
	}

	public List<NFT> crawlSmartContract(String address){

		if (address.matches("^0x[a-fA-F0-9]{40}$")){

			if (!smartContractRepository.existsByAddressEquals(address)) {
				SmartContract contract2 = new SmartContract();
				contract2.setValid(true);
				contract2.setAddress(address);
				smartContractRepository.save(contract2);
			}

			List<NFT> nfts = NFTRepository.findFirst50BySmartContractsId_AddressEquals(address);
			final SmartContract contract = smartContractRepository.findByAddressEquals(address);


			Thread update =	new Thread(){

				@Override
				public void run() {
					super.run();
					nfts.addAll(walletManager.scanSmartContract(contract, nfts));
					NFTRepository.saveAllAndFlush(nfts);
				}
			};

			update.start();

			if (nfts.isEmpty()) {

				try {

					update.join();
					return nfts;

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			return nfts;
		}

		return null;
	}

	public List<SmartContract> getMostPopularSmarts() {
		return smartContractRepository.getMostPopularSmartContract();
	}
}
