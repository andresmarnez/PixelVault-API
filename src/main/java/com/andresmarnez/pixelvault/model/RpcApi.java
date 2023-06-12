package com.andresmarnez.pixelvault.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rpc_api")
public class RpcApi {
	@Id
	@Column(name = "ip", nullable = false, length = 50)
	private String id;

	@Column(name = "password",nullable = false, length = 20)
	private String password;
}