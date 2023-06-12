package com.andresmarnez.pixelvault.service;

import com.andresmarnez.pixelvault.blockchain.EthManager;
import com.andresmarnez.pixelvault.model.Address;
import com.andresmarnez.pixelvault.model.User;
import com.andresmarnez.pixelvault.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.web3j.crypto.CipherException;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Optional;

@Service
public class UserService {

	private final UserRepository repository;
	private final EthManager walletManager = new EthManager();

	public UserService(UserRepository repository) {
		this.repository = repository;
	}

	public Optional<User> getById(Long id) {
		return repository.findById(id);
	}

	public Optional<User> getByUsername(String username){
		return repository.findByUsername(username);
	}

	public Optional<User> getByUsernameAndPassword(String username, String password){
		return repository.findByUsernameAndPassword(username, password);
	}

	public User create(User user){ return repository.save(user); }

	public User update(User user) {
		return repository.save(user);
	}

	public boolean delete(Long id) {
		if (repository.existsById(id)){
			repository.deleteById(id);
			return true;
		}
		return false;
	}

	public int createUser(String email, String username, String password) {

		if (!email.matches(".+[@].+[.].+")) return -1;
		if (repository.existsByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase(username,email)) return -1;
		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(password);
		repository.save(user);

		try {
			String location = walletManager.createWallet(username);
			Address address = new Address();
			address.setAddress(walletManager.getCredentialsFrom(location).getAddress());
			address.setDescription("Pixel Vault Eth Address");
			address.setUsersId(user);
			address.setLocation(location);
			address.setIsActive(true);

			user.getAddresses().add(address);
			repository.save(user);
		} catch (IOException | CipherException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
			e.printStackTrace();
		}

		return 1;
	}
}
