package mate.academy.onlinebookstore.service.user.impl;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ShoppingCartService shoppingCartService;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Verify successful registration with valid request")
    void register_WithValidRequestDto_Ok() throws RegistrationException {
        //given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("test@test.com")
                .setPassword("Password123")
                .setRepeatPassword("Password123")
                .setFirstName("User")
                .setLastName("Test");

        User user = new User();
        user.setEmail(requestDto.getPassword());

        Role role = new Role();
        role.setName(Role.RoleName.ROLE_USER);

        UserResponseDto expected = new UserResponseDto();
        expected.setId(1L);
        expected.setEmail("test@test.com");
        expected.setFirstName("User");
        expected.setLastName("Test");

        when(userRepository.existsUserByEmail(requestDto.getEmail())).thenReturn(false);
        when(userMapper.toModel(requestDto)).thenReturn(user);
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");
        when(roleRepository.findRoleByName(Role.RoleName.ROLE_USER)).thenReturn(Optional.of(role));
        when(userMapper.toDto(user)).thenReturn(expected);

        //when
        UserResponseDto actual = userService.register(requestDto);

        //then
        assertEquals(expected, actual);
        assertEquals("encodedPassword", user.getPassword());
        assertEquals(Set.of(role), user.getRoles());
        verify(userRepository).save(user);
        verify(shoppingCartService).registerNewShoppingCart(user);
    }

    @Test
    @DisplayName("Verify the method with existing email throws RegistrationException")
    void register_WithExistingEmail_ThrowsRegistrationException() {
        //given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("test@test.com");

        when(userRepository.existsUserByEmail("test@test.com")).thenReturn(true);

        //when
        RegistrationException ex = assertThrows(RegistrationException.class,
                () -> userService.register(requestDto));

        //then
        assertEquals("Can't register user. User with email: test@test.com already exists.",
                ex.getMessage());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(userMapper, passwordEncoder, roleRepository, shoppingCartService);
    }

    @Test
    @DisplayName("Verify the method when role is missing throws EntityNotFoundException")
    void register_WhenRoleNotFound_ThrowsEntityNotFoundException() {
        //given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("test@test.com");
        requestDto.setPassword("Password123");

        when(userRepository.existsUserByEmail("test@test.com")).thenReturn(false);
        when(userMapper.toModel(requestDto)).thenReturn(new User());
        when(roleRepository.findRoleByName(Role.RoleName.ROLE_USER)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> userService.register(requestDto));

        //then
        assertEquals("Can't find role: " + Role.RoleName.ROLE_USER, ex.getMessage());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(shoppingCartService);
    }
}
