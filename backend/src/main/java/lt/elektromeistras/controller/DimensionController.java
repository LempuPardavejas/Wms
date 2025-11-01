package lt.elektromeistras.controller;

import lt.elektromeistras.domain.DimensionType;
import lt.elektromeistras.domain.DimensionValue;
import lt.elektromeistras.repository.DimensionTypeRepository;
import lt.elektromeistras.repository.DimensionValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Controller for managing dynamic dimensions
 */
@RestController
@RequestMapping("/api/dimensions")
@RequiredArgsConstructor
@Slf4j
public class DimensionController {

    private final DimensionTypeRepository dimensionTypeRepository;
    private final DimensionValueRepository dimensionValueRepository;

    // ========== DIMENSION TYPES ==========

    /**
     * Get all dimension types
     * GET /api/dimensions/types
     */
    @GetMapping("/types")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<DimensionType>> getAllDimensionTypes() {
        List<DimensionType> types = dimensionTypeRepository.findAll();
        return ResponseEntity.ok(types);
    }

    /**
     * Get active dimension types
     * GET /api/dimensions/types/active
     */
    @GetMapping("/types/active")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<DimensionType>> getActiveDimensionTypes() {
        List<DimensionType> types = dimensionTypeRepository.findByIsActiveTrue();
        return ResponseEntity.ok(types);
    }

    /**
     * Get dimension type by ID
     * GET /api/dimensions/types/{id}
     */
    @GetMapping("/types/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<DimensionType> getDimensionTypeById(@PathVariable UUID id) {
        DimensionType type = dimensionTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dimension type not found with id: " + id));
        return ResponseEntity.ok(type);
    }

    /**
     * Get dimension type by code
     * GET /api/dimensions/types/code/{code}
     */
    @GetMapping("/types/code/{code}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<DimensionType> getDimensionTypeByCode(@PathVariable String code) {
        DimensionType type = dimensionTypeRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Dimension type not found with code: " + code));
        return ResponseEntity.ok(type);
    }

