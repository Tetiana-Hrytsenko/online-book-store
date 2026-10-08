package mate.academy.onlinebookstore.controller;

import lombok.SneakyThrows;
import mate.academy.onlinebookstore.dto.shoppingcart.AddItemToCartRequestDto;
import mate.academy.onlinebookstore.dto.shoppingcart.UpdateQuantityRequestDto;
import mate.academy.onlinebookstore.model.User;
import mate.academy.onlinebookstore.repository.user.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;

import java.sql.Connection;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(
        scripts = {"classpath:database/book/insert-three-books.sql"},
        executionPhase = BEFORE_TEST_METHOD)
@Sql(
        scripts = {
                "classpath:database/cartItem/remove-cart-items.sql",
                "classpath:database/book/remove-books.sql"
        },
        executionPhase = AFTER_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingCartControllerTest {
    private static final String USER_EMAIL = "alice@example.com";
    private static final String ADMIN_EMAIL = "admin@admin.com";
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    private User testUser;

    @BeforeAll
    static void beforeAll(@Autowired DataSource dataSource,
                          @Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
    }

    @BeforeAll
    void init() {
        testUser = userRepository.findUserByEmail(USER_EMAIL).orElseThrow();
    }

    @Test
    @DisplayName("POST /cart adds a book to the user's cart")
    void addItemToCart_WithValidRequestDto_ReturnsCreatedStatus() throws Exception {
        //given
        AddItemToCartRequestDto requestDto = new AddItemToCartRequestDto(1L, 2);

        //when
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/cart")
                        .with(user(testUser))
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cartItems.length()").value(1))
                .andExpect(jsonPath("$.cartItems[0].bookId").value(1))
                .andExpect(jsonPath("$.cartItems[0].quantity").value(2));
    }

    @Test
    @DisplayName("POST /cart with a book already in the cart increases quantity")
    @Sql(scripts = {"classpath:database/cartItem/add-cart-items.sql"},
            executionPhase = BEFORE_TEST_METHOD)
    void addItemToCart_WithExistingItem_IncreasesQuantity() throws Exception {
        AddItemToCartRequestDto requestDto = new AddItemToCartRequestDto(1L, 2);

        mockMvc.perform(post("/cart")
                        .with(user(testUser))
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cartItems.length()").value(2))
                .andExpect(jsonPath("$.cartItems[?(@.id == 1)].quantity").value(4));
    }

    @Test
    @DisplayName("POST /cart returns 401 with anonymous user")
    void addItemToCart_WithAnonymousUser_ReturnsUnauthorised() throws Exception {

        AddItemToCartRequestDto requestDto = new AddItemToCartRequestDto(1L, 2);

        mockMvc.perform(post("/cart")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /cart returns 403 with admin role")
    void addItemToCart_WithAdminRole_ReturnsUnauthorised() throws Exception {
        User testAdmin = userRepository.findUserByEmail(ADMIN_EMAIL).orElseThrow();

        AddItemToCartRequestDto requestDto = new AddItemToCartRequestDto(1L, 2);

        mockMvc.perform(post("/cart")
                        .with(user(testAdmin))
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /cart returns the authenticated user's shopping cart")
    @Sql(scripts = {"classpath:database/cartItem/add-cart-items.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getShoppingCart_WithAuthenticatedUser_ReturnsCart() throws Exception {

        mockMvc.perform(get("/cart")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.cartItems.length()").value(2))
                .andExpect(jsonPath("$.cartItems[?(@.id == 1)].bookId").value(1))
                .andExpect(jsonPath("$.cartItems[?(@.id == 2)].bookId").value(2))
                .andExpect(jsonPath("$.cartItems[?(@.id == 1)].quantity").value(2))
                .andExpect(jsonPath("$.cartItems[?(@.id == 2)].quantity").value(4));
    }

    @Test
    @DisplayName("GET /cart returns empty cart when no items were added")
    void getShoppingCart_WithEmptyCart_ReturnsEmptyItems() throws Exception {
        mockMvc.perform(get("/cart")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartItems.length()").value(0));
    }

    @Test
    @DisplayName("PUT /cart/items/{cartItemId} updates quantity of the cart item")
    @Sql(scripts = {"classpath:database/cartItem/add-cart-items.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void updateBookQuantity_WithValidRequest_ReturnsUpdatedCart() throws Exception {

        UpdateQuantityRequestDto requestDto = new UpdateQuantityRequestDto(7);

        mockMvc.perform(put("/cart/items/{cartItemId}", 1L)
                        .with(user(testUser))
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartItems.length()").value(2))
                .andExpect(jsonPath("$.cartItems[?(@.id == 1)].quantity").value(7))
                .andExpect(jsonPath("$.cartItems[?(@.id == 2)].quantity").value(4));
    }

    @Test
    @DisplayName("PUT /cart/items/{cartItemId} with non-existing item returns 404")
    void updateBookQuantity_WithNotExistingItem_ReturnsNotFound() throws Exception {
        UpdateQuantityRequestDto requestDto = new UpdateQuantityRequestDto(3);

        mockMvc.perform(put("/cart/items/{cartItemId}", 999L)
                        .with(user(testUser))
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /cart/items/{cartItemId} with invalid quantity returns 400")
    @Sql(scripts = {"classpath:database/cartItem/add-cart-items.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void updateBookQuantity_WithInvalidQuantity_ReturnsBadRequest() throws Exception {
        UpdateQuantityRequestDto requestDto = new UpdateQuantityRequestDto(0);

        mockMvc.perform(put("/cart/items/{cartItemId}", 1L)
                        .with(user(testUser))
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /cart/items/{cartItemId} with invalid quantity returns 400")
    @Sql(scripts = {"classpath:database/cartItem/add-cart-items.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void deleteItem_WithValidUser_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/cart/items/{cartItemId}", 1L)
                        .with(user(testUser)))
                .andExpect(status().isNoContent());

    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("database/book/remove-books.sql"));
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("database/cartItem/remove-cart-items.sql"));
        }
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }
}
