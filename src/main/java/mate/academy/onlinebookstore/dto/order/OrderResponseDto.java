package mate.academy.onlinebookstore.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import mate.academy.onlinebookstore.model.Order;

public record OrderResponseDto(
        Long id,
        Long userId,
        Set<OrderItemResponseDto> orderItems,
        LocalDateTime orderDate,
        BigDecimal total,
        Order.Status status) {
}
