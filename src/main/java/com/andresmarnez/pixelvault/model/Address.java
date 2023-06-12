package com.andresmarnez.pixelvault.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "addresses")
public class Address {
	@Id
	@Column(name = "eth_address", nullable = false, length = 42)
	private String address;

	@Column(name="description")
	private String description;

	@JsonIgnore
	@Column(name="location")
	private String location;

	@Column(name = "is_active")
	private Boolean isActive;

	@ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
	@JoinColumn(name = "users_id", nullable = false)
	private User usersId;

}