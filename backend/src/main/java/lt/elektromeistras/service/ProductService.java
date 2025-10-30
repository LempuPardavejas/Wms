package lt.elektromeistras.service;

import lt.elektromeistras.domain.Product;
import lt.elektromeistras.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * FAST product search for autocomplete - CRITICAL for UX
     * Limits results to top 20 for performance
     * Searches by code, SKU, name, EAN
     * Prioritizes exact code matches
     */
    public List<Product> searchProducts(String query) {
        log.debug("Searching products with query: {}", query);

        if (query == null || query.trim().isEmpty()) {
            // Return top 20 active products if no query
            return productRepository.findByIsActiveTrue(
                    PageRequest.of(0, 20, Sort.by("name"))
            ).getContent();
        }

        // Use repository search which has optimized ordering
        List<Product> results = productRepository.searchProducts(query.trim());

        // Limit to 20 for autocomplete performance
        return results.size() > 20 ? results.subList(0, 20) : results;
    }

    /**
     * ULTRA FAST exact code lookup - for direct code entry like "0010006"
     * Uses indexed code column for instant lookup
     */
    public Product getByCode(String code) {
        log.debug("Getting product by code: {}", code);
        return productRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Product not found with code: " + code));
    }

    /**
     * Fast code prefix search - for progressive code typing
     */
    public List<Product> searchByCodePrefix(String codePrefix) {
        log.debug("Searching products by code prefix: {}", codePrefix);
        List<Product> results = productRepository.findByCodeStartingWith(codePrefix);
        return results.size() > 20 ? results.subList(0, 20) : results;
    }

    /**
     * Get product by ID
     */
    public Product getById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    /**
     * Get all products with pagination
     */
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable);
    }

    /**
     * Search products with pagination
     */
    public Page<Product> searchProducts(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return productRepository.findByIsActiveTrue(pageable);
        }
        return productRepository.searchProducts(query.trim(), pageable);
    }

    /**
     * Get products by category
     */
    public Page<Product> getProductsByCategory(UUID categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    /**
     * Get cable products
     */
    public Page<Product> getCableProducts(Pageable pageable) {
        return productRepository.findByIsCableTrueAndIsActiveTrue(pageable);
    }

    /**
     * Get modular products
     */
    public Page<Product> getModularProducts(Pageable pageable) {
        return productRepository.findByIsModularTrueAndIsActiveTrue(pageable);
    }

    /**
     * Create new product
     */
    @Transactional
    public Product createProduct(Product product) {
        log.info("Creating new product with code: {}", product.getCode());

        // Validate unique code
        if (productRepository.findByCode(product.getCode()).isPresent()) {
            throw new RuntimeException("Product with code already exists: " + product.getCode());
        }

        // Validate unique SKU
        if (productRepository.findBySku(product.getSku()).isPresent()) {
            throw new RuntimeException("Product with SKU already exists: " + product.getSku());
        }

        return productRepository.save(product);
    }

    /**
     * Update product
     */
    @Transactional
    public Product updateProduct(UUID id, Product productDetails) {
        log.info("Updating product with id: {}", id);

        Product product = getById(id);

        // Update fields
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setCategoryId(productDetails.getCategoryId());
        product.setManufacturerId(productDetails.getManufacturerId());
        product.setUnitOfMeasure(productDetails.getUnitOfMeasure());
        product.setBasePrice(productDetails.getBasePrice());
        product.setCostPrice(productDetails.getCostPrice());
        product.setTaxRate(productDetails.getTaxRate());
        product.setIsCable(productDetails.getIsCable());
        product.setIsModular(productDetails.getIsModular());
        product.setModuleWidth(productDetails.getModuleWidth());
        product.setWeight(productDetails.getWeight());
        product.setImageUrl(productDetails.getImageUrl());
        product.setIsActive(productDetails.getIsActive());
        product.setMinStockLevel(productDetails.getMinStockLevel());

        return productRepository.save(product);
    }

    /**
     * Delete product (soft delete by setting isActive = false)
     */
    @Transactional
    public void deleteProduct(UUID id) {
        log.info("Deleting product with id: {}", id);
        Product product = getById(id);
        product.setIsActive(false);
        productRepository.save(product);
    }

    /**
     * Check if product code exists
     */
    public boolean existsByCode(String code) {
        return productRepository.findByCode(code).isPresent();
    }
}
