package com.billiontech.bookkeeping.repository;

import com.billiontech.bookkeeping.entity.AgentJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AgentJobRepository extends JpaRepository<AgentJob, UUID> {

    Optional<AgentJob> findTopByClient_IdAndStatusAndJobTypeOrderByCompletedAtDesc(
            UUID clientId, String status, String jobType);
}
