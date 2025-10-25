package ro.tuc.ds2025.devices.repository;

import ro.tuc.ds2025.devices.model.Device;
import ro.tuc.ds2025.devices.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {

    List<Device> findByUser(User user);

    boolean existsByUser(User user);
}
