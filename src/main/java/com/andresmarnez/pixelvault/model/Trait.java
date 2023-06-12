package com.andresmarnez.pixelvault.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "trait")
public class Trait {
	@EmbeddedId
	private TraitId id;

	@JsonIgnore
	@MapsId("nftsId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "nfts_id", nullable = false)
	private NFT nfts;

	@Column(name = "value", nullable = false, length = 45)
	private String value;

	@Column(name = "rarity", length = 45)
	private String rarity;

}