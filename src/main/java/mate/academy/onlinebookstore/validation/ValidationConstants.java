package mate.academy.onlinebookstore.validation;

public class ValidationConstants {
    public static final String ISBN_REGEX = "^(?:97[89][-\\d]{10,16}|\\d[-\\d]{8,12}[\\dX])$";
    public static final String INVALID_ISBN_FORMAT_MESSAGE = "must be in ISBN-10 or ISBN-13 format";
}
