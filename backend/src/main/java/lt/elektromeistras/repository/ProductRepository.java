package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Find product by code - FAST lookup with index
     */
    Optional<Product> findByCode(String code);

    /**
     * Find product by SKU
     */
    Optional<Product> findBySku(String sku);

    /**
     * Find product by EAN
     */
    Optional<Product> findByEan(String ean);

    /**
     * Find all active products
     */
    Page<Product> findByIsActiveTrue(Pageable pageable);

    /**
     * FAST product search by code or name - CRITICAL for autocomplete
     * Searches in code, SKU, name - case insensitive
     * Uses indexed columns for performance
     */
    @Query("SELECT p FROM Product p WHERE " +
            "p.isActive = true AND (" +
            "LOWER(p.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.ean) LIKE LOWER(CONCAT('%', :query, '%'))" +
            ") ORDER BY " +
            "CASE WHEN LOWER(p.code) = LOWER(:query) THEN 1 " +
            "WHEN LOWER(p.code) LIKE LOWER(CONCAT(:query, '%')) THEN 2 " +
            "WHEN LOWER(p.sku) = LOWER(:query) THEN 3 " +
            "WHEN LOWER(p.ean) = LOWER(:query) THEN 4 " +
            "ELSE 5 END, p.name")
    List<Product> searchProducts(@Param("query") String query);

    /**
     * FAST product search with pagination
     */
    @Query("SELECT p FROM Product p WHERE " +
            "p.isActive = true AND (" +
            "LOWER(p.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.ean) LIKE LOWER(CONCAT('%', :query, '%'))" +
            ")")
    Page<Product> searchProducts(@Param("query") String query, Pageable pageable);

    /**
     * Search products starting with code - OPTIMIZED for direct code entry like "0010006"
     * This is the FASTEST search for exact code matches
     */
    @Query("SELECT p FROM Product p WHERE " +
            "p.isActive = true AND " +
            "LOWER(p.code) LIKE LOWER(CONCAT(:codePrefix, '%')) " +
            "ORDER BY p.code")
    List<Product> findByCodeStartingWith(@Param("codePrefix") String codePrefix);

    /**
     * Find products by category
     */
    @Query("SELECT p FROM Product p WHERE p.categoryId = :categoryId AND p.isActive = true")
    Page<Product> findByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);

    /**
     * Find cable products
     */
    Page<Product> findByIsCableTrueAndIsActiveTrue(Pageable pageable);

    /**
     * Find modular products
     */
    Page<Product> findByIsModularTrueAndIsActiveTrue(Pageable pageable);

    /**
     * Find low stock products
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.minStockLevel IS NOT NULL")
    List<Product> findLowStockProducts();
}
