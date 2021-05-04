package com.tradeledger.cards.repository;

import com.tradeledger.cards.entity.EligibilityResponse;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EligibilityRepository extends CassandraRepository<EligibilityResponse, String> {

}