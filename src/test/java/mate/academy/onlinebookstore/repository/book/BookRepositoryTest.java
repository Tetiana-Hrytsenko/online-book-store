package mate.academy.onlinebookstore.repository.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mate.academy.onlinebookstore.model.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {
        "classpath:database/book/insert-three-books.sql",
        "classpath:database/category/insert-four-categories.sql",
        "classpath:database/books-categories/insert-relations.sql"
},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(scripts = {
        "classpath:database/books-categories/remove-relations.sql",
        "classpath:database/book/remove-books.sql",
        "classpath:database/category/remove-categories.sql"
},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class BookRepositoryTest {
    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("Verify that all book are found by category id")
    void findAllByCategoryId_ValidId_Ok() {
        Long categoryId = 1L;
        Pageable pageable = PageRequest.of(0, 5);

        Page<Book> actual = bookRepository.findAllByCategoryId(categoryId, pageable);

        assertEquals(2, actual.getContent().size());
        assertEquals("The Great Gatsby", actual.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("Verify that no books are found by existing category id")
    void findAllByCategoryId_NoBookFound_ShouldReturnEmptyPage() {
        Long categoryId = 4L;
        Pageable pageable = PageRequest.of(0, 5);

        Page<Book> actual = bookRepository.findAllByCategoryId(categoryId, pageable);

        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("Verify that empty page is returned with not existing category id")
    void findAllByCategoryId_WithNotExistingId_ShouldReturnEmptyPage() {
        Long categoryId = -999L;
        Pageable pageable = PageRequest.of(0, 5);

        Page<Book> actual = bookRepository.findAllByCategoryId(categoryId, pageable);

        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }
}
