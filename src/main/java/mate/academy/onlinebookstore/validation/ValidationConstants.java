package mate.academy.onlinebookstore.validation;

public class ValidationConstants {
    public static final String ISBN_REGEX = "^(?:97[89][-\\d]{10,16}|\\d[-\\d]{8,12}[\\dX])$";
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$";
    public static final String SHIPPING_ADDRESS_REGEX = "^[a-zA-Z0-9\\s.,/\\-]{10,150}$";
}
