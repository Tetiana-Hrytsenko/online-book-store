package mate.academy.onlinebookstore.service.book;

import mate.academy.onlinebookstore.dto.BookResponseDto;
import mate.academy.onlinebookstore.dto.BookSearchParametersDto;
import mate.academy.onlinebookstore.dto.CreateBookRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
    BookResponseDto save(CreateBookRequestDto requestDto);

    Page<BookResponseDto> findAll(Pageable pageable);

    BookResponseDto findBookById(Long id);

    BookResponseDto update(Long id, CreateBookRequestDto requestDto);

    void deleteById(Long id);

    Page<BookResponseDto> search(BookSearchParametersDto searchParameters, Pageable pageable);
}
