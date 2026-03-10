package mate.academy.onlinebookstore.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request for category creation")
public record CreateCategoryRequestDto(
        @NotBlank
        @Schema(description = "Category name")
        String name,
        @Schema(description = "Category description")
        String description) {
}
