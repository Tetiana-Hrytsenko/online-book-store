package mate.academy.onlinebookstore.validation.fieldmatch;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import mate.academy.onlinebookstore.dto.UserRegistrationRequestDto;

public class FieldMatchValidator implements ConstraintValidator<
        FieldMatch,
        UserRegistrationRequestDto> {
    @Override
    public boolean isValid(UserRegistrationRequestDto requestDto,
                           ConstraintValidatorContext constraintValidatorContext) {
        return requestDto.getRepeatPassword().equals(requestDto.getPassword());
    }
}
