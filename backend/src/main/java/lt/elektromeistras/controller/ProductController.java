package lt.elektromeistras.controller;

import lt.elektromeistras.domain.Product;
import lt.elektromeistras.dto.response.ProductSearchResponse;
import lt.elektromeistras.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * FAST product search for autocomplete - CRITICAL endpoint
     * GET /api/products/search?q=0010006
     * Returns top 20 results optimized for autocomplete
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('PRODUCT_VIEW', 'SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<ProductSearchResponse>> searchProducts(@RequestParam String q) {
        log.debug("Product search request: {}", q);

        List<Product> products = productService.searchProducts(q);

        List<ProductSearchResponse> response = products.stream()
                .map(this::mapToSearchResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get product by code - for exact code lookup
     * GET /api/products/code/0010006
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyAuthority('PRODUCT_VIEW', 'SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Product> getByCode(@PathVariable String code) {
        Product product = productService.getByCode(code);
        return ResponseEntity.ok(product);
    }

    /**
     * Get product by ID
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRODUCT_VIEW', 'SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Product> getById(@PathVariable UUID id) {
        Product product = productService.getById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Get all products with pagination
     * GET /api/products?page=0&size=20
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PRODUCT_VIEW', 'SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Page<Product>> getAllProducts(Pageable pageable) {
        Page<Product> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get products by category
     * GET /api/products/category/{categoryId}
     */
    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAnyAuthority('PRODUCT_VIEW', 'SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @PathVariable UUID categoryId,
            Pageable pageable) {
        Page<Product> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get cable products
     * GET /api/products/cables
     */
    @GetMapping("/cables")
    @PreAuthorize("hasAnyAuthority('PRODUCT_VIEW', 'SALES_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Page<Product>> getCableProducts(Pageable pageable) {
        Page<Product> products = productService.getCableProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Create new product
     * POST /api/products
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGE', 'ADMIN_FULL')")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product created = productService.createProduct(product);
        return ResponseEntity.ok(created);
    }

    /**
     * Update product
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGE', 'ADMIN_FULL')")
    public ResponseEntity<Product> updateProduct(
            @PathVariable UUID id,
            @RequestBody Product product) {
        Product updated = productService.updateProduct(id, product);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete product (soft delete)
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGE', 'ADMIN_FULL')")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Map Product to lightweight ProductSearchResponse
     */
    private ProductSearchResponse mapToSearchResponse(Product p) {
        return new ProductSearchResponse(
                p.getId(),
                p.getCode(),
                p.getSku(),
                p.getName(),
                p.getUnitOfMeasure(),
                p.getBasePrice(),
                p.getIsCable(),
                p.getIsModular(),
                p.getImageUrl()
        );
    }
}
