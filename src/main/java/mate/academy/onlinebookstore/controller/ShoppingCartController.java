package mate.academy.onlinebookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.onlinebookstore.dto.shoppingcart.AddItemToCartRequestDto;
import mate.academy.onlinebookstore.dto.shoppingcart.ShoppingCartResponseDto;
import mate.academy.onlinebookstore.dto.shoppingcart.UpdateQuantityRequestDto;
import mate.academy.onlinebookstore.model.User;
import mate.academy.onlinebookstore.service.shoppingcart.ShoppingCartService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Shopping cart management", description = "Endpoints for managing chopping carts")
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    @Operation(summary = "Add book to shopping cart",
            description = "Add a specific book to authenticated user's shopping cart with "
                    + "optional sorting and filtering items")
    @PreAuthorize("hasRole('USER')")
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingCartResponseDto addItemToCart(@RequestBody
                                                 @Valid
                                                 AddItemToCartRequestDto requestDto,
                                                 @AuthenticationPrincipal User user,
                                                 Pageable pageable) {
        return shoppingCartService.addItem(requestDto, user.getId(), pageable);
    }

    @Operation(summary = "Get shopping cart",
            description = "Returns the authenticated user's shopping cart with optional sorting "
                    + "and filtering items")
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ShoppingCartResponseDto getShoppingCart(@AuthenticationPrincipal User user,
                                                   Pageable pageable) {
        return shoppingCartService.getShoppingCart(user.getId(), pageable);
    }

    @Operation(summary = "Update book quantity",
            description = "Updates book quantity in shopping cart and returns updated cart")
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/items/{cartItemId}")
    public ShoppingCartResponseDto updateBookQuantity(@PathVariable Long cartItemId,
                                                      @RequestBody
                                                      @Valid
                                                      UpdateQuantityRequestDto requestDto,
                                                      @AuthenticationPrincipal User user,
                                                      Pageable pageable) {
        return shoppingCartService.updateBookQuantity(cartItemId, requestDto, user.getId(),
                pageable);
    }

    @Operation(summary = "Delete book",
            description = "Deletes a specific book from shopping cart")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/items/{cartItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long cartItemId) {
        shoppingCartService.deleteItem(cartItemId);
    }
}
