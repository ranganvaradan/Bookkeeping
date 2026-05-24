package com.billiontech.bookkeeping.repository;

import com.billiontech.bookkeeping.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByClientIdAndQboTxnId(UUID clientId, String qboTxnId);
}
