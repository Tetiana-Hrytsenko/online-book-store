package mate.academy.onlinebookstore.service.user.impl;

import jakarta.transaction.Transactional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import mate.academy.onlinebookstore.dto.user.UserRegistrationRequestDto;
import mate.academy.onlinebookstore.dto.user.UserResponseDto;
import mate.academy.onlinebookstore.exception.EntityNotFoundException;
import mate.academy.onlinebookstore.exception.RegistrationException;
import mate.academy.onlinebookstore.mapper.UserMapper;
import mate.academy.onlinebookstore.model.Role;
import mate.academy.onlinebookstore.model.User;
import mate.academy.onlinebookstore.repository.role.RoleRepository;
import mate.academy.onlinebookstore.repository.user.UserRepository;
import mate.academy.onlinebookstore.service.shoppingcart.ShoppingCartService;
import mate.academy.onlinebookstore.service.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final ShoppingCartService shoppingCartService;

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
        userRepository.save(user);
        shoppingCartService.registerNewShoppingCart(user);
        return userMapper.toDto(user);
    }
}
