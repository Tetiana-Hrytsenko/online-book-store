package mate.academy.onlinebookstore.service.user;

import mate.academy.onlinebookstore.dto.UserRegistrationRequestDto;
import mate.academy.onlinebookstore.dto.UserResponseDto;
import mate.academy.onlinebookstore.exception.RegistrationException;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;
}
