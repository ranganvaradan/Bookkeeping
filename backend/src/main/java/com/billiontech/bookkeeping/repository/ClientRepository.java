package com.billiontech.bookkeeping.repository;

import com.billiontech.bookkeeping.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    List<Client> findByTenantId(UUID tenantId);

    List<Client> findByQboRealmIdIsNotNull();
}
