package mate.academy.onlinebookstore.repository.book;

import java.util.Optional;
import mate.academy.onlinebookstore.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long>,
        JpaSpecificationExecutor<Book> {
    Book save(Book book);

    @EntityGraph(attributePaths = {"categories"})
    Page<Book> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"categories"})
    Page<Book> findAll(Specification<Book> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"categories"})
    Optional<Book> findById(Long id);

    void deleteById(Long id);

    @EntityGraph(attributePaths = {"categories"})
    @Query(value = "SELECT b FROM Book b JOIN b.categories c WHERE c.id =:id")
    Page<Book> findAllByCategoryId(@Param("id")Long id, Pageable pageable);
}
