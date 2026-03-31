package mate.academy.onlinebookstore.service.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import mate.academy.onlinebookstore.dto.book.BookDtoWithoutCategoryIds;
import mate.academy.onlinebookstore.dto.book.BookResponseDto;
import mate.academy.onlinebookstore.dto.book.BookSearchParametersDto;
import mate.academy.onlinebookstore.dto.book.CreateBookRequestDto;
import mate.academy.onlinebookstore.exception.EntityNotFoundException;
import mate.academy.onlinebookstore.mapper.BookMapper;
import mate.academy.onlinebookstore.model.Book;
import mate.academy.onlinebookstore.model.Category;
import mate.academy.onlinebookstore.repository.book.BookRepository;
import mate.academy.onlinebookstore.repository.book.BookSpecificationBuilder;
import mate.academy.onlinebookstore.service.book.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    @InjectMocks
    private BookServiceImpl bookService;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private BookSpecificationBuilder bookSpecificationBuilder;
    private Book greatGatsbyBook;
    private Book prideAndPrejudiceBook;
    private BookResponseDto greatGatsbyDto;
    private BookResponseDto prideAndPrejudiceDto;

    @BeforeEach
    void setUp() {
        Category classic = new Category()
                .setId(1L)
                .setName("Classic");
        Category fiction = new Category()
                .setId(2L)
                .setName("Fiction");
        greatGatsbyBook = new Book()
                .setId(1L)
                .setTitle("The great Gatsby")
                .setAuthor("F.Scott Fitzgerald")
                .setIsbn("9780743273565")
                .setPrice(BigDecimal.valueOf(12.99))
                .setDescription("A classic novel about the American dream and love.")
                .setCoverImage("https://example.com/images/great-gatsby.jpg")
                .setCategories(Set.of(classic, fiction));
        prideAndPrejudiceBook = new Book()
                .setId(5L)
                .setTitle("Pride and Prejudice")
                .setAuthor("Jane Austen")
                .setIsbn("9780141439518")
                .setPrice(BigDecimal.valueOf(8.50))
                .setDescription("A romantic novel of manners that follows the character "
                        + "development of Elizabeth Bennet.")
                .setCoverImage("https://example.com")
                .setCategories(Set.of(classic));
        greatGatsbyDto = new BookResponseDto()
                .setId(greatGatsbyBook.getId())
                .setTitle(greatGatsbyBook.getTitle())
                .setAuthor(greatGatsbyBook.getAuthor())
                .setIsbn(greatGatsbyBook.getIsbn())
                .setPrice(greatGatsbyBook.getPrice())
                .setDescription(greatGatsbyBook.getDescription())
                .setCoverImage(greatGatsbyBook.getCoverImage())
                .setCategoryIds(greatGatsbyBook.getCategories()
                        .stream()
                        .map(Category::getId)
                        .collect(Collectors.toSet()));
        prideAndPrejudiceDto = new BookResponseDto()
                .setId(prideAndPrejudiceBook.getId())
                .setTitle(prideAndPrejudiceBook.getTitle())
                .setAuthor(prideAndPrejudiceBook.getAuthor())
                .setIsbn(prideAndPrejudiceBook.getIsbn())
                .setPrice(prideAndPrejudiceBook.getPrice())
                .setDescription(prideAndPrejudiceBook.getDescription())
                .setCoverImage(prideAndPrejudiceBook.getCoverImage())
                .setCategoryIds(prideAndPrejudiceBook.getCategories()
                        .stream()
                        .map(Category::getId)
                        .collect(Collectors.toSet()));
    }

    @Test
    @DisplayName("Verify save() method works with valid request dto")
    void save_WithValidCreateBookRequestDto_ShouldReturnBookResponseDto() {
        CreateBookRequestDto requestDto = new CreateBookRequestDto()
                .setTitle("The great Gatsby")
                .setAuthor("F.Scott Fitzgerald")
                .setIsbn("9780743273565")
                .setPrice(BigDecimal.valueOf(12.99))
                .setDescription("A classic novel about the American dream and love.")
                .setCoverImage("https://example.com/images/great-gatsby.jpg")
                .setCategoryIds(Set.of(1L, 2L));

        Book savedBook = greatGatsbyBook;
        BookResponseDto expected = greatGatsbyDto;

        when(bookMapper.toModel(requestDto)).thenReturn(savedBook);
        when(bookRepository.save(savedBook)).thenReturn(savedBook);
        when(bookMapper.toDto(savedBook)).thenReturn(expected);

        BookResponseDto actual = bookService.save(requestDto);

        assertEquals(expected.getTitle(), actual.getTitle());
        assertEquals(2, actual.getCategoryIds().size());
        verify(bookMapper, times(1)).toModel(requestDto);
        verify(bookRepository, times(1)).save(savedBook);
        verify(bookMapper, times(1)).toDto(savedBook);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify that findAll() method works with valid Pageable")
    void findAll_WithValidPageable_ShouldReturnAllProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(List.of(greatGatsbyBook, prideAndPrejudiceBook),
                pageable, 20);

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(greatGatsbyBook)).thenReturn(greatGatsbyDto);
        when(bookMapper.toDto(prideAndPrejudiceBook)).thenReturn(prideAndPrejudiceDto);

        Page<BookResponseDto> actual = bookService.findAll(pageable);

        assertEquals(2, actual.getContent().size());
        assertEquals(20, actual.getTotalElements());
        assertEquals(actual.getContent().get(0), greatGatsbyDto);
        assertEquals(actual.getContent().get(1), prideAndPrejudiceDto);
        verify(bookRepository, times(1)).findAll(pageable);
        verify(bookMapper, times(1)).toDto(greatGatsbyBook);
        verify(bookMapper, times(1)).toDto(prideAndPrejudiceBook);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify that findById() method works with valid id")
    void findBookById_WithValidBookId_ShouldReturnBookDto() {
        Long bookId = 1L;
        Book fromDb = greatGatsbyBook;
        BookResponseDto expected = greatGatsbyDto;

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(fromDb));
        when(bookMapper.toDto(fromDb)).thenReturn(expected);

        BookResponseDto actual = bookService.findBookById(1L);

        assertEquals(expected, actual);
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookMapper, times(1)).toDto(fromDb);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify that an exception is throw when not existing book ID is passed")
    void findBookById_NonExistingBookId_ShouldThrowEntityNotFound() {
        Long bookId = 999L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> bookService.findBookById(bookId));

        assertEquals("Can't find book by id: " + bookId, exception.getMessage());
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Verify that an exception is thrown when negative book ID is passed")
    void findBookById_NegativeBookId_ShouldThrowEntityNotFound() {
        Long bookId = -2L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> bookService.findBookById(bookId));

        assertEquals("Can't find book by id: " + bookId, exception.getMessage());
        verify(bookMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Verify that correct dto is returned with valid book ID and request dto")
    void update_WithValidBookIdAndRequestDto_ShouldReturnUpdatedBookDto() {
        Long bookId = 1L;
        CreateBookRequestDto requestDto = new CreateBookRequestDto()
                .setTitle("Updated title")
                .setAuthor("Updated author")
                .setIsbn("1234567890000")
                .setPrice(BigDecimal.valueOf(12.99))
                .setDescription("Updated description")
                .setCoverImage("https://example.com/images/updated-book.jpg")
                .setCategoryIds(Set.of(1L));
        Book bookFromDb = greatGatsbyBook;
        Book updatedBook = bookFromDb
                .setTitle(requestDto.getTitle())
                .setAuthor(requestDto.getAuthor())
                .setIsbn(requestDto.getIsbn())
                .setPrice(requestDto.getPrice())
                .setDescription(requestDto.getDescription())
                .setCoverImage(requestDto.getCoverImage())
                .setCategories(requestDto.getCategoryIds().stream()
                        .map(Category::new)
                        .collect(Collectors.toSet()));

        BookResponseDto expected = new BookResponseDto()
                .setId(updatedBook.getId())
                .setTitle(updatedBook.getTitle())
                .setAuthor(updatedBook.getAuthor())
                .setIsbn(updatedBook.getIsbn())
                .setPrice(updatedBook.getPrice())
                .setDescription(updatedBook.getDescription())
                .setCoverImage(updatedBook.getCoverImage())
                .setCategoryIds(updatedBook.getCategories().stream()
                        .map(Category::getId)
                        .collect(Collectors.toSet()));

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(bookFromDb));
        when(bookRepository.save(bookFromDb)).thenReturn(updatedBook);
        when(bookMapper.toDto(bookFromDb)).thenReturn(expected);

        BookResponseDto actual = bookService.update(bookId, requestDto);

        assertEquals(expected, actual);
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookMapper, times(1))
                .updateBookFromDto(requestDto, bookFromDb);
        verify(bookRepository, times(1)).save(bookFromDb);
        verify(bookMapper, times(1)).toDto(bookFromDb);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify that exception is thrown if not existing book ID is passed")
    void update_WithNotExistingBookId_ShouldThrowException() {
        Long bookId = 999L;
        CreateBookRequestDto requestDto = new CreateBookRequestDto()
                .setTitle("Updated title")
                .setAuthor("Updated author")
                .setIsbn("1234567890000")
                .setPrice(BigDecimal.valueOf(12.99))
                .setDescription("Updated description")
                .setCoverImage("https://example.com/images/updated-book.jpg")
                .setCategoryIds(Set.of(1L));
        when(bookRepository.findById(bookId)).thenThrow(new EntityNotFoundException("Can't find "
                + "book by id: " + bookId));

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> bookService.update(bookId, requestDto));

        assertEquals("Can't find "
                + "book by id: " + bookId, exception.getMessage());
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify that method works with valid book ID")
    void deleteById_WithValidBookId_ShouldDeleteBook() {
        Long bookId = 1L;

        bookService.deleteById(bookId);

        verify(bookRepository, times(1)).deleteById(bookId);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Verify that method returns all appropriate books by special search params")
    void search_WithValidSearchParams_ShouldReturnAllAppropriateBooks() {
        BookSearchParametersDto searchParams = new BookSearchParametersDto(
                new String[]{"The Great Gatsby"},
                new String[]{"F. Scott Fitzgerald"}
        );

        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(List.of(greatGatsbyBook));
        Specification<Book> bookSpecification = mock(Specification.class);

        when(bookSpecificationBuilder.build(searchParams)).thenReturn(bookSpecification);
        when(bookRepository.findAll(bookSpecification, pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(greatGatsbyBook)).thenReturn(greatGatsbyDto);

        Page<BookResponseDto> actual = bookService.search(searchParams, pageable);

        assertEquals(1, actual.getContent().size());
        assertEquals(greatGatsbyDto, actual.getContent().get(0));
        verify(bookSpecificationBuilder).build(searchParams);
        verify(bookRepository).findAll(bookSpecification, pageable);
        verify(bookMapper).toDto(greatGatsbyBook);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Verify that method returns all books when search params are empty")
    void search_WithEmptyParams_ReturnsAllBooks() {
        BookSearchParametersDto emptyParams = new BookSearchParametersDto(
                new String[]{},
                new String[]{});
        Pageable pageable = PageRequest.of(0, 10);

        Specification<Book> emptySpec = Specification.allOf();
        Page<Book> allBooksPage = new PageImpl<>(List.of(greatGatsbyBook, prideAndPrejudiceBook));

        when(bookSpecificationBuilder.build(emptyParams)).thenReturn(emptySpec);
        when(bookRepository.findAll(emptySpec, pageable)).thenReturn(allBooksPage);
        when(bookMapper.toDto(any(Book.class))).thenReturn(new BookResponseDto());

        Page<BookResponseDto> actual = bookService.search(emptyParams, pageable);

        assertEquals(2, actual.getContent().size());
        verify(bookRepository).findAll(emptySpec, pageable);
    }

    @Test
    @DisplayName("Verify that all books with input category id is returned")
    void findAllByCategoryId_WithValidIdAndPageable_ShouldReturnAllAppropriateBooks() {
        Long categoryId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(List.of(greatGatsbyBook, prideAndPrejudiceBook));
        BookDtoWithoutCategoryIds greatGatsbyDtoWithout = new BookDtoWithoutCategoryIds()
                .setId(greatGatsbyBook.getId())
                .setTitle(greatGatsbyBook.getTitle())
                .setAuthor(greatGatsbyBook.getAuthor())
                .setIsbn(greatGatsbyBook.getIsbn())
                .setDescription(greatGatsbyBook.getDescription())
                .setCoverImage(greatGatsbyBook.getCoverImage());
        BookDtoWithoutCategoryIds prideDtoWithout = new BookDtoWithoutCategoryIds()
                .setId(prideAndPrejudiceBook.getId())
                .setTitle(prideAndPrejudiceBook.getTitle())
                .setAuthor(prideAndPrejudiceBook.getAuthor())
                .setIsbn(prideAndPrejudiceBook.getIsbn())
                .setDescription(prideAndPrejudiceBook.getDescription())
                .setCoverImage(prideAndPrejudiceBook.getCoverImage());

        when(bookRepository.findAllByCategoryId(categoryId, pageable)).thenReturn(bookPage);
        when(bookMapper.getBookDtoWithoutCategoryIds(prideAndPrejudiceBook))
                .thenReturn(prideDtoWithout);
        when(bookMapper.getBookDtoWithoutCategoryIds(greatGatsbyBook))
                .thenReturn(greatGatsbyDtoWithout);

        Page<BookDtoWithoutCategoryIds> actual = bookService.findAllByCategoryId(categoryId,
                pageable);

        assertEquals(2, actual.getContent().size());
        assertEquals(greatGatsbyDtoWithout, actual.getContent().get(0));
        verify(bookRepository, times(1))
                .findAllByCategoryId(categoryId, pageable);
        verify(bookMapper, atLeast(1)).getBookDtoWithoutCategoryIds(any());
    }

    @Test
    @DisplayName("Verify that empty page is returned when there is no book with input "
            + "category")
    void findAllByCategoryId_NoBooksFound_ShouldReturnEmptyPage() {
        Long categoryId = 3L;
        Pageable pageable = PageRequest.of(0, 10);

        when(bookRepository.findAllByCategoryId(categoryId, pageable)).thenReturn(Page.empty(pageable));

        Page<BookDtoWithoutCategoryIds> actual = bookService.findAllByCategoryId(categoryId,
                pageable);

        assertNotNull(actual);
        assertEquals(0, actual.getContent().size());
        verify(bookMapper, never()).getBookDtoWithoutCategoryIds(any());
    }
}
