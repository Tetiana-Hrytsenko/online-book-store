package mate.academy.onlinebookstore.service.user.impl;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import mate.academy.onlinebookstore.dto.UserRegistrationRequestDto;
import mate.academy.onlinebookstore.dto.UserResponseDto;
import mate.academy.onlinebookstore.exception.EntityNotFoundException;
import mate.academy.onlinebookstore.exception.RegistrationException;
import mate.academy.onlinebookstore.mapper.UserMapper;
import mate.academy.onlinebookstore.model.Role;
import mate.academy.onlinebookstore.model.User;
import mate.academy.onlinebookstore.repository.role.RoleRepository;
import mate.academy.onlinebookstore.repository.user.UserRepository;
import mate.academy.onlinebookstore.service.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.existsUserByEmail(requestDto.getEmail())) {
            throw new RegistrationException("Can't register user. User with email: "
                    + requestDto.getEmail() + " already exists.");
        }
        User user = userMapper.toModel(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        Role role = roleRepository.findRoleByName(Role.RoleName.ROLE_USER).orElseThrow(
                () -> new EntityNotFoundException("Can't find role: " + Role.RoleName.ROLE_USER)
        );
        user.setRoles(Set.of(role));
        return userMapper.toDto(userRepository.save(user));
    }
}
