package mate.academy.onlinebookstore.mapper;

import mate.academy.onlinebookstore.config.MapperConfig;
import mate.academy.onlinebookstore.dto.shoppingcart.AddItemToCartRequestDto;
import mate.academy.onlinebookstore.dto.shoppingcart.CartItemResponseDto;
import mate.academy.onlinebookstore.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class, uses = BookMapper.class)
public interface CartItemMapper {
    @Mapping(target = "book", source = "bookId", qualifiedByName = "bookFromId")
    CartItem toModel(AddItemToCartRequestDto requestDto);

    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "title", source = "book.title")
    CartItemResponseDto toDto(CartItem cartItem);
}
