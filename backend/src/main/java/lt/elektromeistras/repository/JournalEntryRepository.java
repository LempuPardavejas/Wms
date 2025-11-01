package lt.elektromeistras.repository;

import lt.elektromeistras.domain.BudgetPeriod;
import lt.elektromeistras.domain.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {

    Optional<JournalEntry> findByEntryNumber(String entryNumber);

    List<JournalEntry> findByEntryDate(LocalDate entryDate);

    List<JournalEntry> findByStatus(JournalEntry.EntryStatus status);

    List<JournalEntry> findByEntryType(JournalEntry.EntryType entryType);

    List<JournalEntry> findBySourceType(JournalEntry.SourceType sourceType);

    List<JournalEntry> findByBudgetPeriod(BudgetPeriod budgetPeriod);

    @Query("SELECT je FROM JournalEntry je WHERE je.entryDate BETWEEN :startDate AND :endDate")
    List<JournalEntry> findByEntryDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT je FROM JournalEntry je WHERE je.sourceType = :sourceType AND je.sourceDocumentId = :sourceDocumentId")
    List<JournalEntry> findBySourceDocument(
            @Param("sourceType") JournalEntry.SourceType sourceType,
            @Param("sourceDocumentId") UUID sourceDocumentId
    );

    boolean existsByEntryNumber(String entryNumber);
}
