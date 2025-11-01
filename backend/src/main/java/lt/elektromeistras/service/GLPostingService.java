package lt.elektromeistras.service;

import lt.elektromeistras.domain.*;
import lt.elektromeistras.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for automatically posting transactions to General Ledger
 * Creates journal entries from business transactions (orders, returns, payments, etc.)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GLPostingService {

    private final JournalEntryService journalEntryService;
    private final GLAccountRepository glAccountRepository;
    private final BudgetPeriodRepository budgetPeriodRepository;
    private final SeriesRepository seriesRepository;

    /**
     * Post order to GL
     * Debit: Accounts Receivable (Customer owes money)
     * Credit: Sales Revenue
     */
    @Transactional
    public JournalEntry postOrderToGL(Order order) {
        log.info("Posting order {} to GL", order.getOrderNumber());

        // Find appropriate GL accounts
        GLAccount receivableAccount = findAccountByCode("1300"); // Accounts Receivable
        GLAccount revenueAccount = findAccountByCode("4000"); // Sales Revenue
        GLAccount vatPayableAccount = findAccountByCode("2410"); // VAT Payable

        // Get current budget period
        BudgetPeriod period = getCurrentBudgetPeriod(order.getOrderDate().toLocalDate());

        // Create journal entry
        JournalEntry entry = JournalEntry.builder()
                .entryNumber(generateEntryNumber("JE-ORDER"))
                .entryDate(order.getOrderDate().toLocalDate())
                .entryType(JournalEntry.EntryType.AUTOMATIC)
                .sourceType(JournalEntry.SourceType.ORDER)
                .sourceDocumentId(order.getId())
                .sourceDocumentNumber(order.getOrderNumber())
                .description("Sales order: " + order.getOrderNumber())
                .budgetPeriod(period)
                .status(JournalEntry.EntryStatus.DRAFT)
                .build();

        entry = journalEntryService.createJournalEntry(entry);

        // Line 1: Debit Accounts Receivable (total including VAT)
        JournalEntryLine receivableLine = JournalEntryLine.builder()
                .glAccount(receivableAccount)
                .debitAmount(order.getTotalAmount())
                .creditAmount(BigDecimal.ZERO)
                .description("Customer: " + order.getCustomer().getName())
                .build();

        entry = journalEntryService.addJournalEntryLine(entry.getId(), receivableLine);

        // Line 2: Credit Sales Revenue (subtotal without VAT)
        JournalEntryLine revenueLine = JournalEntryLine.builder()
                .glAccount(revenueAccount)
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(order.getSubtotalAmount())
                .description("Sales revenue")
                .build();

        entry = journalEntryService.addJournalEntryLine(entry.getId(), revenueLine);

        // Line 3: Credit VAT Payable (if VAT exists)
        if (order.getTaxAmount() != null && order.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            JournalEntryLine vatLine = JournalEntryLine.builder()
                    .glAccount(vatPayableAccount)
                    .debitAmount(BigDecimal.ZERO)
                    .creditAmount(order.getTaxAmount())
                    .description("VAT on sales")
                    .build();

            entry = journalEntryService.addJournalEntryLine(entry.getId(), vatLine);
        }

        // Validate and post
        entry = journalEntryService.validateJournalEntry(entry.getId());
        entry = journalEntryService.postJournalEntry(entry.getId(), null);

        log.info("Order {} posted to GL successfully. Entry: {}", order.getOrderNumber(), entry.getEntryNumber());

        return entry;
    }

    /**
     * Post payment to GL
     * Debit: Cash/Bank
     * Credit: Accounts Receivable
     */
    @Transactional
    public JournalEntry postPaymentToGL(
            UUID customerId,
            BigDecimal amount,
            LocalDate paymentDate,
            String paymentReference,
            String bankAccountCode) {

        log.info("Posting payment to GL: {} from customer {}", amount, customerId);

        // Find GL accounts
        GLAccount cashAccount = findAccountByCode(bankAccountCode != null ? bankAccountCode : "1000"); // Cash/Bank
        GLAccount receivableAccount = findAccountByCode("1300"); // Accounts Receivable

        // Get current budget period
        BudgetPeriod period = getCurrentBudgetPeriod(paymentDate);

        // Create journal entry
        JournalEntry entry = JournalEntry.builder()
                .entryNumber(generateEntryNumber("JE-PMT"))
                .entryDate(paymentDate)
                .entryType(JournalEntry.EntryType.AUTOMATIC)
                .sourceType(JournalEntry.SourceType.PAYMENT)
                .sourceDocumentNumber(paymentReference)
                .description("Payment received: " + paymentReference)
                .budgetPeriod(period)
                .status(JournalEntry.EntryStatus.DRAFT)
                .build();

        entry = journalEntryService.createJournalEntry(entry);

        // Line 1: Debit Cash/Bank
        JournalEntryLine cashLine = JournalEntryLine.builder()
                .glAccount(cashAccount)
                .debitAmount(amount)
                .creditAmount(BigDecimal.ZERO)
                .description("Payment received")
                .build();

        entry = journalEntryService.addJournalEntryLine(entry.getId(), cashLine);

        // Line 2: Credit Accounts Receivable
        JournalEntryLine receivableLine = JournalEntryLine.builder()
                .glAccount(receivableAccount)
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(amount)
                .description("Payment received from customer")
                .build();

        entry = journalEntryService.addJournalEntryLine(entry.getId(), receivableLine);

        // Validate and post
        entry = journalEntryService.validateJournalEntry(entry.getId());
        entry = journalEntryService.postJournalEntry(entry.getId(), null);

        log.info("Payment posted to GL successfully. Entry: {}", entry.getEntryNumber());

        return entry;
    }

    /**
     * Post return to GL
     * Debit: Sales Returns (contra revenue)
     * Debit: VAT Payable (VAT reduction)
     * Credit: Accounts Receivable (reduce customer debt)
     */
    @Transactional
    public JournalEntry postReturnToGL(Return customerReturn) {
        log.info("Posting return {} to GL", customerReturn.getReturnNumber());

        // Find GL accounts
        GLAccount salesReturnsAccount = findAccountByCode("4100"); // Sales Returns (contra revenue)
        GLAccount vatPayableAccount = findAccountByCode("2410"); // VAT Payable
        GLAccount receivableAccount = findAccountByCode("1300"); // Accounts Receivable

        // Get current budget period
        BudgetPeriod period = getCurrentBudgetPeriod(customerReturn.getReturnDate().toLocalDate());

        // Calculate total refund amount
        BigDecimal totalRefund = customerReturn.getReturnLines().stream()
                .map(ReturnLine::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Assuming 21% VAT rate
        BigDecimal vatRate = new BigDecimal("0.21");
        BigDecimal refundWithoutVat = totalRefund.divide(BigDecimal.ONE.add(vatRate), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal vatAmount = totalRefund.subtract(refundWithoutVat);

        // Create journal entry
        JournalEntry entry = JournalEntry.builder()
                .entryNumber(generateEntryNumber("JE-RET"))
                .entryDate(customerReturn.getReturnDate().toLocalDate())
                .entryType(JournalEntry.EntryType.AUTOMATIC)
                .sourceType(JournalEntry.SourceType.RETURN)
                .sourceDocumentId(customerReturn.getId())
                .sourceDocumentNumber(customerReturn.getReturnNumber())
                .description("Customer return: " + customerReturn.getReturnNumber())
                .budgetPeriod(period)
                .status(JournalEntry.EntryStatus.DRAFT)
                .build();

        entry = journalEntryService.createJournalEntry(entry);

        // Line 1: Debit Sales Returns (contra revenue)
        JournalEntryLine returnsLine = JournalEntryLine.builder()
                .glAccount(salesReturnsAccount)
                .debitAmount(refundWithoutVat)
                .creditAmount(BigDecimal.ZERO)
                .description("Sales returns")
                .build();

        entry = journalEntryService.addJournalEntryLine(entry.getId(), returnsLine);

        // Line 2: Debit VAT Payable (reduce VAT liability)
        if (vatAmount.compareTo(BigDecimal.ZERO) > 0) {
            JournalEntryLine vatLine = JournalEntryLine.builder()
                    .glAccount(vatPayableAccount)
                    .debitAmount(vatAmount)
                    .creditAmount(BigDecimal.ZERO)
                    .description("VAT on returns")
                    .build();

            entry = journalEntryService.addJournalEntryLine(entry.getId(), vatLine);
        }

        // Line 3: Credit Accounts Receivable (reduce customer debt)
        JournalEntryLine receivableLine = JournalEntryLine.builder()
                .glAccount(receivableAccount)
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(totalRefund)
                .description("Return from customer")
                .build();

        entry = journalEntryService.addJournalEntryLine(entry.getId(), receivableLine);

        // Validate and post
        entry = journalEntryService.validateJournalEntry(entry.getId());
        entry = journalEntryService.postJournalEntry(entry.getId(), null);

        log.info("Return {} posted to GL successfully. Entry: {}", customerReturn.getReturnNumber(), entry.getEntryNumber());

        return entry;
    }

    /**
     * Post expense to GL
     * Debit: Expense Account
     * Credit: Accounts Payable or Cash
     */
    @Transactional
    public JournalEntry postExpenseToGL(
            String expenseAccountCode,
            BigDecimal amount,
            LocalDate expenseDate,
            String description,
            boolean isPaid,
            Department department,
            CostCenter costCenter) {

        log.info("Posting expense to GL: {}", description);

        // Find GL accounts
        GLAccount expenseAccount = findAccountByCode(expenseAccountCode);
        GLAccount creditAccount = isPaid
                ? findAccountByCode("1000") // Cash if paid
                : findAccountByCode("2100"); // Accounts Payable if unpaid

        // Get current budget period
        BudgetPeriod period = getCurrentBudgetPeriod(expenseDate);

        // Create journal entry
        JournalEntry entry = JournalEntry.builder()
                .entryNumber(generateEntryNumber("JE-EXP"))
                .entryDate(expenseDate)
                .entryType(JournalEntry.EntryType.MANUAL)
                .sourceType(JournalEntry.SourceType.MANUAL)
                .description(description)
                .budgetPeriod(period)
                .status(JournalEntry.EntryStatus.DRAFT)
                .build();

        entry = journalEntryService.createJournalEntry(entry);

        // Line 1: Debit Expense
        JournalEntryLine expenseLine = JournalEntryLine.builder()
                .glAccount(expenseAccount)
                .debitAmount(amount)
                .creditAmount(BigDecimal.ZERO)
                .description(description)
                .department(department)
                .costCenter(costCenter)
                .build();

        entry = journalEntryService.addJournalEntryLine(entry.getId(), expenseLine);

        // Line 2: Credit Cash or Accounts Payable
        JournalEntryLine creditLine = JournalEntryLine.builder()
                .glAccount(creditAccount)
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(amount)
                .description(isPaid ? "Cash payment" : "Accounts payable")
                .build();

        entry = journalEntryService.addJournalEntryLine(entry.getId(), creditLine);

        // Validate and post
        entry = journalEntryService.validateJournalEntry(entry.getId());
        entry = journalEntryService.postJournalEntry(entry.getId(), null);

        log.info("Expense posted to GL successfully. Entry: {}", entry.getEntryNumber());

        return entry;
    }

    /**
     * Helper method to find GL account by code
     */
    private GLAccount findAccountByCode(String code) {
        return glAccountRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("GL Account not found with code: " + code));
    }

    /**
     * Helper method to get current budget period for a date
     */
    private BudgetPeriod getCurrentBudgetPeriod(LocalDate date) {
        List<BudgetPeriod> periods = budgetPeriodRepository.findByDate(date);
        if (periods.isEmpty()) {
            log.warn("No budget period found for date: {}. Creating default period.", date);
            return createDefaultPeriod(date);
        }
        return periods.get(0);
    }

    /**
     * Create default budget period if none exists
     */
    private BudgetPeriod createDefaultPeriod(LocalDate date) {
        int year = date.getYear();
        BudgetPeriod period = BudgetPeriod.builder()
                .code("FY" + year)
                .name("Fiscal Year " + year)
                .periodType(BudgetPeriod.PeriodType.YEAR)
                .fiscalYear(year)
                .startDate(LocalDate.of(year, 1, 1))
                .endDate(LocalDate.of(year, 12, 31))
                .status(BudgetPeriod.PeriodStatus.ACTIVE)
                .isActive(true)
                .build();

        return budgetPeriodRepository.save(period);
    }

    /**
     * Generate entry number with prefix
     */
    private String generateEntryNumber(String prefix) {
        // Try to find series for journal entries
        try {
            List<Series> journalSeries = seriesRepository.findBySeriesType(Series.SeriesType.JOURNAL);
            if (!journalSeries.isEmpty()) {
                Series series = journalSeries.get(0);
                synchronized (series) {
                    Long nextNumber = (series.getCurrentNumber() != null ? series.getCurrentNumber() : 0L) + 1;
                    series.setCurrentNumber(nextNumber);
                    seriesRepository.save(series);
                    return series.getPrefix() + String.format("%06d", nextNumber);
                }
            }
        } catch (Exception e) {
            log.warn("Could not use series for entry number generation: {}", e.getMessage());
        }

        // Fallback to timestamp-based number
        return prefix + "-" + System.currentTimeMillis();
    }

    /**
     * Check if a transaction has already been posted to GL
     */
    public boolean isAlreadyPosted(JournalEntry.SourceType sourceType, UUID sourceDocumentId) {
        List<JournalEntry> entries = journalEntryService.getJournalEntriesBySourceDocument(sourceType, sourceDocumentId);
        return !entries.isEmpty();
    }
}
