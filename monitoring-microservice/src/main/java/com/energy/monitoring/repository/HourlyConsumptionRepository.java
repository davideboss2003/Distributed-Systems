package com.energy.monitoring.repository;

import com.energy.monitoring.entity.HourlyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HourlyConsumptionRepository extends JpaRepository<HourlyConsumption, Long> {
    
    List<HourlyConsumption> findByDeviceIdOrderByTimestampAsc(String deviceId);
    
    List<HourlyConsumption> findByDeviceIdAndTimestampBetweenOrderByTimestampAsc(
        String deviceId, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
}
