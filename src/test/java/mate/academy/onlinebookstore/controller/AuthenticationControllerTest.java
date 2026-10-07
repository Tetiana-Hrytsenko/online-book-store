package mate.academy.onlinebookstore.controller;

import lombok.SneakyThrows;
import mate.academy.onlinebookstore.dto.user.UserLoginRequestDto;
import mate.academy.onlinebookstore.dto.user.UserRegistrationRequestDto;
import mate.academy.onlinebookstore.model.User;
import mate.academy.onlinebookstore.repository.shoppingcart.ShoppingCartRepository;
import mate.academy.onlinebookstore.repository.user.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;

import java.sql.Connection;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @BeforeAll
    static void beforeAll(@Autowired DataSource dataSource,
                          @Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
    }

    @Test
    void register() {
    }

    @Test
    @DisplayName("POST /auth/registration creates user and shopping cart, returns status 200")
    @Sql(scripts = "classpath:database/user/remove-registered-user.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void register_WithValidRequest_ReturnsCreated() throws Exception {
        //given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("newuser@test.com")
                .setPassword("Password123")
                .setRepeatPassword("Password123")
                .setFirstName("New")
                .setLastName("User");

        //when
        mockMvc.perform(post("/auth/registration")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.password").doesNotExist());

        User saved = userRepository.findUserByEmail("newuser@test.com").orElseThrow();
        assertTrue(shoppingCartRepository.existsById(saved.getId()));
    }

    @Test
    @DisplayName("POST /auth/registration returns 409 with a user that already exists")
    void register_WithExistingEmail_ReturnsConflict() throws Exception {
        //given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("alice@example.com")
                .setPassword("Password123")
                .setRepeatPassword("Password123")
                .setFirstName("Alice")
                .setLastName("User");

        //when
        mockMvc.perform(post("/auth/registration")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isConflict());
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    @DisplayName("POST /auth/registration returns 400 with invalid request dto")
    void register_WithInvalidRequest_ReturnsBadRequest(UserRegistrationRequestDto dto)
            throws Exception {
        mockMvc.perform(post("/auth/registration")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login successfully logins user")
    void login_WithValidEmailAndPassword_Ok() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto("alice@example.com", "Alice123");
        mockMvc.perform(post("/auth/login")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/login creates token")
    void login_WithValidEmailAndPassword_TokenIsCreated() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto("alice@example.com", "Alice123");

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asString();

        mockMvc.perform(get("/cart").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /auth/login returns 401")
    void login_WithInvalidPassword_TokenIsCreated() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto("user@example.com", "WrongPassword");

        mockMvc.perform(post("/auth/login")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    static Stream<UserRegistrationRequestDto> invalidRequests() {
        return Stream.of(
                // empty email
                new UserRegistrationRequestDto()
                        .setEmail("")
                        .setPassword("Password123")
                        .setRepeatPassword("Password123")
                        .setFirstName("New")
                        .setLastName("User"),
                // email without @
                new UserRegistrationRequestDto()
                        .setEmail("not-an-email")
                        .setPassword("Password123")
                        .setRepeatPassword("Password123")
                        .setFirstName("New")
                        .setLastName("User"),
                // short password
                new UserRegistrationRequestDto()
                        .setEmail("newuser@test.com")
                        .setPassword("123")
                        .setRepeatPassword("123")
                        .setFirstName("New")
                        .setLastName("User"),
                // passwords don't match
                new UserRegistrationRequestDto()
                        .setEmail("newuser@test.com")
                        .setPassword("Password123")
                        .setRepeatPassword("Different456")
                        .setFirstName("New")
                        .setLastName("User")
        );
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/user/remove-registered-user.sql"));
        }
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }
}
