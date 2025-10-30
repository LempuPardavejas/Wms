package lt.elektromeistras.repository;

import lt.elektromeistras.domain.Order;
import lt.elektromeistras.domain.OrderLine;
import lt.elektromeistras.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, UUID> {
    List<OrderLine> findByOrder(Order order);
    List<OrderLine> findByProduct(Product product);
}
