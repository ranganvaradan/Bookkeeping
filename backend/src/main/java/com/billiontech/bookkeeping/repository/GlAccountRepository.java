package com.billiontech.bookkeeping.repository;

import com.billiontech.bookkeeping.entity.GlAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GlAccountRepository extends JpaRepository<GlAccount, UUID> {

    Optional<GlAccount> findByClientIdAndQboAccountId(UUID clientId, String qboAccountId);
}
