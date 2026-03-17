package mate.academy.onlinebookstore.mapper;

import mate.academy.onlinebookstore.config.MapperConfig;
import mate.academy.onlinebookstore.dto.order.OrderItemResponseDto;
import mate.academy.onlinebookstore.model.CartItem;
import mate.academy.onlinebookstore.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface OrderItemMapper {
    @Mapping(target = "price", source = "book.price")
    @Mapping(target = "id", ignore = true)
    OrderItem fromCartItem(CartItem cartItem);

    @Mapping(target = "bookId", source = "book.id")
    OrderItemResponseDto toDto(OrderItem orderItem);
}
