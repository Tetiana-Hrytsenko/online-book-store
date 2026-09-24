package mate.academy.onlinebookstore.service.category;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import mate.academy.onlinebookstore.dto.book.BookDtoWithoutCategoryIds;
import mate.academy.onlinebookstore.dto.category.CategoryDto;
import mate.academy.onlinebookstore.dto.category.CreateCategoryRequestDto;
import mate.academy.onlinebookstore.exception.EntityNotFoundException;
import mate.academy.onlinebookstore.mapper.CategoryMapper;
import mate.academy.onlinebookstore.model.Category;
import mate.academy.onlinebookstore.repository.category.CategoryRepository;
import mate.academy.onlinebookstore.service.book.BookService;
import mate.academy.onlinebookstore.service.category.impl.CategoryServiceImpl;
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

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @InjectMocks
    private CategoryServiceImpl categoryService;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private BookService bookService;
    private Category classic;
    private Category fiction;
    private CategoryDto classicDto;
    private CategoryDto fictionDto;

    @BeforeEach
    void setUp() {
        classic = new Category()
                .setId(1L)
                .setName("Classic")
                .setDescription("Famous books that remain popular and important for many "
                        + "generations.");
        fiction = new Category()
                .setId(2L)
                .setName("Fiction")
                .setDescription("Imaginary stories about people and events created by the "
                        + "author's mind.");
        classicDto = new CategoryDto(classic.getId(), classic.getName(), classic.getDescription());
        fictionDto = new CategoryDto(fiction.getId(), fiction.getName(), fiction.getDescription());
    }

    @Test
    @DisplayName("Verify that method works with correct pagination")
    void findAll_WithCorrectPagination_ShouldReturnCategoryPage() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Category> categories = List.of(classic, fiction);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, 20);

        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(categoryMapper.toDto(classic)).thenReturn(classicDto);
        when(categoryMapper.toDto(fiction)).thenReturn(fictionDto);

        Page<CategoryDto> actual = categoryService.findAll(pageable);

        assertEquals(2, actual.getContent().size());
        assertEquals(20, actual.getTotalElements());
        assertEquals(actual.getContent().get(0), classicDto);
        assertEquals(actual.getContent().get(1), fictionDto);
        verify(categoryRepository, times(1)).findAll(pageable);
        verify(categoryMapper, times(1)).toDto(classic);
        verify(categoryMapper, times(1)).toDto(fiction);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);

    }

    @Test
    @DisplayName("Verify that method works correct with valid id")
    void getById_WithExistingId_ShouldReturnCategory() {
        Long id = 1L;
        Category categoryFromDb = classic;

        when(categoryRepository.findById(id)).thenReturn(Optional.of(categoryFromDb));
        when(categoryMapper.toDto(classic)).thenReturn(classicDto);

        CategoryDto actual = categoryService.getById(id);

        assertEquals(categoryFromDb.getName(), actual.name());
        verify(categoryRepository, times(1)).findById(id);
        verify(categoryMapper, times(1)).toDto(categoryFromDb);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Verify that exception is thrown with invalid id")
    void getById_WithInvalidId_ShouldThrowException() {
        Long id = -10L;

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> categoryService.getById(id));

        assertEquals("Can't find category by id: " + id, exception.getMessage());
        verify(categoryRepository).findById(id);
        verify(categoryMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Verify that method works correctly with valid input request dto")
    void save_WithValidInputRequestDto_Ok() {
        CreateCategoryRequestDto requestDto = new CreateCategoryRequestDto(
                "Classic",
                "Famous books that remain popular and important for many generations.");

        when(categoryMapper.toModel(requestDto)).thenReturn(classic);
        when(categoryRepository.save(classic)).thenReturn(classic);
        when(categoryMapper.toDto(classic)).thenReturn(classicDto);

        CategoryDto actual = categoryService.save(requestDto);

        assertEquals(classicDto.name(), actual.name());
        verify(categoryMapper, times(1)).toModel(requestDto);
        verify(categoryRepository, times(1)).save(classic);
        verify(categoryMapper, times(1)).toDto(classic);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Verify that method works correctly with valid input id and request dto")
    void update_WithValidIdAndRequestDto_Ok() {
        // public CategoryDto update(Long id, CreateCategoryRequestDto requestDto) {
        //        Category category = categoryRepository.findById(id).orElseThrow(
        //                () -> new EntityNotFoundException("Can't find category by id: " + id));
        //        categoryMapper.updateCategoryFromDto(requestDto, category);
        //        return categoryMapper.toDto(categoryRepository.save(category));
        //    }
        Long id = 1L;
        CreateCategoryRequestDto requestDto = new CreateCategoryRequestDto(
                "Update name",
                "Update description");
        when(categoryRepository.findById(id)).thenReturn(Optional.of(classic));
        when(categoryRepository.save(classic)).thenReturn(classic);
        when(categoryMapper.toDto(classic)).thenReturn(classicDto);

        CategoryDto actual = categoryService.update(id, requestDto);

        assertEquals(classicDto.name(), actual.name());
        verify(categoryMapper, times(1)).updateCategoryFromDto(requestDto, classic);
        verify(categoryRepository, times(1)).save(classic);
        verify(categoryMapper, times(1)).toDto(classic);
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("Verify that exception is thrown with invalid id")
    void update_WithInvalidId_ShouldThrowException() {
        Long id = -10L;
        CreateCategoryRequestDto requestDto = new CreateCategoryRequestDto(
                "Update name",
                "Update description");

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> categoryService.update(id, requestDto));

        assertEquals("Can't find category by id: " + id, exception.getMessage());
        verify(categoryRepository).findById(id);
        verify(categoryMapper, never()).toDto(any());
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("Verify that method works with valid book ID")
    void deleteById_WithValidBookId_Ok() {
        Long bookId = 1L;

        categoryService.deleteById(bookId);

        verify(categoryRepository, times(1)).deleteById(bookId);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void findAllByCategoryId() {
        Long id = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        BookDtoWithoutCategoryIds bookDto = new BookDtoWithoutCategoryIds();
        Page<BookDtoWithoutCategoryIds> bookDtoPage = new PageImpl<>(List.of(bookDto));

        when(bookService.findAllByCategoryId(id, pageable)).thenReturn(bookDtoPage);

        Page<BookDtoWithoutCategoryIds> actual = categoryService.findAllByCategoryId(id, pageable);
        assertEquals(1, actual.getContent().size());
        verify(bookService, times(1)).findAllByCategoryId(id, pageable);
        verifyNoMoreInteractions(categoryRepository);
    }
}
