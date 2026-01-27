package mate.academy.onlinebookstore.repository.book;

import java.util.List;
import java.util.Optional;
import mate.academy.onlinebookstore.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    Book save(Book book);

    List<Book> findAll();

    Optional<Book> findById(Long id);

    void deleteById(Long id);
}
