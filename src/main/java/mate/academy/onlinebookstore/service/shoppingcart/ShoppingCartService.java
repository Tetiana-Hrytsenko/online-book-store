package mate.academy.onlinebookstore.service.shoppingcart;

import mate.academy.onlinebookstore.dto.shoppingcart.AddItemToCartRequestDto;
import mate.academy.onlinebookstore.dto.shoppingcart.ShoppingCartResponseDto;
import mate.academy.onlinebookstore.dto.shoppingcart.UpdateQuantityRequestDto;
import mate.academy.onlinebookstore.model.User;
import org.springframework.data.domain.Pageable;

public interface ShoppingCartService {
    void registerNewShoppingCart(User user);

    ShoppingCartResponseDto addItem(AddItemToCartRequestDto requestDto,
                                    Long userId,
                                    Pageable pageable);

    ShoppingCartResponseDto updateBookQuantity(Long cartItemId,
                                               UpdateQuantityRequestDto requestDto,
                                               Long userId,
                                               Pageable pageable);

    void deleteItem(Long id);

    ShoppingCartResponseDto getShoppingCart(Long userId, Pageable pageable);
}
