package com.andresmarnez.pixelvault.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nfts")
public class NFT {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonIgnore
	@ManyToOne(cascade = {CascadeType.MERGE},fetch= FetchType.EAGER ,optional = false)
	@JoinColumn(name = "smart_contracts_id", nullable = false)
	private SmartContract smartContractsId;

	@Column(name = "nft_id", nullable = false, length = 45)
	private String nftId;

	@Column(name = "metadata")
	private String metadata;

	@Column(name = "metadata_url", unique = true)
	private String metadataUrl;

	@Column(name = "img_url")
	private String imgUrl;

	@Column(name = "value", precision = 5, scale = 2)
	private BigDecimal value;

	@Column(name = "mint_time")
	private LocalDateTime mintTime;

	@OneToMany(mappedBy = "nfts", cascade = CascadeType.MERGE)
	private Set<Trait> traits = new LinkedHashSet<>();
}