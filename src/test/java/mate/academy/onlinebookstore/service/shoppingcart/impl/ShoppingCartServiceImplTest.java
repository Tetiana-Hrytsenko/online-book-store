package mate.academy.onlinebookstore.service.shoppingcart.impl;

import mate.academy.onlinebookstore.dto.shoppingcart.AddItemToCartRequestDto;
import mate.academy.onlinebookstore.dto.shoppingcart.ShoppingCartResponseDto;
import mate.academy.onlinebookstore.dto.shoppingcart.UpdateQuantityRequestDto;
import mate.academy.onlinebookstore.exception.EntityNotFoundException;
import mate.academy.onlinebookstore.mapper.CartItemMapper;
import mate.academy.onlinebookstore.mapper.ShoppingCartMapper;
import mate.academy.onlinebookstore.model.*;
import mate.academy.onlinebookstore.repository.book.BookRepository;
import mate.academy.onlinebookstore.repository.cartitem.CartItemRepository;
import mate.academy.onlinebookstore.repository.shoppingcart.ShoppingCartRepository;
import mate.academy.onlinebookstore.service.shoppingcart.ShoppingCartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTest {
    public static final Long USER_ID = 1L;

    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private CartItemMapper cartItemMapper;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ShoppingCartMapper shoppingCartMapper;
    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartServiceImpl;

    @Test
    @DisplayName("Verify that the method registers new shopping cart with valid user")
    void registerNewShoppingCart_withValidUser_Ok() {
        //given
        User user = new User();
        user.setId(1L);
        ArgumentCaptor<ShoppingCart> cartCaptor = ArgumentCaptor.forClass(ShoppingCart.class);

        //when
        shoppingCartServiceImpl.registerNewShoppingCart(user);

        //then
        verify(shoppingCartRepository).save(cartCaptor.capture());
        ShoppingCart savedCart = cartCaptor.getValue();
        assertNotNull(savedCart);
        assertEquals(user, savedCart.getUser());
        verifyNoMoreInteractions(shoppingCartRepository);
    }

    @Test
    @DisplayName("Verify adding new item to a shopping cart with valid AddItemToCartRequestDto and userId")
    void addItem_WithItemAlreadyInCart_IncreaseItemQuantity() {
        // given
        Long bookId = 5L;
        Long cartId = 1L;
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(cartId);
        shoppingCart.setCartItems(new HashSet<>());
        AddItemToCartRequestDto requestDto = new AddItemToCartRequestDto(bookId, 2);
        CartItem existingCartItem = new CartItem();
        existingCartItem.setQuantity(3);

        ShoppingCartResponseDto expected = mock(ShoppingCartResponseDto.class);

        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        when(cartItemRepository.findByShoppingCartIdAndBookId(cartId, bookId))
                .thenReturn(Optional.of(existingCartItem));
        when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(expected);

        // when
        ShoppingCartResponseDto actual = shoppingCartServiceImpl.addItem(requestDto, USER_ID);

        // then
        assertEquals(5, existingCartItem.getQuantity());
        assertSame(expected, actual);
        verify(cartItemRepository, never()).save(any());
        verifyNoInteractions(bookRepository, cartItemMapper);

    }

    @Test
    @DisplayName("Verify that new cart item is created and add to a cart")
    void addItem_WithItemNotInCart_CreateNewItem() {
        // given
        Long bookId = 5L;
        Book book = new Book();
        book.setId(bookId);
        Long cartId = 1L;
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(cartId);
        shoppingCart.setCartItems(new HashSet<>());
        AddItemToCartRequestDto requestDto = new AddItemToCartRequestDto(bookId, 2);
        CartItem newItem = new CartItem();
        newItem.setQuantity(requestDto.quantity());

        ShoppingCartResponseDto expected = mock(ShoppingCartResponseDto.class);

        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        when(cartItemRepository.findByShoppingCartIdAndBookId(cartId, bookId))
                .thenReturn(Optional.empty());
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(cartItemMapper.toModel(requestDto)).thenReturn(newItem);
        when(cartItemRepository.save(newItem)).thenReturn(newItem);
        when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(expected);

        // when
        ShoppingCartResponseDto actual = shoppingCartServiceImpl.addItem(requestDto, USER_ID);

        // then
        assertSame(expected, actual);
        assertSame(book, newItem.getBook());
        assertSame(shoppingCart, newItem.getShoppingCart());
        assertTrue(shoppingCart.getCartItems().contains(newItem));
        verify(cartItemRepository).save(newItem);
    }

    @Test
    @DisplayName("Verify that the method throws exception when shopping cart does not exist")
    void addItem_WithShoppingCartNotFound_ThrowException() {
        //given
        AddItemToCartRequestDto requestDto = new AddItemToCartRequestDto(5L, 2);

        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> shoppingCartServiceImpl.addItem(requestDto, USER_ID));

        //then
        assertEquals("Can't find shopping cart with user id: " + USER_ID, ex.getMessage());
        verifyNoInteractions(cartItemRepository, bookRepository, cartItemMapper);
    }

    @Test
    @DisplayName("Verify that the method trows an exception when book does not exist")
    void addItem_WithBookNotFound_ThrowException() {
        //given
        Long bookId = 50L;
        Long cartId = 1L;
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(cartId);
        AddItemToCartRequestDto requestDto = new AddItemToCartRequestDto(bookId, 2);

        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        when(cartItemRepository.findByShoppingCartIdAndBookId(cartId, bookId))
                .thenReturn(Optional.empty());
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> shoppingCartServiceImpl.addItem(requestDto, USER_ID));

        //then
        assertEquals("Can't find book with id: " + bookId, ex.getMessage());
        verify(cartItemRepository, never()).save(any());
        verifyNoInteractions(cartItemMapper);
    }

    @Test
    @DisplayName("Verify correct receiving shopping cart with valid user ID")
    void getShoppingCart_WithValidUserId_Ok() {
        //given
        ShoppingCart cart = new ShoppingCart();
        ShoppingCartResponseDto expected = mock(ShoppingCartResponseDto.class);

        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(shoppingCartMapper.toDto(cart)).thenReturn(expected);

        //when
        ShoppingCartResponseDto actual = shoppingCartServiceImpl.getShoppingCart(USER_ID);

        //then
        assertSame(expected, actual);
        verify(shoppingCartRepository).findByUserId(USER_ID);
        verify(shoppingCartMapper).toDto(cart);
        verifyNoMoreInteractions(shoppingCartRepository, shoppingCartMapper);
    }

    @Test
    @DisplayName("Verify the method throws an exception with not existing user ID")
    void getShoppingCart_WithNotExistingUserId_ThrowEntityNotFoundException() {
        //given
        Long invalidUserId = 3L;
        when(shoppingCartRepository.findByUserId(invalidUserId)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> shoppingCartServiceImpl.getShoppingCart(invalidUserId));

        //then
        assertEquals("Can't find shopping cart with user id: " + invalidUserId, ex.getMessage());
        verifyNoInteractions(shoppingCartMapper);
    }


    @Test
    @DisplayName("Verify the method update quantity of cartItem")
    void updateBookQuantity_WithValidCartItem_Ok() {
        //given
        Long cartItemId = 5L;
        CartItem cartItem = new CartItem();
        cartItem.setQuantity(1);
        ShoppingCart shoppingCart = new ShoppingCart();
        UpdateQuantityRequestDto requestDto = new UpdateQuantityRequestDto(3);

        ShoppingCartResponseDto expected = mock(ShoppingCartResponseDto.class);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));
        when(shoppingCartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(shoppingCart));
        when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(expected);

        //when
        ShoppingCartResponseDto actual = shoppingCartServiceImpl.updateBookQuantity(cartItemId, requestDto, USER_ID);

        //then
        assertEquals(3, cartItem.getQuantity());
        assertSame(expected, actual);
        verify(cartItemRepository).findById(cartItemId);
        verify(shoppingCartMapper).toDto(shoppingCart);
    }

    @Test
    @DisplayName("Verify the method throws exception with not existing cartItemId")
    void updateBookQuantity_WithNotExistingCartItemId_ThrowException() {
        //given
        Long invalidCartItemId = 99L;
        UpdateQuantityRequestDto requestDto = new UpdateQuantityRequestDto(8);

        when(cartItemRepository.findById(invalidCartItemId)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> shoppingCartServiceImpl.updateBookQuantity(invalidCartItemId, requestDto, USER_ID));

        //then
        assertEquals("Can't find cart item by id: " + invalidCartItemId, ex.getMessage());
        verifyNoInteractions(shoppingCartRepository, shoppingCartMapper);
    }

    @Test
    @DisplayName("Verify the method deletes cart item")
    void deleteItem_WithValidCartItemId_Ok() {
        //given
        Long cartItemId = 2L;

        //when
        shoppingCartServiceImpl.deleteItem(cartItemId);

        //then
        verify(cartItemRepository).deleteById(cartItemId);
    }

    @Test
    @DisplayName("Verify that clear removes all items and saves the cart")
    void clear_WithItemsInCart_RemovesAllItemsAndSavesCart() {
        //given
        ShoppingCart cart = new ShoppingCart();
        cart.setId(10L);
        cart.setCartItems(new HashSet<>());

        CartItem first = new CartItem();
        first.setId(1L);
        CartItem second = new CartItem();
        second.setId(2L);
        cart.getCartItems().add(first);
        cart.getCartItems().add(second);
        assertEquals(2, cart.getCartItems().size());

        //when
        shoppingCartServiceImpl.clear(cart);

        //then
        assertTrue(cart.getCartItems().isEmpty());
        verify(shoppingCartRepository).save(cart);
        verifyNoMoreInteractions(shoppingCartRepository);
    }
}
