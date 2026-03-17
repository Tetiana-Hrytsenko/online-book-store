package mate.academy.onlinebookstore.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import mate.academy.onlinebookstore.model.Order;

public record UpdateOrderStatusRequestDto(
        @NotNull
        @Schema(example = "COMPLETED")
        Order.Status status) {
}
