package mate.academy.onlinebookstore.repository.book;

import java.util.Optional;
import mate.academy.onlinebookstore.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BookRepository extends JpaRepository<Book, Long>,
        JpaSpecificationExecutor<Book> {
    Book save(Book book);

    Page<Book> findAll(Pageable pageable);

    Page<Book> findAll(Specification<Book> spec, Pageable pageable);

    Optional<Book> findById(Long id);

    void deleteById(Long id);
}
