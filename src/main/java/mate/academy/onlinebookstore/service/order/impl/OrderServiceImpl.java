package mate.academy.onlinebookstore.service.order.impl;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mate.academy.onlinebookstore.dto.order.CompleteOrderRequestDto;
import mate.academy.onlinebookstore.dto.order.OrderItemResponseDto;
import mate.academy.onlinebookstore.dto.order.OrderResponseDto;
import mate.academy.onlinebookstore.dto.order.UpdateOrderStatusRequestDto;
import mate.academy.onlinebookstore.exception.EntityNotFoundException;
import mate.academy.onlinebookstore.mapper.OrderItemMapper;
import mate.academy.onlinebookstore.mapper.OrderMapper;
import mate.academy.onlinebookstore.model.Order;
import mate.academy.onlinebookstore.model.OrderItem;
import mate.academy.onlinebookstore.model.ShoppingCart;
import mate.academy.onlinebookstore.model.User;
import mate.academy.onlinebookstore.repository.order.OrderRepository;
import mate.academy.onlinebookstore.repository.orderitem.OrderItemRepository;
import mate.academy.onlinebookstore.repository.shoppingcart.ShoppingCartRepository;
import mate.academy.onlinebookstore.service.order.OrderService;
import mate.academy.onlinebookstore.service.shoppingcart.ShoppingCartService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Transactional
@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ShoppingCartService shoppingCartService;

    @Override
    public OrderResponseDto completeOrder(CompleteOrderRequestDto requestDto, User user) {
        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.Status.PENDING);
        order.setShippingAddress(requestDto.shippingAddress());
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Can't find shopping cart with "
                        + "user id: " + user.getId()));
        Set<OrderItem> orderItems = createOrderItems(shoppingCart, order);
        order.setOrderItems(orderItems);
        order.setTotal(calculateTotal(orderItems));
        Order save = orderRepository.save(order);
        shoppingCartService.clear(shoppingCart);
        return orderMapper.toDto(save);
    }

    @Override
    public Page<OrderResponseDto> getOrders(Pageable pageable, Long userId) {
        return orderRepository.findAllByUserId(pageable, userId)
                .map(orderMapper::toDto);
    }

    @Override
    public Page<OrderItemResponseDto> getOrderItems(Long orderId, Pageable pageable) {
        return orderItemRepository.findByOrderId(orderId, pageable)
                .map(orderItemMapper::toDto);
    }

    @Override
    public OrderItemResponseDto getOrderItem(Long id, Long orderId) {
        OrderItem orderItem = orderItemRepository.findByIdAndOrderId(orderId, id).orElseThrow(
                () -> new EntityNotFoundException("Can't find order item by id: " + id));
        return orderItemMapper.toDto(orderItem);
    }

    @Override
    public OrderResponseDto updateOrderStatus(Long id, UpdateOrderStatusRequestDto requestDto) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find order by id: " + id));
        order.setStatus(requestDto.status());
        return orderMapper.toDto(order);
    }

    private Set<OrderItem> createOrderItems(ShoppingCart shoppingCart, Order order) {
        return shoppingCart.getCartItems()
                .stream()
                .map(cartItem -> {
                    OrderItem orderItem = orderItemMapper.fromCartItem(cartItem);
                    orderItem.setOrder(order);
                    return orderItem;
                })
                .collect(Collectors.toSet());
    }

    private BigDecimal calculateTotal(Set<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return orderItems.stream()
                .map(this::calculateItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateItemTotal(OrderItem orderItem) {
        BigDecimal quantity = BigDecimal.valueOf(orderItem.getQuantity());
        return orderItem.getPrice().multiply(quantity);
    }
}
