package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Department;
import lt.elektromeistras.domain.GLAccount;
import lt.elektromeistras.domain.JournalEntry;
import lt.elektromeistras.domain.JournalEntryLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, UUID> {

    List<JournalEntryLine> findByJournalEntry(JournalEntry journalEntry);

    List<JournalEntryLine> findByGlAccount(GLAccount glAccount);

    List<JournalEntryLine> findByDepartment(Department department);
}
