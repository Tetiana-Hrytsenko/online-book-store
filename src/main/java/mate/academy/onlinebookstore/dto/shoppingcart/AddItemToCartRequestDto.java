package mate.academy.onlinebookstore.dto.shoppingcart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddItemToCartRequestDto(
        @NotNull
        @Positive
        Long bookId,
        @Positive
        int quantity) {
}
