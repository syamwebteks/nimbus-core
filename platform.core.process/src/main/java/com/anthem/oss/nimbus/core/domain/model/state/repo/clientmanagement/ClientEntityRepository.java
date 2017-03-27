package com.anthem.oss.nimbus.core.domain.model.state.repo.clientmanagement;

import org.springframework.data.domain.Page;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.anthem.oss.nimbus.core.entity.client.ClientEntity;

public interface ClientEntityRepository extends GraphRepository<ClientEntity>,PagingAndSortingRepository<ClientEntity, Long>{
	
	ClientEntity findByCode(String code);
	
	ClientEntity findByName(String name);
	
	Page<ClientEntity> findAll();
			
}
