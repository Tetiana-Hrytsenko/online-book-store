package mate.academy.onlinebookstore.repository.orderitem;

import java.util.Optional;
import mate.academy.onlinebookstore.model.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @EntityGraph(attributePaths = "book")
    Page<OrderItem> findByOrderId(Long orderId, Pageable pageable);

    Optional<OrderItem> findByIdAndOrderId(Long id, Long orderId);
}
