package lt.elektromeistras.controller;

import lt.elektromeistras.domain.*;
import lt.elektromeistras.dto.request.CreateJournalEntryRequest;
import lt.elektromeistras.dto.request.JournalEntryLineRequest;
import lt.elektromeistras.service.JournalEntryService;
import lt.elektromeistras.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/journal-entries")
@RequiredArgsConstructor
@Slf4j
public class JournalEntryController {

    private final JournalEntryService journalEntryService;
    private final GLAccountRepository glAccountRepository;
    private final DepartmentRepository departmentRepository;
    private final CostCenterRepository costCenterRepository;
    private final BusinessObjectRepository businessObjectRepository;
    private final SeriesRepository seriesRepository;
    private final PersonRepository personRepository;
    private final DimensionValueRepository dimensionValueRepository;

    /**
     * Get all journal entries with pagination
     * GET /api/journal-entries?page=0&size=20
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('GL_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<Page<JournalEntry>> getAllJournalEntries(Pageable pageable) {
        Page<JournalEntry> entries = journalEntryService.getAllJournalEntries(pageable);
        return ResponseEntity.ok(entries);
    }

    /**
     * Get journal entry by ID
     * GET /api/journal-entries/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('GL_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<JournalEntry> getById(@PathVariable UUID id) {
        JournalEntry entry = journalEntryService.getById(id);
        return ResponseEntity.ok(entry);
    }

    /**
     * Get journal entry by entry number
     * GET /api/journal-entries/number/{entryNumber}
     */
    @GetMapping("/number/{entryNumber}")
    @PreAuthorize("hasAnyAuthority('GL_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<JournalEntry> getByEntryNumber(@PathVariable String entryNumber) {
        JournalEntry entry = journalEntryService.getByEntryNumber(entryNumber);
        return ResponseEntity.ok(entry);
    }

    /**
     * Get journal entries by status
     * GET /api/journal-entries/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('GL_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<JournalEntry>> getByStatus(@PathVariable String status) {
        JournalEntry.EntryStatus entryStatus = JournalEntry.EntryStatus.valueOf(status);
        List<JournalEntry> entries = journalEntryService.getJournalEntriesByStatus(entryStatus);
        return ResponseEntity.ok(entries);
    }

    /**
     * Get journal entries by date range
     * GET /api/journal-entries/date-range?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyAuthority('GL_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<JournalEntry>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<JournalEntry> entries = journalEntryService.getJournalEntriesByDateRange(startDate, endDate);
        return ResponseEntity.ok(entries);
    }

    /**
     * Get journal entries by source document
     * GET /api/journal-entries/source?type=ORDER&id={uuid}
     */
    @GetMapping("/source")
    @PreAuthorize("hasAnyAuthority('GL_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<JournalEntry>> getBySourceDocument(
            @RequestParam String type,
            @RequestParam UUID id) {
        JournalEntry.SourceType sourceType = JournalEntry.SourceType.valueOf(type);
        List<JournalEntry> entries = journalEntryService.getJournalEntriesBySourceDocument(sourceType, id);
        return ResponseEntity.ok(entries);
    }

    /**
     * Create new journal entry
     * POST /api/journal-entries
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('GL_CREATE', 'ADMIN_FULL')")
    public ResponseEntity<JournalEntry> createJournalEntry(@Valid @RequestBody CreateJournalEntryRequest request) {
        log.info("Creating new journal entry: {}", request.getEntryNumber());

        // Build journal entry from request
        JournalEntry entry = JournalEntry.builder()
                .entryNumber(request.getEntryNumber())
                .entryDate(request.getEntryDate())
                .entryType(JournalEntry.EntryType.valueOf(request.getEntryType()))
                .description(request.getDescription())
                .notes(request.getNotes())
                .build();

        if (request.getSourceType() != null) {
            entry.setSourceType(JournalEntry.SourceType.valueOf(request.getSourceType()));
        }
        entry.setSourceDocumentId(request.getSourceDocumentId());
        entry.setSourceDocumentNumber(request.getSourceDocumentNumber());

        // Create entry without lines first
        JournalEntry createdEntry = journalEntryService.createJournalEntry(entry);

        // Add journal entry lines
        for (JournalEntryLineRequest lineRequest : request.getLines()) {
            JournalEntryLine line = mapToJournalEntryLine(lineRequest);
            journalEntryService.addJournalEntryLine(createdEntry.getId(), line);
        }

        // Fetch updated entry with lines
        JournalEntry finalEntry = journalEntryService.getById(createdEntry.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(finalEntry);
    }

    /**
     * Add journal entry line
     * POST /api/journal-entries/{id}/lines
     */
    @PostMapping("/{id}/lines")
    @PreAuthorize("hasAnyAuthority('GL_CREATE', 'ADMIN_FULL')")
    public ResponseEntity<JournalEntry> addJournalEntryLine(
            @PathVariable UUID id,
            @Valid @RequestBody JournalEntryLineRequest request) {
        log.info("Adding line to journal entry: {}", id);

        JournalEntryLine line = mapToJournalEntryLine(request);
        JournalEntry entry = journalEntryService.addJournalEntryLine(id, line);

        return ResponseEntity.ok(entry);
    }

