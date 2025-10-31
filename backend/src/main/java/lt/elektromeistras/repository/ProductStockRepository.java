package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Product;
import lt.elektromeistras.domain.ProductStock;
import lt.elektromeistras.domain.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ProductStock entity operations.
 */
@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, UUID> {

    /**
     * Find stock by product and warehouse
     * @param product The product
     * @param warehouse The warehouse
     * @return Optional containing stock if found
     */
    Optional<ProductStock> findByProductAndWarehouse(Product product, Warehouse warehouse);

    /**
     * Find stock by product ID and warehouse ID
     * @param productId Product UUID
     * @param warehouseId Warehouse UUID
     * @return Optional containing stock if found
     */
    @Query("SELECT ps FROM ProductStock ps WHERE ps.product.id = :productId AND ps.warehouse.id = :warehouseId")
    Optional<ProductStock> findByProductIdAndWarehouseId(@Param("productId") UUID productId,
                                                           @Param("warehouseId") UUID warehouseId);

    /**
     * Find all stock records for a specific product across all warehouses
     * @param product The product
     * @return List of stock records
     */
    List<ProductStock> findByProduct(Product product);

    /**
     * Find all stock records for a specific warehouse
     * @param warehouse The warehouse
     * @return List of stock records
     */
    List<ProductStock> findByWarehouse(Warehouse warehouse);

    /**
     * Find low stock items (placeholder - requires minimum quantity configuration)
     * @return List of low stock items
     */
    @Query("SELECT ps FROM ProductStock ps WHERE ps.quantity < 10")
    List<ProductStock> findLowStockItems();

    /**
     * Find stock by roll ID (for cable products)
     * @param rollId The roll ID (UUID)
     * @return Optional containing stock if found
     */
    Optional<ProductStock> findByRollId(UUID rollId);

    /**
     * Find stock by roll number (for cable products)
     * @param rollNumber The roll number (human-readable)
     * @return Optional containing stock if found
     */
    Optional<ProductStock> findByRollNumber(String rollNumber);

    /**
     * Find all cable stock with remaining length
     * @return List of cable stock records
     */
    @Query("SELECT ps FROM ProductStock ps WHERE ps.rollId IS NOT NULL AND ps.rollCurrentLength IS NOT NULL AND ps.rollCurrentLength > 0")
    List<ProductStock> findAllCableStock();

    /**
     * Find stock by warehouse and product code
     * @param warehouseId Warehouse UUID
     * @param productCode Product code
     * @return Optional containing stock if found
     */
    @Query("SELECT ps FROM ProductStock ps WHERE ps.warehouse.id = :warehouseId AND ps.product.code = :productCode")
    Optional<ProductStock> findByWarehouseIdAndProductCode(@Param("warehouseId") UUID warehouseId,
                                                             @Param("productCode") String productCode);
}
