package mate.academy.onlinebookstore.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import mate.academy.onlinebookstore.dto.book.BookResponseDto;
import mate.academy.onlinebookstore.dto.book.BookSearchParametersDto;
import mate.academy.onlinebookstore.dto.book.CreateBookRequestDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;
import tools.jackson.databind.ObjectMapper;

@Sql(
        scripts = {
                "classpath:database/category/insert-four-categories.sql",
                "classpath:database/book/insert-three-books.sql",
                "classpath:database/books-categories/insert-relations.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        scripts = {
                "classpath:database/books-categories/remove-relations.sql",
                "classpath:database/category/remove-categories.sql",
                "classpath:database/book/remove-books.sql"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired DataSource dataSource,
                          @Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
    }

    @WithMockUser(username = "user")
    @Test
    @DisplayName("Verify that method works with valid input data")
    void getAll_WithValidUserAndDefaultPagination_ShouldReturnAllBooks() throws Exception {
        BookResponseDto greatGatsby = new BookResponseDto()
                .setId(1L)
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setIsbn("9780743273565").setPrice(BigDecimal.valueOf(12.99))
                .setDescription("A classic novel about the American dream and love in the 1920s.")
                .setCategoryIds(Set.of(1L, 2L));
        BookResponseDto orwell = new BookResponseDto()
                .setId(2L)
                .setTitle("1984")
                .setAuthor("George Orwell")
                .setIsbn("9780451524935").setPrice(BigDecimal.valueOf(9.99))
                .setDescription("A masterpiece about government surveillance and totalitarianism.")
                .setCategoryIds(Set.of(1L));
        BookResponseDto atomicHabits = new BookResponseDto()
                .setId(2L)
                .setTitle("Atomic Habits")
                .setAuthor("James Clear")
                .setIsbn("9780735211292").setPrice(BigDecimal.valueOf(16.20))
                .setDescription("A proven framework for improving every day through tiny habits.")
                .setCategoryIds(Set.of(3L));

        Page<BookResponseDto> expected = new PageImpl<>(List.of(greatGatsby, orwell, atomicHabits));

        mockMvc.perform(get("/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(expected.getContent().size())))
                .andExpect(jsonPath("$.totalElements").value(expected.getTotalElements()))
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].title").value("The Great Gatsby"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].title").value("1984"))
                .andExpect(jsonPath("$.content[2].author").value("James Clear"))
                .andExpect(jsonPath("$.content[2].isbn").value("9780735211292"));
    }


    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that method works with pagination and sorting")
    void getAll_WithPaginationAndSorting_ShouldReturnPaginatedAndSortedBooks() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "author"));
        mockMvc.perform(get("/books")
                        .param("page", String.valueOf(pageable.getPageNumber()))
                        .param("size", String.valueOf(pageable.getPageSize()))
                        .param("sort", pageable.getSort().toString().replace(": ", ","))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.content[0].author").value("James Clear"));
    }

    @WithMockUser(username = "user")
    @Test
    @DisplayName("Verify that method works with valid id")
    void getBookById_WithValidId_Ok() throws Exception {
        Long id = 2L;
        BookResponseDto expected = new BookResponseDto()
                .setId(2L)
                .setTitle("1984")
                .setAuthor("George Orwell")
                .setIsbn("9780451524935").setPrice(BigDecimal.valueOf(9.99))
                .setDescription("A dystopian masterpiece about government surveillance and totalitarianism.")
                .setCategoryIds(Set.of(1L));

        MvcResult result = mockMvc.perform(get("/books/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookResponseDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                BookResponseDto.class);

        EqualsBuilder.reflectionEquals(expected, actual);
    }

    @WithMockUser(username = "user")
    @Test
    @DisplayName("Verify that NOT_FOUND status is returned with invalid id")
    void getBookById_WithInvalidId_ShouldReturnStatusNoFound() throws Exception {
        Long id = 5L;
        mockMvc.perform(get("/books/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that method works with valid input dto and role")
    void createBook_ValidRequestDto_Ok() throws Exception {
        CreateBookRequestDto requestDto = new CreateBookRequestDto()
                .setTitle("Pride and Prejudice")
                .setAuthor("Jane Austen")
                .setIsbn("9780141439518")
                .setPrice(BigDecimal.valueOf(8.50))
                .setDescription("A romantic novel of manners that follows the character "
                        + "development of Elizabeth Bennet.")
                .setCoverImage("https://example.com")
                .setCategoryIds(Set.of(1L));

        BookResponseDto expected = new BookResponseDto()
                .setId(4L)
                .setTitle(requestDto.getTitle())
                .setAuthor(requestDto.getAuthor())
                .setIsbn(requestDto.getIsbn())
                .setPrice(requestDto.getPrice())
                .setDescription(requestDto.getDescription())
                .setCoverImage(requestDto.getCoverImage())
                .setCategoryIds(requestDto.getCategoryIds());

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post("/books")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        BookResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), BookResponseDto.class);

        assertNotNull(actual);
        assertNotNull(actual.getId());
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @WithMockUser(username = "user")
    @Test
    @DisplayName("Verify that FORBIDDEN status is returned with invalid role")
    void createBook_WithUserRole_ShouldReturnForbiddenStatus() throws Exception {
        CreateBookRequestDto requestDto = new CreateBookRequestDto()
                .setTitle("Pride and Prejudice")
                .setAuthor("Jane Austen")
                .setIsbn("9780141439518")
                .setPrice(BigDecimal.valueOf(8.50))
                .setDescription("A romantic novel of manners that follows the character "
                        + "development of Elizabeth Bennet.")
                .setCoverImage("https://example.com")
                .setCategoryIds(Set.of(1L));

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/books")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    @DisplayName("Verify that BAD_REQUEST status is returned with invalid request")
    void createBook_WithInvalidRequestDto_ShouldReturnBadRequestStatus() throws Exception {
        CreateBookRequestDto requestDto = new CreateBookRequestDto()
                .setPrice(BigDecimal.valueOf(8.50))
                .setDescription("A romantic novel of manners that follows the character "
                        + "development of Elizabeth Bennet.")
                .setCoverImage("https://example.com")
                .setCategoryIds(Set.of(1L));

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/books")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    @DisplayName("Verify that BAD_REQUEST is returned with empty request body")
    void createBook_WithEmptyRequestBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/books")
                        .content("")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that BAD_REQUEST is returned with invalid isbn")
    void createBook_WithInvalidIsbn_ShouldReturnBadRequestStatus() throws Exception {
        CreateBookRequestDto requestDto = new CreateBookRequestDto()
                .setTitle("Pride and Prejudice")
                .setAuthor("Jane Austen")
                .setIsbn("9780141---")
                .setPrice(BigDecimal.valueOf(8.50))
                .setDescription("A romantic novel of manners that follows the character "
                        + "development of Elizabeth Bennet.")
                .setCoverImage("https://example.com")
                .setCategoryIds(Set.of(1L));
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/books")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors",
                        hasItem("isbn must be in ISBN-10 or ISBN-13 format.")));
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that method works with valid input dto and role")
    void update_WithValidRequestAndRole_Ok() throws Exception {
        Long id = 1L;
        CreateBookRequestDto update = new CreateBookRequestDto()
                .setTitle("Update title")
                .setAuthor("Update author")
                .setIsbn("9780141439518")
                .setPrice(BigDecimal.valueOf(10.50))
                .setDescription("Update description")
                .setCoverImage("https://example.com")
                .setCategoryIds(Set.of(1L, 2L));

        BookResponseDto expected = new BookResponseDto()
                .setId(1L)
                .setTitle(update.getTitle())
                .setAuthor(update.getAuthor())
                .setIsbn(update.getIsbn())
                .setPrice(update.getPrice())
                .setDescription(update.getDescription())
                .setCoverImage(update.getCoverImage())
                .setCategoryIds(update.getCategoryIds());

        String jsonRequest = objectMapper.writeValueAsString(update);

        MvcResult result = mockMvc.perform(put("/books/{id}", id)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        BookResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), BookResponseDto.class);

        EqualsBuilder.reflectionEquals(expected, actual);
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that BAD_REQUEST is returned with negative price")
    void update_WithNegativePrice_ShouldReturnBadRequestStatus() throws Exception {
        Long id = 1L;
        CreateBookRequestDto update = new CreateBookRequestDto()
                .setTitle("Update title")
                .setAuthor("Update author")
                .setIsbn("9780141439518")
                .setPrice(BigDecimal.valueOf(-10.50))
                .setDescription("Update description")
                .setCoverImage("https://example.com")
                .setCategoryIds(Set.of(1L, 2L));
        String jsonRequest = objectMapper.writeValueAsString(update);

        mockMvc.perform(put("/books/{id}", id)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(username = "user")
    @Test
    @DisplayName("Verify that FORBIDDEN status is returned with user role")
    void update_WithUserRole_ShouldReturnForbiddenStatus() throws Exception {
        Long id = 1L;
        CreateBookRequestDto update = new CreateBookRequestDto()
                .setTitle("Update title")
                .setAuthor("Update author")
                .setIsbn("9780141439518")
                .setPrice(BigDecimal.valueOf(10.50))
                .setDescription("Update description")
                .setCoverImage("https://example.com")
                .setCategoryIds(Set.of(1L, 2L));
        String jsonRequest = objectMapper.writeValueAsString(update);

        mockMvc.perform(put("/books/{id}", id)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that method works correctly with valid id and admin role")
    void delete_ValidId_Ok() throws Exception {
        Long id = 1L;
        mockMvc.perform(MockMvcRequestBuilders.delete("/books/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @WithMockUser(username = "user")
    @Test
    @DisplayName("Verify that method works correctly")
    void search_WithValidParameters_Ok() throws Exception {
        BookSearchParametersDto searchParams = new BookSearchParametersDto(
                new String[]{"The Great Gatsby", "Emma"},
                new String[]{"F. Scott Fitzgerald"}
        );

        mockMvc.perform(get("/books/search")
                        .param("title", searchParams.title())
                        .param("author", searchParams.author())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].title").value(searchParams.title()[0]))
                .andExpect(jsonPath("$.content[0].author").value(searchParams.author()[0]));
    }

    @Test
    @WithMockUser(username = "user")
    @DisplayName("Should return all books when search parameters are null")
    void search_WithNullParameters_ShouldReturnAllBooks() throws Exception {
        int booksInDb = 3;

        mockMvc.perform(get("/books/search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(booksInDb)))
                .andExpect(jsonPath("$.totalElements").value(booksInDb));
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("database/books-categories/remove-relations.sql"));
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("database/book/remove-books.sql"));
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("database/category/remove-categories.sql"));
        }
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }
}
