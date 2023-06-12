package com.andresmarnez.pixelvault.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TraitId implements Serializable {
	private static final long serialVersionUID = 2403736052249973243L;
	@Column(name = "type", nullable = false, length = 60)
	private String type;
	@Column(name = "nfts_id", nullable = false)
	private Long nftsId;

	@Override
	public int hashCode() {
		return Objects.hash(nftsId, type);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		TraitId entity = (TraitId) o;
		return Objects.equals(this.nftsId, entity.nftsId) &&
				Objects.equals(this.type, entity.type);
	}
}