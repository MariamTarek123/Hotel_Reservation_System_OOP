package exceptions;

/**
 * Custom exception thrown when a provided date range is invalid.
 * For example, this can be used when a check-out date is before or equal to a check-in date.
 */
public class InvalidDateRangeException extends Exception {
    public InvalidDateRangeException(String message) { super(message); }
}
