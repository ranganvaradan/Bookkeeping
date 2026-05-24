package com.billiontech.bookkeeping.repository;

import com.billiontech.bookkeeping.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
}
