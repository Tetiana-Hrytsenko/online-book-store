package mate.academy.onlinebookstore.service.book;

import mate.academy.onlinebookstore.dto.book.BookDtoWithoutCategoryIds;
import mate.academy.onlinebookstore.dto.book.BookResponseDto;
import mate.academy.onlinebookstore.dto.book.BookSearchParametersDto;
import mate.academy.onlinebookstore.dto.book.CreateBookRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
    BookResponseDto save(CreateBookRequestDto requestDto);

    Page<BookResponseDto> findAll(Pageable pageable);

    BookResponseDto findBookById(Long id);

    BookResponseDto update(Long id, CreateBookRequestDto requestDto);

    void deleteById(Long id);

    Page<BookResponseDto> search(BookSearchParametersDto searchParameters, Pageable pageable);

    Page<BookDtoWithoutCategoryIds> findAllByCategoryId(Long id, Pageable pageable);
}
