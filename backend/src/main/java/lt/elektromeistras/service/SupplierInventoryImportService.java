package lt.elektromeistras.service;

import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.elektromeistras.domain.*;
import lt.elektromeistras.dto.request.SupplierInventoryCsvRow;
import lt.elektromeistras.dto.response.ImportResultResponse;
import lt.elektromeistras.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for importing supplier inventory data from CSV files
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierInventoryImportService {

    private static final int BATCH_SIZE = 500;
    private static final int MAX_ERRORS_TO_REPORT = 100;

    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductCategoryRepository categoryRepository;

    /**
     * Import supplier inventory from CSV file
     *
     * @param file CSV file from FORMAPAK system
     * @param warehouseCode Warehouse code to import stock into
     * @param updateExisting Whether to update existing products or skip them
     * @return Import result with statistics
     */
    @Transactional
    public ImportResultResponse importFromCsv(MultipartFile file, String warehouseCode, boolean updateExisting) {
        log.info("Starting import from file: {}, warehouse: {}, updateExisting: {}",
            file.getOriginalFilename(), warehouseCode, updateExisting);

        ImportResultResponse result = ImportResultResponse.builder()
            .startTime(LocalDateTime.now())
            .totalRows(0)
            .processedRows(0)
            .createdProducts(0)
            .updatedProducts(0)
            .createdStock(0)
            .updatedStock(0)
            .skippedRows(0)
            .errorRows(0)
            .build();

        try {
            // Find warehouse
            Warehouse warehouse = warehouseRepository.findByCode(warehouseCode)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + warehouseCode));

            // Parse CSV file
            List<SupplierInventoryCsvRow> rows = parseCsvFile(file);
            result.setTotalRows(rows.size());
            log.info("Parsed {} rows from CSV", rows.size());

            // Process in batches
            processBatches(rows, warehouse, updateExisting, result);

            result.setEndTime(LocalDateTime.now());
            result.calculateDuration();
            result.determineStatus();

            log.info("Import completed. Status: {}, Processed: {}, Created products: {}, Updated products: {}, Errors: {}",
                result.getStatus(), result.getProcessedRows(), result.getCreatedProducts(),
                result.getUpdatedProducts(), result.getErrorRows());

        } catch (Exception e) {
            log.error("Failed to import CSV", e);
            result.addError("Fatal error: " + e.getMessage());
            result.setStatus("FAILED");
            result.setEndTime(LocalDateTime.now());
            result.calculateDuration();
        }

        return result;
    }

    /**
     * Parse CSV file into DTO objects
     */
    private List<SupplierInventoryCsvRow> parseCsvFile(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            List<SupplierInventoryCsvRow> rows = new CsvToBeanBuilder<SupplierInventoryCsvRow>(reader)
                .withType(SupplierInventoryCsvRow.class)
                .withIgnoreLeadingWhiteSpace(true)
                .withIgnoreEmptyLine(true)
                .build()
                .parse();

            // Clean data
            rows.forEach(SupplierInventoryCsvRow::cleanData);

            return rows;
        }
    }

    /**
     * Process rows in batches for performance
     */
    private void processBatches(List<SupplierInventoryCsvRow> rows, Warehouse warehouse,
                                boolean updateExisting, ImportResultResponse result) {

        // Cache for suppliers and categories to avoid repeated DB lookups
        Map<String, Supplier> supplierCache = new HashMap<>();
        Map<String, ProductCategory> categoryCache = new HashMap<>();

        List<Product> productsToSave = new ArrayList<>();
        List<ProductStock> stockToSave = new ArrayList<>();

        int rowNumber = 0;
        for (SupplierInventoryCsvRow row : rows) {
            rowNumber++;

            try {
                // Validate row
                if (!row.isValid()) {
                    result.setSkippedRows(result.getSkippedRows() + 1);
                    if (result.getErrors().size() < MAX_ERRORS_TO_REPORT) {
                        result.addWarning("Row " + rowNumber + ": Invalid data - missing required fields");
                    }
                    continue;
                }

                // Get or create supplier
                Supplier supplier = getOrCreateSupplier(row, supplierCache);

                // Get or create category
                ProductCategory category = getOrCreateCategory(row, categoryCache);

                // Process product
                Product product = processProduct(row, supplier, category, updateExisting, result);
                if (product != null) {
                    productsToSave.add(product);

                    // Process stock
                    ProductStock stock = processStock(row, product, warehouse, result);
                    if (stock != null) {
                        stockToSave.add(stock);
                    }
                }

                result.setProcessedRows(result.getProcessedRows() + 1);

                // Save in batches
                if (productsToSave.size() >= BATCH_SIZE) {
                    saveBatch(productsToSave, stockToSave);
                    productsToSave.clear();
                    stockToSave.clear();
                    log.info("Processed {} / {} rows", result.getProcessedRows(), result.getTotalRows());
                }

            } catch (Exception e) {
                result.setErrorRows(result.getErrorRows() + 1);
                if (result.getErrors().size() < MAX_ERRORS_TO_REPORT) {
                    result.addError("Row " + rowNumber + " (product: " + row.getProductCode() + "): " + e.getMessage());
                }
                log.error("Error processing row {}: {}", rowNumber, e.getMessage());
            }
        }

        // Save remaining batch
        if (!productsToSave.isEmpty()) {
            saveBatch(productsToSave, stockToSave);
            log.info("Processed final batch. Total: {} rows", result.getProcessedRows());
        }
    }

    /**
     * Get or create supplier from cache or database
     */
    private Supplier getOrCreateSupplier(SupplierInventoryCsvRow row, Map<String, Supplier> cache) {
        String supplierCode = row.getSupplierCode();
        if (supplierCode == null || supplierCode.isEmpty()) {
            supplierCode = "UNKNOWN";
        }

        return cache.computeIfAbsent(supplierCode, code -> {
            Optional<Supplier> existing = supplierRepository.findByCode(code);
            if (existing.isPresent()) {
                return existing.get();
            }

            // Create new supplier
            Supplier supplier = new Supplier();
            supplier.setCode(code);
            supplier.setName(row.getSupplierName() != null ? row.getSupplierName() : code);
            supplier.setIsActive(true);
            return supplierRepository.save(supplier);
        });
    }

    /**
     * Get or create category from cache or database
     */
    private ProductCategory getOrCreateCategory(SupplierInventoryCsvRow row, Map<String, ProductCategory> cache) {
        String categoryCode = row.getCategoryCode();
        if (categoryCode == null || categoryCode.isEmpty()) {
            categoryCode = "UNCATEGORIZED";
        }

        return cache.computeIfAbsent(categoryCode, code -> {
            Optional<ProductCategory> existing = categoryRepository.findByCode(code);
            if (existing.isPresent()) {
                return existing.get();
            }

            // Create new category
            ProductCategory category = new ProductCategory();
            category.setCode(code);
            category.setName(row.getCategoryName() != null ? row.getCategoryName() : code);
            category.setIsActive(true);
            return categoryRepository.save(category);
        });
    }

    /**
     * Process product - create or update
     */
    private Product processProduct(SupplierInventoryCsvRow row, Supplier supplier,
                                   ProductCategory category, boolean updateExisting,
                                   ImportResultResponse result) {

        Optional<Product> existingOpt = productRepository.findByCode(row.getProductCode());

        if (existingOpt.isPresent()) {
            if (updateExisting) {
                Product existing = existingOpt.get();
                updateProductFromRow(existing, row, category);
                result.setUpdatedProducts(result.getUpdatedProducts() + 1);
                return existing;
            } else {
                // Skip existing products
                return existingOpt.get();
            }
        } else {
            // Create new product
            Product product = new Product();
            product.setCode(row.getProductCode());
            updateProductFromRow(product, row, category);
            result.setCreatedProducts(result.getCreatedProducts() + 1);
            return product;
        }
    }

    /**
     * Update product fields from CSV row
     */
    private void updateProductFromRow(Product product, SupplierInventoryCsvRow row, ProductCategory category) {
        product.setName(row.getProductName());
        product.setCategory(category);
        product.setUnitOfMeasure(mapUnitOfMeasure(row.getUnitOfMeasure()));

        if (row.getBarcode() != null && !row.getBarcode().isEmpty()) {
            product.setEan(row.getBarcode());
        }

        if (row.getRetailPrice() != null) {
            product.setBasePrice(row.getRetailPrice());
        }

        if (row.getReceivingPrice() != null) {
            product.setCostPrice(row.getReceivingPrice());
        }

        if (row.getVatPercent() != null) {
            product.setTaxRate(row.getVatPercent());
        } else {
            product.setTaxRate(BigDecimal.valueOf(21)); // Default VAT
        }

        if (row.getSpecification() != null && !row.getSpecification().isEmpty()) {
            product.setDescription(row.getSpecification());
        }

        product.setIsActive(true);
    }

    /**
     * Map unit of measure from CSV to system format
     */
    private String mapUnitOfMeasure(String unit) {
        if (unit == null || unit.isEmpty()) {
            return "PCS";
        }

        unit = unit.trim().toLowerCase();

        switch (unit) {
            case "vnt":
            case "vnt.":
                return "PCS";
            case "m":
            case "m.":
                return "M";
            case "kg":
            case "kg.":
                return "KG";
            case "l":
            case "l.":
                return "L";
            default:
                return unit.toUpperCase();
        }
    }

    /**
     * Process stock entry
     */
    private ProductStock processStock(SupplierInventoryCsvRow row, Product product,
                                     Warehouse warehouse, ImportResultResponse result) {

        if (row.getQuantity() == null || row.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        // Find existing stock
        Optional<ProductStock> existingStock = productStockRepository
            .findByProductAndWarehouse(product, warehouse);

        ProductStock stock;
        if (existingStock.isPresent()) {
            stock = existingStock.get();
            // Update quantity (add to existing)
            stock.setQuantity(stock.getQuantity().add(row.getQuantity()));
            result.setUpdatedStock(result.getUpdatedStock() + 1);
        } else {
            // Create new stock entry
            stock = new ProductStock();
            stock.setProduct(product);
            stock.setWarehouse(warehouse);
            stock.setQuantity(row.getQuantity());
            stock.setReservedQuantity(BigDecimal.ZERO);
            result.setCreatedStock(result.getCreatedStock() + 1);
        }

        stock.setLastCountedDate(row.getParsedReceivingDate());
        stock.setLastCountedQuantity(row.getQuantity());

        return stock;
    }

    /**
     * Save batch of products and stock
     */
    private void saveBatch(List<Product> products, List<ProductStock> stocks) {
        if (!products.isEmpty()) {
            productRepository.saveAll(products);
        }
        if (!stocks.isEmpty()) {
            productStockRepository.saveAll(stocks);
        }
    }
}
