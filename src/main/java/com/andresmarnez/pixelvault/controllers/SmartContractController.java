package com.andresmarnez.pixelvault.controllers;

import com.andresmarnez.pixelvault.model.NFT;
import com.andresmarnez.pixelvault.model.SmartContract;
import com.andresmarnez.pixelvault.service.PixelVaultService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:8443", "http://localhost:3000/"})
@RestController
@RequestMapping("/")
public class SmartContractController {

	private final PixelVaultService pixelVaultService;

	public SmartContractController(PixelVaultService pixelVaultService) {
		this.pixelVaultService = pixelVaultService;
	}

	@GetMapping("/smartcontracts/{address}")
	public List<NFT> findNFTsFrom(@PathVariable("address") String address){
		return pixelVaultService.crawlSmartContract(address);
	}

	@GetMapping("/")
	public List<SmartContract> getMostPopularSmarts(){
		return pixelVaultService.getMostPopularSmarts();
	}
}
