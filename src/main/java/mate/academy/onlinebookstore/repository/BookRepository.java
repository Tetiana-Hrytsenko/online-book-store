package mate.academy.onlinebookstore.repository;

import java.util.List;
import java.util.Optional;
import mate.academy.onlinebookstore.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
    Book save(Book book);

    List<Book> findAll();

    Optional<Book> findById(Long id);

    void deleteById(Long id);
}
