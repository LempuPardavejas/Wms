package lt.elektromeistras.service;

import lt.elektromeistras.domain.*;
import lt.elektromeistras.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing journal entries and general ledger postings
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final GLAccountRepository glAccountRepository;
    private final BudgetPeriodRepository budgetPeriodRepository;

    /**
     * Get journal entry by ID
     */
    public JournalEntry getById(UUID id) {
        return journalEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Journal entry not found with id: " + id));
    }

    /**
     * Get journal entry by entry number
     */
    public JournalEntry getByEntryNumber(String entryNumber) {
        return journalEntryRepository.findByEntryNumber(entryNumber)
                .orElseThrow(() -> new RuntimeException("Journal entry not found with number: " + entryNumber));
    }

    /**
     * Get all journal entries with pagination
     */
    public Page<JournalEntry> getAllJournalEntries(Pageable pageable) {
        return journalEntryRepository.findAll(pageable);
    }

    /**
     * Get journal entries by status
     */
    public List<JournalEntry> getJournalEntriesByStatus(JournalEntry.EntryStatus status) {
        return journalEntryRepository.findByStatus(status);
    }

    /**
     * Get journal entries by date range
     */
    public List<JournalEntry> getJournalEntriesByDateRange(LocalDate startDate, LocalDate endDate) {
        return journalEntryRepository.findByEntryDateBetween(startDate, endDate);
    }

    /**
     * Get journal entries by source document
     */
    public List<JournalEntry> getJournalEntriesBySourceDocument(
            JournalEntry.SourceType sourceType, UUID sourceDocumentId) {
        return journalEntryRepository.findBySourceDocument(sourceType, sourceDocumentId);
    }

    /**
     * Create new journal entry
     */
    @Transactional
    public JournalEntry createJournalEntry(JournalEntry journalEntry) {
        log.info("Creating new journal entry: {}", journalEntry.getEntryNumber());

        // Validate budget period if provided
        if (journalEntry.getBudgetPeriod() != null) {
            BudgetPeriod period = budgetPeriodRepository.findById(journalEntry.getBudgetPeriod().getId())
                    .orElseThrow(() -> new RuntimeException("Budget period not found"));
            journalEntry.setBudgetPeriod(period);
        }

        journalEntry.setStatus(JournalEntry.EntryStatus.DRAFT);
        journalEntry.setTotalDebit(BigDecimal.ZERO);
        journalEntry.setTotalCredit(BigDecimal.ZERO);

        return journalEntryRepository.save(journalEntry);
    }

    /**
     * Add journal entry line
     */
    @Transactional
    public JournalEntry addJournalEntryLine(UUID journalEntryId, JournalEntryLine line) {
        log.info("Adding line to journal entry: {}", journalEntryId);

        JournalEntry journalEntry = getById(journalEntryId);

        // Only allow adding lines if entry is in DRAFT status
        if (journalEntry.getStatus() != JournalEntry.EntryStatus.DRAFT) {
            throw new RuntimeException("Cannot add lines to journal entry in " + journalEntry.getStatus() + " status");
        }

        // Validate GL account exists
        GLAccount glAccount = glAccountRepository.findById(line.getGlAccount().getId())
                .orElseThrow(() -> new RuntimeException("GL Account not found"));

        // Validate GL account allows direct posting
        if (!glAccount.getAllowDirectPosting()) {
            throw new RuntimeException("GL Account " + glAccount.getCode() + " does not allow direct posting");
        }

        // Validate required dimensions
        validateRequiredDimensions(glAccount, line);

        line.setGlAccount(glAccount);
        line.setLineNumber(journalEntry.getJournalEntryLines().size() + 1);

        journalEntry.addJournalEntryLine(line);

        return journalEntryRepository.save(journalEntry);
    }

    /**
     * Validate required dimensions for GL account
     */
    private void validateRequiredDimensions(GLAccount glAccount, JournalEntryLine line) {
        if (glAccount.getRequireDepartment() && line.getDepartment() == null) {
            throw new RuntimeException("Department is required for GL Account " + glAccount.getCode());
        }
        if (glAccount.getRequireCostCenter() && line.getCostCenter() == null) {
            throw new RuntimeException("Cost Center is required for GL Account " + glAccount.getCode());
        }
        if (glAccount.getRequireBusinessObject() && line.getBusinessObject() == null) {
            throw new RuntimeException("Business Object is required for GL Account " + glAccount.getCode());
        }
    }

    /**
     * Remove journal entry line
     */
    @Transactional
    public JournalEntry removeJournalEntryLine(UUID journalEntryId, UUID lineId) {
        log.info("Removing line {} from journal entry: {}", lineId, journalEntryId);

        JournalEntry journalEntry = getById(journalEntryId);

        // Only allow removing lines if entry is in DRAFT status
        if (journalEntry.getStatus() != JournalEntry.EntryStatus.DRAFT) {
            throw new RuntimeException("Cannot remove lines from journal entry in " + journalEntry.getStatus() + " status");
        }

        JournalEntryLine lineToRemove = journalEntry.getJournalEntryLines().stream()
                .filter(line -> line.getId().equals(lineId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Journal entry line not found"));

        journalEntry.removeJournalEntryLine(lineToRemove);

        return journalEntryRepository.save(journalEntry);
    }

    /**
     * Validate journal entry (check if balanced)
     */
    @Transactional
    public JournalEntry validateJournalEntry(UUID journalEntryId) {
        log.info("Validating journal entry: {}", journalEntryId);

        JournalEntry journalEntry = getById(journalEntryId);

        if (journalEntry.getStatus() != JournalEntry.EntryStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT journal entries can be validated");
        }

        if (journalEntry.getJournalEntryLines().isEmpty()) {
            throw new RuntimeException("Cannot validate journal entry with no lines");
        }

        if (!journalEntry.isBalanced()) {
            throw new RuntimeException("Journal entry is not balanced. Debit: " +
                    journalEntry.getTotalDebit() + ", Credit: " + journalEntry.getTotalCredit());
        }

        journalEntry.setStatus(JournalEntry.EntryStatus.VALIDATED);

        return journalEntryRepository.save(journalEntry);
    }

    /**
     * Post journal entry to GL
     */
    @Transactional
    public JournalEntry postJournalEntry(UUID journalEntryId, UUID postedByUserId) {
        log.info("Posting journal entry: {}", journalEntryId);

        JournalEntry journalEntry = getById(journalEntryId);

        if (journalEntry.getStatus() != JournalEntry.EntryStatus.VALIDATED) {
            throw new RuntimeException("Only VALIDATED journal entries can be posted");
        }

        if (!journalEntry.isBalanced()) {
            throw new RuntimeException("Cannot post unbalanced journal entry");
        }

        // Post to GL accounts
        for (JournalEntryLine line : journalEntry.getJournalEntryLines()) {
            GLAccount glAccount = line.getGlAccount();
            BigDecimal netAmount = line.getNetAmount();

            // Update GL account balance
            BigDecimal currentBalance = glAccount.getCurrentBalance() != null ?
                    glAccount.getCurrentBalance() : BigDecimal.ZERO;

            // For debit normal balance accounts, increase on debit, decrease on credit
            // For credit normal balance accounts, decrease on debit, increase on credit
            if (glAccount.getNormalBalance() == GLAccount.NormalBalance.DEBIT) {
                glAccount.setCurrentBalance(currentBalance.add(netAmount));
            } else {
                glAccount.setCurrentBalance(currentBalance.subtract(netAmount));
            }

            glAccountRepository.save(glAccount);
        }

        journalEntry.setStatus(JournalEntry.EntryStatus.POSTED);
        journalEntry.setPostingDate(LocalDate.now());
        journalEntry.setPostedAt(java.time.LocalDateTime.now());
        // Set postedBy user if needed

        return journalEntryRepository.save(journalEntry);
    }

    /**
     * Reverse journal entry
     */
    @Transactional
    public JournalEntry reverseJournalEntry(UUID journalEntryId, String reversalReason) {
        log.info("Reversing journal entry: {}", journalEntryId);

        JournalEntry originalEntry = getById(journalEntryId);

        if (originalEntry.getStatus() != JournalEntry.EntryStatus.POSTED) {
            throw new RuntimeException("Only POSTED journal entries can be reversed");
        }

        // Create reversal entry
        JournalEntry reversalEntry = JournalEntry.builder()
                .entryNumber(generateReversalEntryNumber(originalEntry.getEntryNumber()))
                .entryDate(LocalDate.now())
                .entryType(JournalEntry.EntryType.REVERSAL)
                .sourceType(originalEntry.getSourceType())
                .sourceDocumentId(originalEntry.getSourceDocumentId())
                .description("Reversal of " + originalEntry.getEntryNumber() + ": " + reversalReason)
                .budgetPeriod(originalEntry.getBudgetPeriod())
                .status(JournalEntry.EntryStatus.DRAFT)
                .build();

        reversalEntry = journalEntryRepository.save(reversalEntry);

        // Copy lines with reversed amounts
        for (JournalEntryLine originalLine : originalEntry.getJournalEntryLines()) {
            JournalEntryLine reversalLine = JournalEntryLine.builder()
                    .lineNumber(originalLine.getLineNumber())
                    .glAccount(originalLine.getGlAccount())
                    .description("Reversal: " + originalLine.getDescription())
                    .debitAmount(originalLine.getCreditAmount())  // Swap debit and credit
                    .creditAmount(originalLine.getDebitAmount())
                    .department(originalLine.getDepartment())
                    .businessObject(originalLine.getBusinessObject())
                    .costCenter(originalLine.getCostCenter())
                    .series(originalLine.getSeries())
                    .person(originalLine.getPerson())
                    .build();

            reversalEntry.addJournalEntryLine(reversalLine);
        }

        reversalEntry = journalEntryRepository.save(reversalEntry);

        // Validate and post the reversal
        validateJournalEntry(reversalEntry.getId());
        postJournalEntry(reversalEntry.getId(), null);

        // Mark original entry as reversed
        originalEntry.setStatus(JournalEntry.EntryStatus.REVERSED);
        journalEntryRepository.save(originalEntry);

        return reversalEntry;
    }

    /**
     * Generate reversal entry number
     */
    private String generateReversalEntryNumber(String originalNumber) {
        return "REV-" + originalNumber;
    }

    /**
     * Get journal entry lines by GL account
     */
    public List<JournalEntryLine> getJournalEntryLinesByAccount(UUID glAccountId) {
        GLAccount glAccount = glAccountRepository.findById(glAccountId)
                .orElseThrow(() -> new RuntimeException("GL Account not found"));
        return journalEntryLineRepository.findByGlAccount(glAccount);
    }

    /**
     * Delete journal entry (only DRAFT entries)
     */
    @Transactional
    public void deleteJournalEntry(UUID journalEntryId) {
        log.info("Deleting journal entry: {}", journalEntryId);

        JournalEntry journalEntry = getById(journalEntryId);

        if (journalEntry.getStatus() != JournalEntry.EntryStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT journal entries can be deleted");
        }

        journalEntryRepository.delete(journalEntry);
    }
}
