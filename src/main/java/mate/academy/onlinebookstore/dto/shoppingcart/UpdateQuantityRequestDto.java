package mate.academy.onlinebookstore.dto.shoppingcart;

import jakarta.validation.constraints.Positive;

public record UpdateQuantityRequestDto(
        @Positive
        int quantity) {
}