    /**
     * Create new dimension type
     * POST /api/dimensions/types
     */
    @PostMapping("/types")
    @PreAuthorize("hasAnyAuthority('DIMENSION_CREATE', 'ADMIN_FULL')")
    public ResponseEntity<DimensionType> createDimensionType(@Valid @RequestBody DimensionType dimensionType) {
        log.info("Creating new dimension type: {}", dimensionType.getCode());

        if (dimensionTypeRepository.existsByCode(dimensionType.getCode())) {
            throw new RuntimeException("Dimension type with code " + dimensionType.getCode() + " already exists");
        }

        DimensionType created = dimensionTypeRepository.save(dimensionType);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update dimension type
     * PUT /api/dimensions/types/{id}
     */
    @PutMapping("/types/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_EDIT', 'ADMIN_FULL')")
    public ResponseEntity<DimensionType> updateDimensionType(
            @PathVariable UUID id,
            @Valid @RequestBody DimensionType updatedType) {
        log.info("Updating dimension type: {}", id);

        DimensionType type = dimensionTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dimension type not found"));

        type.setName(updatedType.getName());
        type.setDescription(updatedType.getDescription());
        type.setDataType(updatedType.getDataType());
        type.setIsHierarchical(updatedType.getIsHierarchical());
        type.setIsActive(updatedType.getIsActive());
        type.setSortOrder(updatedType.getSortOrder());

        DimensionType saved = dimensionTypeRepository.save(type);
        return ResponseEntity.ok(saved);
    }

    /**
     * Delete dimension type
     * DELETE /api/dimensions/types/{id}
     */
    @DeleteMapping("/types/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_DELETE', 'ADMIN_FULL')")
    public ResponseEntity<Void> deleteDimensionType(@PathVariable UUID id) {
        log.info("Deleting dimension type: {}", id);

        DimensionType type = dimensionTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dimension type not found"));

        // Check if there are any values for this type
        List<DimensionValue> values = dimensionValueRepository.findByDimensionType(type);
        if (!values.isEmpty()) {
            throw new RuntimeException("Cannot delete dimension type with existing values");
        }

        dimensionTypeRepository.delete(type);
        return ResponseEntity.noContent().build();
    }

    // ========== DIMENSION VALUES ==========

    /**
     * Get all dimension values for a type
     * GET /api/dimensions/types/{typeId}/values
     */
    @GetMapping("/types/{typeId}/values")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<DimensionValue>> getValuesByType(@PathVariable UUID typeId) {
        DimensionType type = dimensionTypeRepository.findById(typeId)
                .orElseThrow(() -> new RuntimeException("Dimension type not found"));

        List<DimensionValue> values = dimensionValueRepository.findByDimensionType(type);
        return ResponseEntity.ok(values);
    }

    /**
     * Get active dimension values for a type
     * GET /api/dimensions/types/{typeId}/values/active
     */
    @GetMapping("/types/{typeId}/values/active")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<DimensionValue>> getActiveValuesByType(@PathVariable UUID typeId) {
        DimensionType type = dimensionTypeRepository.findById(typeId)
                .orElseThrow(() -> new RuntimeException("Dimension type not found"));

        List<DimensionValue> values = dimensionValueRepository.findByDimensionTypeAndIsActiveTrue(type);
        return ResponseEntity.ok(values);
    }

    /**
     * Get dimension value by ID
     * GET /api/dimensions/values/{id}
     */
    @GetMapping("/values/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<DimensionValue> getDimensionValueById(@PathVariable UUID id) {
        DimensionValue value = dimensionValueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dimension value not found with id: " + id));
        return ResponseEntity.ok(value);
    }

    /**
     * Get dimension value by type and code
     * GET /api/dimensions/types/{typeId}/values/code/{code}
     */
    @GetMapping("/types/{typeId}/values/code/{code}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<DimensionValue> getValueByTypeAndCode(
            @PathVariable UUID typeId,
            @PathVariable String code) {

        DimensionType type = dimensionTypeRepository.findById(typeId)
                .orElseThrow(() -> new RuntimeException("Dimension type not found"));

        DimensionValue value = dimensionValueRepository.findByDimensionTypeAndCode(type, code)
                .orElseThrow(() -> new RuntimeException("Dimension value not found with code: " + code));

        return ResponseEntity.ok(value);
    }

    /**
     * Get child dimension values
     * GET /api/dimensions/values/{parentId}/children
     */
    @GetMapping("/values/{parentId}/children")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<DimensionValue>> getChildValues(@PathVariable UUID parentId) {
        DimensionValue parent = dimensionValueRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent dimension value not found"));

        List<DimensionValue> children = dimensionValueRepository.findByParentValue(parent);
        return ResponseEntity.ok(children);
    }

    /**
     * Create new dimension value
     * POST /api/dimensions/types/{typeId}/values
     */
    @PostMapping("/types/{typeId}/values")
    @PreAuthorize("hasAnyAuthority('DIMENSION_CREATE', 'ADMIN_FULL')")
    public ResponseEntity<DimensionValue> createDimensionValue(
            @PathVariable UUID typeId,
            @Valid @RequestBody DimensionValue dimensionValue) {

        log.info("Creating new dimension value for type: {}", typeId);

        DimensionType type = dimensionTypeRepository.findById(typeId)
                .orElseThrow(() -> new RuntimeException("Dimension type not found"));

        if (dimensionValueRepository.existsByDimensionTypeAndCode(type, dimensionValue.getCode())) {
            throw new RuntimeException("Dimension value with code " + dimensionValue.getCode() + " already exists for this type");
        }

        dimensionValue.setDimensionType(type);

        // Set parent if specified
        if (dimensionValue.getParentValue() != null && dimensionValue.getParentValue().getId() != null) {
            DimensionValue parent = dimensionValueRepository.findById(dimensionValue.getParentValue().getId())
                    .orElseThrow(() -> new RuntimeException("Parent dimension value not found"));
            dimensionValue.setParentValue(parent);
        }

        DimensionValue created = dimensionValueRepository.save(dimensionValue);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update dimension value
     * PUT /api/dimensions/values/{id}
     */
    @PutMapping("/values/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_EDIT', 'ADMIN_FULL')")
    public ResponseEntity<DimensionValue> updateDimensionValue(
            @PathVariable UUID id,
            @Valid @RequestBody DimensionValue updatedValue) {

        log.info("Updating dimension value: {}", id);

        DimensionValue value = dimensionValueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dimension value not found"));

        value.setName(updatedValue.getName());
        value.setDescription(updatedValue.getDescription());
        value.setTextValue(updatedValue.getTextValue());
        value.setNumericValue(updatedValue.getNumericValue());
        value.setDateValue(updatedValue.getDateValue());
        value.setBooleanValue(updatedValue.getBooleanValue());
        value.setIsActive(updatedValue.getIsActive());
        value.setSortOrder(updatedValue.getSortOrder());

        DimensionValue saved = dimensionValueRepository.save(value);
        return ResponseEntity.ok(saved);
    }

    /**
     * Delete dimension value
     * DELETE /api/dimensions/values/{id}
     */
    @DeleteMapping("/values/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_DELETE', 'ADMIN_FULL')")
    public ResponseEntity<Void> deleteDimensionValue(@PathVariable UUID id) {
        log.info("Deleting dimension value: {}", id);

        DimensionValue value = dimensionValueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dimension value not found"));

        // Check if there are child values
        List<DimensionValue> children = dimensionValueRepository.findByParentValue(value);
        if (!children.isEmpty()) {
            throw new RuntimeException("Cannot delete dimension value with child values");
        }

        dimensionValueRepository.delete(value);
        return ResponseEntity.noContent().build();
    }
}
