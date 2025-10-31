package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Warehouse;
import lt.elektromeistras.domain.WarehouseLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseLocationRepository extends JpaRepository<WarehouseLocation, UUID> {
    List<WarehouseLocation> findByWarehouse(Warehouse warehouse);

    @Query("SELECT wl FROM WarehouseLocation wl WHERE wl.warehouse.id = :warehouseId AND wl.code = :code")
    Optional<WarehouseLocation> findByWarehouseIdAndCode(@Param("warehouseId") UUID warehouseId,
                                                          @Param("code") String code);

    @Query("SELECT wl FROM WarehouseLocation wl WHERE wl.warehouse.id = :warehouseId AND wl.isActive = true")
    List<WarehouseLocation> findActiveLocationsByWarehouse(@Param("warehouseId") UUID warehouseId);
}
