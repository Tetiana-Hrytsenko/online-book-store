package mate.academy.onlinebookstore.dto.order;

public record OrderItemResponseDto(
        Long id,
        Long bookId,
        int quantity) {
}
