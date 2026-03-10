package mate.academy.onlinebookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for user login")
public record UserLoginRequestDto(
        @NotBlank
        @Email(message = "Invalid email format")
        @Schema(description = "User email address")
        String email,
        @NotBlank
        @Size(min = 8, max = 35)
        @Schema(description = "User password")
        String password) {
}
