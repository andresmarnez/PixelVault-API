package com.andresmarnez.pixelvault.repository;

import com.andresmarnez.pixelvault.model.SmartContract;
import io.reactivex.Single;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SmartContractRepository extends JpaRepository<SmartContract, String> {

	boolean existsByIdEquals(Long id);

	boolean existsByAddressEquals(String address);

	long countDistinctByIsValidIsTrue();

	SmartContract findByIdEquals(Long id);

	SmartContract findByAddressEquals(String address);

	long deleteByIdEqualsOrNameEquals(Long id, String name);

	@Query(value = """
			SELECT id, eth_address, `description`, is_valid, `name`
			  FROM (
			      SELECT smart_contracts_id, count(*) AS total\s
			      FROM nfts \s
			      GROUP BY smart_contracts_id
			      HAVING total > 5
			   ) AS totales, smart_contracts sc
			        WHERE totales.smart_contracts_id = sc.id
			        ORDER BY totales.total
			        LIMIT 5;""", nativeQuery=true)
	List<SmartContract> getMostPopularSmartContract();
}