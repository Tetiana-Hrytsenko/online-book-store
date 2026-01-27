package mate.academy.onlinebookstore.repository.book;

import static mate.academy.onlinebookstore.repository.book.spec.AuthorSpecificationProvider.AUTHOR;
import static mate.academy.onlinebookstore.repository.book.spec.TitleSpecificationProvider.TITLE;

import lombok.RequiredArgsConstructor;
import mate.academy.onlinebookstore.dto.BookSearchParametersDto;
import mate.academy.onlinebookstore.model.Book;
import mate.academy.onlinebookstore.repository.SpecificationBuilder;
import mate.academy.onlinebookstore.repository.SpecificationProviderManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BookSpecificationBuilder implements SpecificationBuilder<Book> {
    private final SpecificationProviderManager<Book> bookSpecificationProviderManager;

    @Override
    public Specification<Book> build(BookSearchParametersDto searchParams) {
        Specification<Book> spec = Specification.allOf();
        if (searchParams != null && searchParams.author() != null
                && searchParams.author().length > 0) {
            spec = spec.and(bookSpecificationProviderManager.getSpecificationProvider(AUTHOR)
                    .getSpecification(searchParams.author()));
        }
        if (searchParams != null && searchParams.title() != null
                && searchParams.title().length > 0) {
            spec = spec.and(bookSpecificationProviderManager.getSpecificationProvider(TITLE)
                    .getSpecification(searchParams.title()));
        }
        return spec;
    }
}
