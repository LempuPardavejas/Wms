package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Product;
import lt.elektromeistras.domain.Return;
import lt.elektromeistras.domain.ReturnLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReturnLineRepository extends JpaRepository<ReturnLine, UUID> {
    List<ReturnLine> findByReturnEntity(Return returnEntity);

    List<ReturnLine> findByProduct(Product product);

    @Query("SELECT rl FROM ReturnLine rl WHERE rl.restockEligible = true AND rl.restocked = false")
    List<ReturnLine> findPendingRestockLines();

    @Query("SELECT rl FROM ReturnLine rl WHERE rl.returnEntity.id = :returnId AND rl.product.id = :productId")
    List<ReturnLine> findByReturnAndProduct(@Param("returnId") UUID returnId,
                                            @Param("productId") UUID productId);
}
