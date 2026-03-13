package mate.academy.onlinebookstore.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import mate.academy.onlinebookstore.validation.ValidationConstants;
import mate.academy.onlinebookstore.validation.fieldmatch.FieldMatch;

@Getter
@Setter
@FieldMatch
@Schema(description = "Request for user registration")
public class UserRegistrationRequestDto {
    @NotBlank
    @Email(message = "Invalid email format.")
    @Schema(description = "User email address",
            example = "user@example.com")
    private String email;
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 35)
    @Pattern(regexp = ValidationConstants.PASSWORD_REGEX,
            message = "must contain upper, lower case letters and digits.")
    @Schema(description = "User password. Min 8 characters, upper, lower case letters and digits.",
            example = "Pass@w0rd")
    private String password;
    @NotBlank(message = "Repeat password is required")
    @Schema(description = "Password confirmation. Must match password.")
    private String repeatPassword;
    @NotBlank(message = "First name is required")
    @Schema(description = "First user name")
    private String firstName;
    @NotBlank(message = "Last name is required")
    @Schema(description = "Last user name")
    private String lastName;
    @Schema(description = "User shipping address")
    private String shippingAddress;
}
