package lt.elektromeistras.controller;

import lt.elektromeistras.domain.*;
import lt.elektromeistras.repository.*;
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
 * Controller for managing static dimensions:
 * Department, CostCenter, BusinessObject, Series, Person
 */
@RestController
@RequestMapping("/api/static-dimensions")
@RequiredArgsConstructor
@Slf4j
public class StaticDimensionController {

    private final DepartmentRepository departmentRepository;
    private final CostCenterRepository costCenterRepository;
    private final BusinessObjectRepository businessObjectRepository;
    private final SeriesRepository seriesRepository;
    private final PersonRepository personRepository;

    // ========== DEPARTMENTS ==========

    @GetMapping("/departments")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }

    @GetMapping("/departments/active")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<Department>> getActiveDepartments() {
        return ResponseEntity.ok(departmentRepository.findByIsActiveTrue());
    }

    @GetMapping("/departments/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Department> getDepartmentById(@PathVariable UUID id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        return ResponseEntity.ok(dept);
    }

    @GetMapping("/departments/code/{code}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Department> getDepartmentByCode(@PathVariable String code) {
        Department dept = departmentRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        return ResponseEntity.ok(dept);
    }

    @GetMapping("/departments/root")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<Department>> getRootDepartments() {
        return ResponseEntity.ok(departmentRepository.findByParentDepartmentIsNull());
    }

    @PostMapping("/departments")
    @PreAuthorize("hasAnyAuthority('DIMENSION_CREATE', 'ADMIN_FULL')")
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody Department department) {
        log.info("Creating department: {}", department.getCode());
        if (departmentRepository.existsByCode(department.getCode())) {
            throw new RuntimeException("Department with code already exists");
        }
        Department created = departmentRepository.save(department);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/departments/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_EDIT', 'ADMIN_FULL')")
    public ResponseEntity<Department> updateDepartment(
            @PathVariable UUID id,
            @Valid @RequestBody Department updated) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        dept.setName(updated.getName());
        dept.setDescription(updated.getDescription());
        dept.setIsActive(updated.getIsActive());
        dept.setSortOrder(updated.getSortOrder());
        return ResponseEntity.ok(departmentRepository.save(dept));
    }

    @DeleteMapping("/departments/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_DELETE', 'ADMIN_FULL')")
    public ResponseEntity<Void> deleteDepartment(@PathVariable UUID id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        departmentRepository.delete(dept);
        return ResponseEntity.noContent().build();
    }

    // ========== COST CENTERS ==========

    @GetMapping("/cost-centers")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<CostCenter>> getAllCostCenters() {
        return ResponseEntity.ok(costCenterRepository.findAll());
    }

    @GetMapping("/cost-centers/active")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<CostCenter>> getActiveCostCenters() {
        return ResponseEntity.ok(costCenterRepository.findByIsActiveTrue());
    }

    @GetMapping("/cost-centers/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<CostCenter> getCostCenterById(@PathVariable UUID id) {
        CostCenter cc = costCenterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cost center not found"));
        return ResponseEntity.ok(cc);
    }

    @GetMapping("/cost-centers/code/{code}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<CostCenter> getCostCenterByCode(@PathVariable String code) {
        CostCenter cc = costCenterRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Cost center not found"));
        return ResponseEntity.ok(cc);
    }

    @GetMapping("/cost-centers/type/{type}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<CostCenter>> getCostCentersByType(@PathVariable String type) {
        CostCenter.CenterType centerType = CostCenter.CenterType.valueOf(type);
        return ResponseEntity.ok(costCenterRepository.findByCenterType(centerType));
    }

    @PostMapping("/cost-centers")
    @PreAuthorize("hasAnyAuthority('DIMENSION_CREATE', 'ADMIN_FULL')")
    public ResponseEntity<CostCenter> createCostCenter(@Valid @RequestBody CostCenter costCenter) {
        log.info("Creating cost center: {}", costCenter.getCode());
        if (costCenterRepository.existsByCode(costCenter.getCode())) {
            throw new RuntimeException("Cost center with code already exists");
        }
        CostCenter created = costCenterRepository.save(costCenter);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/cost-centers/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_EDIT', 'ADMIN_FULL')")
    public ResponseEntity<CostCenter> updateCostCenter(
            @PathVariable UUID id,
            @Valid @RequestBody CostCenter updated) {
        CostCenter cc = costCenterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cost center not found"));
        cc.setName(updated.getName());
        cc.setDescription(updated.getDescription());
        cc.setCenterType(updated.getCenterType());
        cc.setIsActive(updated.getIsActive());
        cc.setSortOrder(updated.getSortOrder());
        return ResponseEntity.ok(costCenterRepository.save(cc));
    }

    @DeleteMapping("/cost-centers/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_DELETE', 'ADMIN_FULL')")
    public ResponseEntity<Void> deleteCostCenter(@PathVariable UUID id) {
        CostCenter cc = costCenterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cost center not found"));
        costCenterRepository.delete(cc);
        return ResponseEntity.noContent().build();
    }

    // ========== BUSINESS OBJECTS ==========

    @GetMapping("/business-objects")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<BusinessObject>> getAllBusinessObjects() {
        return ResponseEntity.ok(businessObjectRepository.findAll());
    }

    @GetMapping("/business-objects/active")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<BusinessObject>> getActiveBusinessObjects() {
        return ResponseEntity.ok(businessObjectRepository.findByIsActiveTrue());
    }

    @GetMapping("/business-objects/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<BusinessObject> getBusinessObjectById(@PathVariable UUID id) {
        BusinessObject bo = businessObjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business object not found"));
        return ResponseEntity.ok(bo);
    }

    @GetMapping("/business-objects/code/{code}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<BusinessObject> getBusinessObjectByCode(@PathVariable String code) {
        BusinessObject bo = businessObjectRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Business object not found"));
        return ResponseEntity.ok(bo);
    }

    @GetMapping("/business-objects/type/{type}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<BusinessObject>> getBusinessObjectsByType(@PathVariable String type) {
        BusinessObject.ObjectType objectType = BusinessObject.ObjectType.valueOf(type);
        return ResponseEntity.ok(businessObjectRepository.findByObjectType(objectType));
    }

    @PostMapping("/business-objects")
    @PreAuthorize("hasAnyAuthority('DIMENSION_CREATE', 'ADMIN_FULL')")
    public ResponseEntity<BusinessObject> createBusinessObject(@Valid @RequestBody BusinessObject businessObject) {
        log.info("Creating business object: {}", businessObject.getCode());
        if (businessObjectRepository.existsByCode(businessObject.getCode())) {
            throw new RuntimeException("Business object with code already exists");
        }
        BusinessObject created = businessObjectRepository.save(businessObject);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/business-objects/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_EDIT', 'ADMIN_FULL')")
    public ResponseEntity<BusinessObject> updateBusinessObject(
            @PathVariable UUID id,
            @Valid @RequestBody BusinessObject updated) {
        BusinessObject bo = businessObjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business object not found"));
        bo.setName(updated.getName());
        bo.setDescription(updated.getDescription());
        bo.setObjectType(updated.getObjectType());
        bo.setIsActive(updated.getIsActive());
        bo.setSortOrder(updated.getSortOrder());
        return ResponseEntity.ok(businessObjectRepository.save(bo));
    }

    @DeleteMapping("/business-objects/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_DELETE', 'ADMIN_FULL')")
    public ResponseEntity<Void> deleteBusinessObject(@PathVariable UUID id) {
        BusinessObject bo = businessObjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business object not found"));
        businessObjectRepository.delete(bo);
        return ResponseEntity.noContent().build();
    }

    // ========== SERIES ==========

    @GetMapping("/series")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<Series>> getAllSeries() {
        return ResponseEntity.ok(seriesRepository.findAll());
    }

    @GetMapping("/series/active")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<Series>> getActiveSeries() {
        return ResponseEntity.ok(seriesRepository.findByIsActiveTrue());
    }

    @GetMapping("/series/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Series> getSeriesById(@PathVariable UUID id) {
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Series not found"));
        return ResponseEntity.ok(series);
    }

    @GetMapping("/series/code/{code}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Series> getSeriesByCode(@PathVariable String code) {
        Series series = seriesRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Series not found"));
        return ResponseEntity.ok(series);
    }

    @GetMapping("/series/type/{type}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<Series>> getSeriesByType(@PathVariable String type) {
        Series.SeriesType seriesType = Series.SeriesType.valueOf(type);
        return ResponseEntity.ok(seriesRepository.findBySeriesType(seriesType));
    }

    @PostMapping("/series")
    @PreAuthorize("hasAnyAuthority('DIMENSION_CREATE', 'ADMIN_FULL')")
    public ResponseEntity<Series> createSeries(@Valid @RequestBody Series series) {
        log.info("Creating series: {}", series.getCode());
        if (seriesRepository.existsByCode(series.getCode())) {
            throw new RuntimeException("Series with code already exists");
        }
        Series created = seriesRepository.save(series);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/series/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_EDIT', 'ADMIN_FULL')")
    public ResponseEntity<Series> updateSeries(
            @PathVariable UUID id,
            @Valid @RequestBody Series updated) {
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Series not found"));
        series.setName(updated.getName());
        series.setDescription(updated.getDescription());
        series.setSeriesType(updated.getSeriesType());
        series.setPrefix(updated.getPrefix());
        series.setIsActive(updated.getIsActive());
        series.setSortOrder(updated.getSortOrder());
        return ResponseEntity.ok(seriesRepository.save(series));
    }

    @DeleteMapping("/series/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_DELETE', 'ADMIN_FULL')")
    public ResponseEntity<Void> deleteSeries(@PathVariable UUID id) {
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Series not found"));
        seriesRepository.delete(series);
        return ResponseEntity.noContent().build();
    }

    // ========== PERSONS ==========

    @GetMapping("/persons")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<Person>> getAllPersons() {
        return ResponseEntity.ok(personRepository.findAll());
    }

    @GetMapping("/persons/active")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<Person>> getActivePersons() {
        return ResponseEntity.ok(personRepository.findByIsActiveTrue());
    }

    @GetMapping("/persons/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Person> getPersonById(@PathVariable UUID id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found"));
        return ResponseEntity.ok(person);
    }

    @GetMapping("/persons/code/{code}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Person> getPersonByCode(@PathVariable String code) {
        Person person = personRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Person not found"));
        return ResponseEntity.ok(person);
    }

    @GetMapping("/persons/type/{type}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<Person>> getPersonsByType(@PathVariable String type) {
        Person.PersonType personType = Person.PersonType.valueOf(type);
        return ResponseEntity.ok(personRepository.findByPersonType(personType));
    }

    @PostMapping("/persons")
    @PreAuthorize("hasAnyAuthority('DIMENSION_CREATE', 'ADMIN_FULL')")
    public ResponseEntity<Person> createPerson(@Valid @RequestBody Person person) {
        log.info("Creating person: {}", person.getCode());
        if (personRepository.existsByCode(person.getCode())) {
            throw new RuntimeException("Person with code already exists");
        }
        Person created = personRepository.save(person);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/persons/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_EDIT', 'ADMIN_FULL')")
    public ResponseEntity<Person> updatePerson(
            @PathVariable UUID id,
            @Valid @RequestBody Person updated) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found"));
        person.setFirstName(updated.getFirstName());
        person.setLastName(updated.getLastName());
        person.setEmail(updated.getEmail());
        person.setPhone(updated.getPhone());
        person.setPersonType(updated.getPersonType());
        person.setPosition(updated.getPosition());
        person.setIsActive(updated.getIsActive());
        person.setSortOrder(updated.getSortOrder());
        return ResponseEntity.ok(personRepository.save(person));
    }

    @DeleteMapping("/persons/{id}")
    @PreAuthorize("hasAnyAuthority('DIMENSION_DELETE', 'ADMIN_FULL')")
    public ResponseEntity<Void> deletePerson(@PathVariable UUID id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found"));
        personRepository.delete(person);
        return ResponseEntity.noContent().build();
    }
}
