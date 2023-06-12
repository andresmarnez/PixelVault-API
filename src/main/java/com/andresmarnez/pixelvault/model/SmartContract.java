package com.andresmarnez.pixelvault.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "smart_contracts")
public class SmartContract {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "eth_address", nullable = false, length = 42)
	private String address;

	@Column(name = "name", length = 20)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "is_valid", nullable = false)
	private boolean isValid;

	@OneToMany(mappedBy = "smartContractsId", cascade = CascadeType.MERGE , orphanRemoval = true)
	private List<NFT> nfts = new ArrayList<>();

}