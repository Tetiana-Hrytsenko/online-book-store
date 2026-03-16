package mate.academy.onlinebookstore.service.shoppingcart;

import mate.academy.onlinebookstore.dto.shoppingcart.AddItemToCartRequestDto;
import mate.academy.onlinebookstore.dto.shoppingcart.ShoppingCartResponseDto;
import mate.academy.onlinebookstore.dto.shoppingcart.UpdateQuantityRequestDto;
import mate.academy.onlinebookstore.model.User;

public interface ShoppingCartService {
    void registerNewShoppingCart(User user);

    ShoppingCartResponseDto addItem(AddItemToCartRequestDto requestDto,
                                    Long userId);

    ShoppingCartResponseDto updateBookQuantity(Long cartItemId,
                                               UpdateQuantityRequestDto requestDto,
                                               Long userId);

    void deleteItem(Long id);

    ShoppingCartResponseDto getShoppingCart(Long userId);
}
