package mate.academy.onlinebookstore;

import java.math.BigDecimal;
import mate.academy.onlinebookstore.model.Book;
import mate.academy.onlinebookstore.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OnlineBookStoreApplication {
    @Autowired
    private BookService bookService;

    public static void main(String[] args) {
        SpringApplication.run(OnlineBookStoreApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner() {
        return args -> {
            Book book = new Book();
            book.setTitle("Frankenstein");
            book.setAuthor("Marry Shelley");
            book.setIsbn("9781912715322");
            book.setPrice(BigDecimal.valueOf(50));
            book.setDescriptions("It is a story of scientist creation a monster.");
            book.setCoverImage("url of image");

            bookService.save(book);
            System.out.println(bookService.findAll());
        };
    }
}