    /**
     * Remove journal entry line
     * DELETE /api/journal-entries/{id}/lines/{lineId}
     */
    @DeleteMapping("/{id}/lines/{lineId}")
    @PreAuthorize("hasAnyAuthority('GL_CREATE', 'ADMIN_FULL')")
    public ResponseEntity<JournalEntry> removeJournalEntryLine(
            @PathVariable UUID id,
            @PathVariable UUID lineId) {
        log.info("Removing line {} from journal entry: {}", lineId, id);

        JournalEntry entry = journalEntryService.removeJournalEntryLine(id, lineId);
        return ResponseEntity.ok(entry);
    }

    /**
     * Validate journal entry
     * POST /api/journal-entries/{id}/validate
     */
    @PostMapping("/{id}/validate")
    @PreAuthorize("hasAnyAuthority('GL_CREATE', 'ADMIN_FULL')")
    public ResponseEntity<JournalEntry> validateJournalEntry(@PathVariable UUID id) {
        log.info("Validating journal entry: {}", id);
        JournalEntry entry = journalEntryService.validateJournalEntry(id);
        return ResponseEntity.ok(entry);
    }

    /**
     * Post journal entry to GL
     * POST /api/journal-entries/{id}/post
     */
    @PostMapping("/{id}/post")
    @PreAuthorize("hasAnyAuthority('GL_POST', 'ADMIN_FULL')")
    public ResponseEntity<JournalEntry> postJournalEntry(@PathVariable UUID id) {
        log.info("Posting journal entry: {}", id);
        // TODO: Get current user ID from security context
        JournalEntry entry = journalEntryService.postJournalEntry(id, null);
        return ResponseEntity.ok(entry);
    }

    /**
     * Reverse journal entry
     * POST /api/journal-entries/{id}/reverse
     */
    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasAnyAuthority('GL_POST', 'ADMIN_FULL')")
    public ResponseEntity<JournalEntry> reverseJournalEntry(
            @PathVariable UUID id,
            @RequestParam String reason) {
        log.info("Reversing journal entry: {}", id);
        JournalEntry reversalEntry = journalEntryService.reverseJournalEntry(id, reason);
        return ResponseEntity.ok(reversalEntry);
    }

    /**
     * Delete journal entry
     * DELETE /api/journal-entries/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('GL_DELETE', 'ADMIN_FULL')")
    public ResponseEntity<Void> deleteJournalEntry(@PathVariable UUID id) {
        log.info("Deleting journal entry: {}", id);
        journalEntryService.deleteJournalEntry(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get journal entry lines by GL account
     * GET /api/journal-entries/account/{accountId}/lines
     */
    @GetMapping("/account/{accountId}/lines")
    @PreAuthorize("hasAnyAuthority('GL_VIEW', 'ADMIN_FULL')")
    public ResponseEntity<List<JournalEntryLine>> getLinesByAccount(@PathVariable UUID accountId) {
        List<JournalEntryLine> lines = journalEntryService.getJournalEntryLinesByAccount(accountId);
        return ResponseEntity.ok(lines);
    }

    // Helper method to map request to JournalEntryLine entity
    private JournalEntryLine mapToJournalEntryLine(JournalEntryLineRequest request) {
        JournalEntryLine line = JournalEntryLine.builder()
                .debitAmount(request.getDebitAmount())
                .creditAmount(request.getCreditAmount())
                .description(request.getDescription())
                .notes(request.getNotes())
                .build();

        // Set GL account
        GLAccount glAccount = glAccountRepository.findById(request.getGlAccountId())
                .orElseThrow(() -> new RuntimeException("GL Account not found"));
        line.setGlAccount(glAccount);

        // Set static dimensions
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            line.setDepartment(department);
        }

        if (request.getCostCenterId() != null) {
            CostCenter costCenter = costCenterRepository.findById(request.getCostCenterId())
                    .orElseThrow(() -> new RuntimeException("Cost Center not found"));
            line.setCostCenter(costCenter);
        }

        if (request.getBusinessObjectId() != null) {
            BusinessObject businessObject = businessObjectRepository.findById(request.getBusinessObjectId())
                    .orElseThrow(() -> new RuntimeException("Business Object not found"));
            line.setBusinessObject(businessObject);
        }

        if (request.getSeriesId() != null) {
            Series series = seriesRepository.findById(request.getSeriesId())
                    .orElseThrow(() -> new RuntimeException("Series not found"));
            line.setSeries(series);
        }

        if (request.getPersonId() != null) {
            Person person = personRepository.findById(request.getPersonId())
                    .orElseThrow(() -> new RuntimeException("Person not found"));
            line.setPerson(person);
        }

        // Set dynamic dimensions (example for first 5)
        if (request.getDimension1Id() != null) {
            DimensionValue dim1 = dimensionValueRepository.findById(request.getDimension1Id())
                    .orElseThrow(() -> new RuntimeException("Dimension value 1 not found"));
            line.setDimension1(dim1);
        }

        // Add similar logic for other dimensions...

        return line;
    }
}
