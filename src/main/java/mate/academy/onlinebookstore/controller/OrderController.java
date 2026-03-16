package mate.academy.onlinebookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.onlinebookstore.dto.order.CompleteOrderRequestDto;
import mate.academy.onlinebookstore.dto.order.OrderItemResponseDto;
import mate.academy.onlinebookstore.dto.order.OrderResponseDto;
import mate.academy.onlinebookstore.dto.order.UpdateOrderStatusRequestDto;
import mate.academy.onlinebookstore.model.User;
import mate.academy.onlinebookstore.service.order.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order management", description = "Endpoints for managing orders")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @Operation(summary = "Place a new order",
            description = "Converts the current user's shopping cart into a formal order. "
                    + "Calculates the total price, saves the shipping address, and clears the "
                    + "shopping cart")
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public OrderResponseDto completeOrder(@RequestBody @Valid CompleteOrderRequestDto requestDto,
                                          @AuthenticationPrincipal User user) {
        return orderService.completeOrder(requestDto, user);
    }

    @Operation(
            summary = "Retrieve user order history",
            description = "Returns a list of all orders associated with the authenticated user. "
                    + "Includes item details, totals, and current statuses."
    )
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public Page<OrderResponseDto> getOrders(Pageable pageable, @AuthenticationPrincipal User user) {
        return orderService.getOrders(pageable, user.getId());
    }

    @Operation(
            summary = "Get order items information",
            description = "Returns A list of all items in a specific order."
    )
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{orderId}/items")
    public Page<OrderItemResponseDto> getOrderItems(
            @PathVariable Long orderId,
            Pageable pageable) {
        return orderService.getOrderItems(orderId, pageable);
    }

    @Operation(
            summary = "Get detailed order item information",
            description = "Fetches the complete details of a specific order item using its unique"
                    + " identifier."
    )
    @PreAuthorize("hasRole('USER')")
    @GetMapping("{orderId}/items/{id}")
    public OrderItemResponseDto getOrderItem(
            @PathVariable Long orderId,
            @PathVariable Long id) {
        return orderService.getOrderItem(orderId, id);
    }

    @Operation(
            summary = "Update order status",
            description = "Allows updating the status of an existing order. Accepts  a valid "
                    + "status string defined in the OrderStatus enum.")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public OrderResponseDto updateOrderStatus(@PathVariable Long id,
                                              @RequestBody
                                              @Valid UpdateOrderStatusRequestDto requestDto) {
        return orderService.updateOrderStatus(id, requestDto);
    }
}
