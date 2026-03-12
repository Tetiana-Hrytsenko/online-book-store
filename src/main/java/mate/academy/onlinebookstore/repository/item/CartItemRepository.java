package mate.academy.onlinebookstore.repository.item;

import java.util.Optional;
import mate.academy.onlinebookstore.model.CartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByShoppingCartIdAndBookId(Long cartId, Long bookId);

    @EntityGraph(attributePaths = {"book"})
    Page<CartItem> findAllByShoppingCartUserId(Long id, Pageable pageable);
}
