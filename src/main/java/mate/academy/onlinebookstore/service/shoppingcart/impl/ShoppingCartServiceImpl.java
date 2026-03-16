package mate.academy.onlinebookstore.service.shoppingcart.impl;

import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mate.academy.onlinebookstore.dto.shoppingcart.AddItemToCartRequestDto;
import mate.academy.onlinebookstore.dto.shoppingcart.ShoppingCartResponseDto;
import mate.academy.onlinebookstore.dto.shoppingcart.UpdateQuantityRequestDto;
import mate.academy.onlinebookstore.exception.EntityNotFoundException;
import mate.academy.onlinebookstore.mapper.CartItemMapper;
import mate.academy.onlinebookstore.mapper.ShoppingCartMapper;
import mate.academy.onlinebookstore.model.Book;
import mate.academy.onlinebookstore.model.CartItem;
import mate.academy.onlinebookstore.model.ShoppingCart;
import mate.academy.onlinebookstore.model.User;
import mate.academy.onlinebookstore.repository.book.BookRepository;
import mate.academy.onlinebookstore.repository.item.CartItemRepository;
import mate.academy.onlinebookstore.repository.shoppingcart.ShoppingCartRepository;
import mate.academy.onlinebookstore.service.shoppingcart.ShoppingCartService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Transactional
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final BookRepository bookRepository;
    private final CartItemMapper cartItemMapper;
    private final CartItemRepository cartItemRepository;
    private final ShoppingCartMapper shoppingCartMapper;

    @Override
    public void registerNewShoppingCart(User user) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);
        shoppingCartRepository.save(shoppingCart);
    }

    @Override
    public ShoppingCartResponseDto addItem(AddItemToCartRequestDto requestDto, Long userId) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Can't find shopping cart with "
                        + "user id: " + userId));
        Optional<CartItem> existingItem =
                cartItemRepository.findByShoppingCartIdAndBookId(shoppingCart.getId(),
                        requestDto.bookId());
        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + requestDto.quantity());
        } else {
            Book book = bookRepository.findById(requestDto.bookId())
                    .orElseThrow(() -> new EntityNotFoundException("Can't find book with id: "
                            + requestDto.bookId()));
            CartItem cartItem = cartItemMapper.toModel(requestDto);
            cartItem.setBook(book);
            cartItem.setShoppingCart(shoppingCart);
            shoppingCart.getCartItems().add(cartItemRepository.save(cartItem));
        }
        return getShoppingCart(userId);
    }

    @Override
    public ShoppingCartResponseDto getShoppingCart(Long userId) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Can't find shopping cart with "
                        + "user id: " + userId));
        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Override
    public ShoppingCartResponseDto updateBookQuantity(Long cartItemId,
                                                      UpdateQuantityRequestDto requestDto,
                                                      Long userId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(
                () -> new EntityNotFoundException("Can't find cart item by id: " + cartItemId));
        cartItem.setQuantity(requestDto.quantity());
        return getShoppingCart(userId);
    }

    @Override
    public void deleteItem(Long id) {
        cartItemRepository.deleteById(id);
    }
}
