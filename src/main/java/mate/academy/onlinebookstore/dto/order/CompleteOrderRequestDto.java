package mate.academy.onlinebookstore.dto.order;

import static mate.academy.onlinebookstore.validation.ValidationConstants.SHIPPING_ADDRESS_REGEX;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CompleteOrderRequestDto(
        @NotBlank
        @Pattern(regexp = SHIPPING_ADDRESS_REGEX)
        String shippingAddress) {
}
