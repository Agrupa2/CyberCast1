package es.swapsounds.configuration;

import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public final class ErrorMessages {

    private static final Map<Integer, String> ERROR_NAMES = new HashMap<>();
    private static final Map<Integer, String> ERROR_MESSAGES = new HashMap<>();

    static {
        // Error names (based on HttpStatus.getReasonPhrase())
        ERROR_NAMES.put(HttpStatus.BAD_REQUEST.value(), "Bad Request");
        ERROR_NAMES.put(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
        ERROR_NAMES.put(HttpStatus.FORBIDDEN.value(), "Forbidden");
        ERROR_NAMES.put(HttpStatus.NOT_FOUND.value(), "Not Found");
        ERROR_NAMES.put(HttpStatus.TOO_MANY_REQUESTS.value(), "Too Many Requests");
        ERROR_NAMES.put(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error");

        // Generic and friendly messages
        ERROR_MESSAGES.put(HttpStatus.BAD_REQUEST.value(), "Invalid request");
        ERROR_MESSAGES.put(HttpStatus.UNAUTHORIZED.value(), "Authentication required");
        ERROR_MESSAGES.put(HttpStatus.FORBIDDEN.value(), "Access denied");
        ERROR_MESSAGES.put(HttpStatus.NOT_FOUND.value(), "Resource not found");
        ERROR_MESSAGES.put(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests");
        ERROR_MESSAGES.put(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
    }

    public static String getErrorName(int statusCode) {
        return ERROR_NAMES.getOrDefault(statusCode, "Unknown Error");
    }

    public static String getErrorMessage(int statusCode) {
        return ERROR_MESSAGES.getOrDefault(statusCode, "An unexpected error occurred");
    }
}
