package com.militaryasset.module.transfer.repository;

import com.militaryasset.module.transfer.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long>, JpaSpecificationExecutor<Transfer> {
    List<Transfer> findBySourceBaseIdOrDestinationBaseId(Long sourceBaseId, Long destinationBaseId);
}
