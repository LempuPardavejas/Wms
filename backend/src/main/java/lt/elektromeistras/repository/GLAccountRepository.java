package lt.elektromeistras.repository;

import lt.elektromeistras.domain.GLAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GLAccountRepository extends JpaRepository<GLAccount, UUID> {

    Optional<GLAccount> findByCode(String code);

    List<GLAccount> findByAccountType(GLAccount.AccountType accountType);

    List<GLAccount> findByAccountCategory(GLAccount.AccountCategory accountCategory);

    List<GLAccount> findByParentAccount(GLAccount parentAccount);

    List<GLAccount> findByParentAccountIsNull();

    List<GLAccount> findByIsActiveTrue();

    List<GLAccount> findByAllowDirectPostingTrue();

    boolean existsByCode(String code);
}
