package mate.academy.onlinebookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.onlinebookstore.dto.BookResponseDto;
import mate.academy.onlinebookstore.dto.BookSearchParametersDto;
import mate.academy.onlinebookstore.dto.CreateBookRequestDto;
import mate.academy.onlinebookstore.service.book.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Book management", description = "Endpoints for managing books")
@RestController
@RequestMapping("books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping
    @Operation(summary = "Retrieve book catalog",
            description = "Returns a paginated list of books with optional sorting and filtering.")
    public Page<BookResponseDto> getAll(Pageable pageable) {
        return bookService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get books by id",
            description = "Returns detailed information about a book by its identifier.")
    public BookResponseDto getBookById(@PathVariable Long id) {
        return bookService.findBookById(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new book",
            description = "Create a new book. Only users with admin role can perform this "
                    + "operation.")
    public BookResponseDto createBook(@RequestBody @Valid CreateBookRequestDto bookDto) {

        return bookService.save(bookDto);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update specific book",
            description = "Update specific book by id. Only users with admin role can perform "
                    + "this operation.")
    public BookResponseDto update(@PathVariable Long id,
                                  @RequestBody @Valid CreateBookRequestDto requestDto) {
        return bookService.update(id, requestDto);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete book",
            description = "Delete book by identifier. Only users with admin role can perform this"
                    + " operation.")
    public void delete(@PathVariable Long id) {
        bookService.deleteById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Search books with parameter",
            description = "Searches for books based on provided criteria such as title and author."
                    + "All search parameters are optional and can be combined."
                    + "Returns a paginated list of matching books")
    public Page<BookResponseDto> search(BookSearchParametersDto searchParameters,
                                        Pageable pageable) {
        return bookService.search(searchParameters, pageable);
    }
}
