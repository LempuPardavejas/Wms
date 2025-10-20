package lt.elektromeistras.controller;

import lombok.RequiredArgsConstructor;
import lt.elektromeistras.domain.Product;
import lt.elektromeistras.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<Page<Product>> getProducts(Pageable pageable) {
        return ResponseEntity.ok(productRepository.findByIsActiveTrue(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable UUID id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam String q,
            Pageable pageable) {
        return ResponseEntity.ok(productRepository.searchProducts(q, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_MANAGE')")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product saved = productRepository.save(product);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_MANAGE')")
    public ResponseEntity<Product> updateProduct(@PathVariable UUID id, @RequestBody Product product) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        product.setId(id);
        Product saved = productRepository.save(product);
        return ResponseEntity.ok(saved);
    }
}
