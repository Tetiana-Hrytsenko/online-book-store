package mate.academy.onlinebookstore.controller;

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
import javax.sql.DataSource;
import lombok.SneakyThrows;
import mate.academy.onlinebookstore.dto.book.BookDtoWithoutCategoryIds;
import mate.academy.onlinebookstore.dto.category.CategoryDto;
import mate.academy.onlinebookstore.dto.category.CreateCategoryRequestDto;
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
        scripts = "classpath:database/category/insert-four-categories.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        scripts = "classpath:database/category/remove-categories.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CategoryControllerTest {
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

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    @DisplayName("Verify that method works with valid input request and role")
    void createCategory_WithValidRequestAndRole_Ok() throws Exception {
        CreateCategoryRequestDto requestDto = new CreateCategoryRequestDto("Romance",
                "A heart-touching love story.");
        CategoryDto expected = new CategoryDto(5L, "Romance",
                "A heart-touching love story.");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post("/categories")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        CategoryDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), CategoryDto.class);

        assertNotNull(actual);
        assertNotNull(actual.id());
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    @DisplayName("Verify that BAD_REQUEST is returned")
    void createCategory_WithEmptyRequestBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/categories")
                        .content("")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(username = "user")
    @Test
    @DisplayName("Verify that FORBIDDEN is returned with USER role")
    void createCategory_WithUserRole_ShouldReturnForbiddenStatus() throws Exception {
        CreateCategoryRequestDto requestDto = new CreateCategoryRequestDto("Romance",
                "A heart-touching love story");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/categories")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    @DisplayName("Verify that FORBIDDEN is returned with USER role")
    void createCategory_WithEmptyName_ShouldReturnBadRequestStatus() throws Exception {
        CreateCategoryRequestDto requestDto = new CreateCategoryRequestDto(null,
                "A heart-touching love story");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/categories")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(username = "user")
    @Test
    @DisplayName("Verify that method works correctly")
    void getAll() throws Exception {
        CategoryDto classic = new CategoryDto(1L, "Classic",
                "Famous books that remain popular for many generations.");
        CategoryDto fiction = new CategoryDto(2L, "Fiction",
                "Imaginary stories about people and events.");
        CategoryDto self_improvement = new CategoryDto(3L, "Self-improvement", null);
        CategoryDto dystopian = new CategoryDto(4L, "Dystopian",
                "Stories about societies with great injustice and suffering.");

        Page<CategoryDto> expected = new PageImpl<>(List.of(classic, fiction, self_improvement, dystopian));
        mockMvc.perform(get("/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(expected.getContent().size())))
                .andExpect(jsonPath("$.totalElements").value(expected.getTotalElements()))
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].name").value("Classic"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].name").value("Fiction"))
                .andExpect(jsonPath("$.content[3].description")
                        .value("Stories about societies with great injustice and suffering."));
    }

    @WithMockUser(username = "user")
    @Test
    @DisplayName("Verify that method works with pagination and sorting")
    void getAll_WithPagination_Ok() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name"));
        mockMvc.perform(get("/categories")
                        .param("page", String.valueOf(pageable.getPageNumber()))
                        .param("size", String.valueOf(pageable.getPageSize()))
                        .param("sort", pageable.getSort().toString().replace(": ", ","))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(4)))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.content[0].name").value("Self-improvement"));
    }

    @WithMockUser(username = "user")
    @Test
    @DisplayName("Verify that method works correctly with valid id")
    void getCategoryById_WithValidId_Ok() throws Exception {
        Long id = 2L;
        CategoryDto expected = new CategoryDto(2L, "Fiction",
                "Imaginary stories about people and events.");

        MvcResult result = mockMvc.perform(get("/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        CategoryDto actual = objectMapper.readValue(result.getResponse().getContentAsString(), CategoryDto.class);

        EqualsBuilder.reflectionEquals(expected, actual);
    }

    @WithMockUser(username = "user")
    @Test
    @DisplayName("Verify that NOT_FOUND is returned with not existing id")
    void getCategoryById_WithNotExistingId_ShouldReturnBedRequest() throws Exception {
        Long id = 999L;

        mockMvc.perform(get("/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    @DisplayName("Verify that method works with valid request dto and admin role")
    void updateCategory_WithValidRequestAndRole_Ok() throws Exception {
        Long id = 1L;
        CreateCategoryRequestDto requestDto = new CreateCategoryRequestDto("Updated name",
                "Update description");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        CategoryDto expected = new CategoryDto(1L, "Updated name",
                "Update description");

        MvcResult result = mockMvc.perform(put("/categories/{id}", id)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        CategoryDto actual = objectMapper.readValue(result.getResponse().getContentAsString(), CategoryDto.class);

        EqualsBuilder.reflectionEquals(expected, actual);
    }

    @WithMockUser(username = "user")
    @Test
    @DisplayName("Verify that FORBIDDEN is returned with user role")
    void updateCategory_WithUserRole_ShouldReturnForbiddenStatus() throws Exception {
        Long id = 1L;
        CreateCategoryRequestDto requestDto = new CreateCategoryRequestDto("Updated name",
                "Update description");
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(put("/categories/{id}", id)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "admin", roles = "ADMIN")
    @Test
    @DisplayName("Verify that BAD_REQUEST is returned with empty request")
    void updateCategory_WithEmptyRequest_ShouldReturnForbiddenStatus() throws Exception {
        Long id = 1L;

        mockMvc.perform(put("/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Verify that method works correctly with valid id and admin role")
    void deleteCategory() throws Exception {
        Long id = 1L;
        mockMvc.perform(MockMvcRequestBuilders.delete("/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

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
    @WithMockUser(username = "user")
    @Test
    @DisplayName("Verify that method returns book page with particular category id")
    void getBooksByCategoryId_WithValidId_Ok() throws Exception {
        Long id = 1L;
        BookDtoWithoutCategoryIds greatGatsby = new BookDtoWithoutCategoryIds()
                .setId(1L)
                .setTitle("The Great Gatsby")
                .setAuthor("F. Scott Fitzgerald")
                .setIsbn("9780743273565").setPrice(BigDecimal.valueOf(12.99))
                .setDescription("A classic novel about the American dream and love in the 1920s.");

        BookDtoWithoutCategoryIds orwell = new BookDtoWithoutCategoryIds()
                .setId(2L)
                .setTitle("1984")
                .setAuthor("George Orwell")
                .setIsbn("9780451524935").setPrice(BigDecimal.valueOf(9.99))
                .setDescription("A masterpiece about government surveillance and totalitarianism.");

        Page<BookDtoWithoutCategoryIds> expected = new PageImpl<>(List.of(greatGatsby, orwell));

        mockMvc.perform(get("/categories/{id}/books", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(expected.getContent().size())))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("The Great Gatsby"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].title").value("1984"));
    }

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
    @WithMockUser(username = "user")
    @Test
    @DisplayName("Verify that method returns an empty book page when no books are found with an existing category ID")
    void getBooksByCategoryId_NoBooksFoundWithExistingCategoryId_ShouldReturnEmptyBookPage() throws Exception {
        Long id = 4L;

        mockMvc.perform(get("/categories/{id}/books", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
        ;
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
