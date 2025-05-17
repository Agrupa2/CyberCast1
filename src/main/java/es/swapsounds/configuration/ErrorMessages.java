package es.swapsounds.configuration;

import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public final class ErrorMessages {

    private static final Map<Integer, String> ERROR_NAMES = new HashMap<>();
    private static final Map<Integer, String> ERROR_MESSAGES = new HashMap<>();

    static {
        // Nombres de errores (basados en HttpStatus.getReasonPhrase())
        ERROR_NAMES.put(HttpStatus.BAD_REQUEST.value(), "Bad Request");
        ERROR_NAMES.put(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
        ERROR_NAMES.put(HttpStatus.FORBIDDEN.value(), "Forbidden");
        ERROR_NAMES.put(HttpStatus.NOT_FOUND.value(), "Not Found");
        ERROR_NAMES.put(HttpStatus.TOO_MANY_REQUESTS.value(), "Too Many Requests");
        ERROR_NAMES.put(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error");

        // Mensajes genéricos y amigables
        ERROR_MESSAGES.put(HttpStatus.BAD_REQUEST.value(), "Solicitud inválida");
        ERROR_MESSAGES.put(HttpStatus.UNAUTHORIZED.value(), "Autenticación requerida");
        ERROR_MESSAGES.put(HttpStatus.FORBIDDEN.value(), "Acceso denegado");
        ERROR_MESSAGES.put(HttpStatus.NOT_FOUND.value(), "Recurso no encontrado");
        ERROR_MESSAGES.put(HttpStatus.TOO_MANY_REQUESTS.value(), "Demasiadas solicitudes");
        ERROR_MESSAGES.put(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error interno del servidor");
    }

    public static String getErrorName(int statusCode) {
        return ERROR_NAMES.getOrDefault(statusCode, "Unknown Error");
    }

    public static String getErrorMessage(int statusCode) {
        return ERROR_MESSAGES.getOrDefault(statusCode, "Ocurrió un error inesperado");
    }
} 
    

