package mate.academy.onlinebookstore.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import mate.academy.onlinebookstore.config.MapperConfig;
import mate.academy.onlinebookstore.dto.shoppingcart.ShoppingCartResponseDto;
import mate.academy.onlinebookstore.model.CartItem;
import mate.academy.onlinebookstore.model.ShoppingCart;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface ShoppingCartMapper {
    @Mapping(target = "userId", source = "user.id")
    ShoppingCartResponseDto toDto(ShoppingCart shoppingCart);

    @AfterMapping
    default void setCartItemIds(@MappingTarget ShoppingCartResponseDto responseDto,
                                ShoppingCart shoppingCart) {
        Set<Long> cartItemIds = shoppingCart.getCartItems().stream()
                .map(CartItem::getId)
                .collect(Collectors.toSet());
        responseDto.setCartItemIds(cartItemIds);
    }
}
