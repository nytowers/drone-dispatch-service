package com.thedrone.dispatch.repository;

import com.thedrone.dispatch.entity.Drone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DroneRepository extends JpaRepository<Drone, Long> {

    boolean existsBySerialNumber(String serialNumber);

    @Query("select distinct d from Drone d left join fetch d.medications where d.serialNumber = :serialNumber")
    Optional<Drone> findBySerialNumberWithMedications(@Param("serialNumber") String serialNumber);

    @Query("select distinct d from Drone d left join fetch d.medications order by d.serialNumber")
    List<Drone> findAllWithMedications();
}