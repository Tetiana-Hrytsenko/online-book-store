package mate.academy.onlinebookstore.service.user;

import lombok.RequiredArgsConstructor;
import mate.academy.onlinebookstore.dto.UserRegistrationRequestDto;
import mate.academy.onlinebookstore.dto.UserResponseDto;
import mate.academy.onlinebookstore.exception.RegistrationException;
import mate.academy.onlinebookstore.mapper.UserMapper;
import mate.academy.onlinebookstore.model.User;
import mate.academy.onlinebookstore.repository.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.existsUserByEmail(requestDto.getEmail())) {
            throw new RegistrationException("Can't register user. User with email: "
                    + requestDto.getEmail() + " already exists.");
        }
        User user = userMapper.toModel(requestDto);
        return userMapper.toDto(userRepository.save(user));
    }
}
