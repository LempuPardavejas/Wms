package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Product;
import lt.elektromeistras.domain.ProductStock;
import lt.elektromeistras.domain.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, UUID> {
    Optional<ProductStock> findByProductAndWarehouse(Product product, Warehouse warehouse);
    List<ProductStock> findByProduct(Product product);
    List<ProductStock> findByWarehouse(Warehouse warehouse);
    
    @Query("SELECT ps FROM ProductStock ps WHERE ps.product.id = :productId AND ps.warehouse.id = :warehouseId")
    Optional<ProductStock> findByProductIdAndWarehouseId(@Param("productId") UUID productId, 
                                                          @Param("warehouseId") UUID warehouseId);
    
    @Query("SELECT ps FROM ProductStock ps WHERE (ps.quantity - ps.reservedQuantity) < ps.reorderPoint")
    List<ProductStock> findLowStockItems();
}
