package mate.academy.onlinebookstore.dto.shoppingcart;

public record CartItemResponseDto(
        Long id,
        Long bookId,
        String title,
        int quantity) {
}
