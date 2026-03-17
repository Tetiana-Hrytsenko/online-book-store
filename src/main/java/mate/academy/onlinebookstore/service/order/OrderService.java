package mate.academy.onlinebookstore.service.order;

import mate.academy.onlinebookstore.dto.order.CompleteOrderRequestDto;
import mate.academy.onlinebookstore.dto.order.OrderItemResponseDto;
import mate.academy.onlinebookstore.dto.order.OrderResponseDto;
import mate.academy.onlinebookstore.dto.order.UpdateOrderStatusRequestDto;
import mate.academy.onlinebookstore.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponseDto completeOrder(CompleteOrderRequestDto requestDto, User user);

    Page<OrderResponseDto> getOrders(Pageable pageable, Long userId);

    Page<OrderItemResponseDto> getOrderItems(Long orderId, Pageable pageable);

    OrderItemResponseDto getOrderItem(Long orderId, Long id);

    OrderResponseDto updateOrderStatus(Long id, UpdateOrderStatusRequestDto requestDto);
}
