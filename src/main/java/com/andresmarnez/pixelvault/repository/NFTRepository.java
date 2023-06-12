package com.andresmarnez.pixelvault.repository;

import com.andresmarnez.pixelvault.model.NFT;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NFTRepository extends JpaRepository<NFT, Long> {

	List<NFT> findFirst50BySmartContractsId_AddressEquals(String address);



}