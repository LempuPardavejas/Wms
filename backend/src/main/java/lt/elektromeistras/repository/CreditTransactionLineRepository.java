package lt.elektromeistras.repository;

import lt.elektromeistras.domain.CreditTransactionLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CreditTransactionLineRepository extends JpaRepository<CreditTransactionLine, UUID> {

    /**
     * Find all lines for a transaction
     */
    List<CreditTransactionLine> findByTransactionId(UUID transactionId);

    /**
     * Find all lines for a product
     */
    List<CreditTransactionLine> findByProductId(UUID productId);

    /**
     * Find lines by product code
     */
    @Query("SELECT ctl FROM CreditTransactionLine ctl WHERE " +
            "LOWER(ctl.productCode) = LOWER(:productCode)")
    List<CreditTransactionLine> findByProductCode(@Param("productCode") String productCode);
}
