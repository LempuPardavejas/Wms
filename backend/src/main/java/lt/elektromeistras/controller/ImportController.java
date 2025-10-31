package lt.elektromeistras.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.elektromeistras.dto.response.ImportResultResponse;
import lt.elektromeistras.service.SupplierInventoryImportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for data import operations
 */
@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
@Slf4j
public class ImportController {

    private final SupplierInventoryImportService importService;

    /**
     * Import supplier inventory from CSV file
     *
     * Example usage:
     * POST /api/import/supplier-inventory?warehouseCode=MAIN&updateExisting=true
     * Content-Type: multipart/form-data
     * Body: file (CSV file)
     *
     * @param file CSV file from FORMAPAK system
     * @param warehouseCode Warehouse code to import stock into (default: "MAIN")
     * @param updateExisting Whether to update existing products (default: false)
     * @return Import result with statistics
     */
    @PostMapping(value = "/supplier-inventory", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGE', 'ADMIN_FULL')")
    public ResponseEntity<ImportResultResponse> importSupplierInventory(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "MAIN") String warehouseCode,
            @RequestParam(defaultValue = "false") boolean updateExisting) {

        log.info("Received import request: file={}, size={}, warehouse={}, updateExisting={}",
            file.getOriginalFilename(), file.getSize(), warehouseCode, updateExisting);

        // Validate file
        if (file.isEmpty()) {
            ImportResultResponse errorResult = ImportResultResponse.builder()
                .status("FAILED")
                .build();
            errorResult.addError("File is empty");
            return ResponseEntity.badRequest().body(errorResult);
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            ImportResultResponse errorResult = ImportResultResponse.builder()
                .status("FAILED")
                .build();
            errorResult.addError("File must be a CSV file");
            return ResponseEntity.badRequest().body(errorResult);
        }

        try {
            ImportResultResponse result = importService.importFromCsv(file, warehouseCode, updateExisting);

            if ("SUCCESS".equals(result.getStatus())) {
                return ResponseEntity.ok(result);
            } else if ("PARTIAL".equals(result.getStatus())) {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(result);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }

        } catch (Exception e) {
            log.error("Failed to import supplier inventory", e);
            ImportResultResponse errorResult = ImportResultResponse.builder()
                .status("FAILED")
                .build();
            errorResult.addError("Import failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }
}
