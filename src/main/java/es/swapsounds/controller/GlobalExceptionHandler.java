package es.swapsounds.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import es.swapsounds.configuration.ErrorMessages;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Método auxiliar para crear ModelAndView
    private ModelAndView createErrorView(int statusCode, String message, Exception ex) {
        logger.error("Handling exception: type={}, message={}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("status", statusCode);
        mav.addObject("error", ErrorMessages.getErrorName(statusCode));
        mav.addObject("message", message);
        return mav;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgumentException(IllegalArgumentException ex, Model model) {
        return createErrorView(HttpStatus.NOT_FOUND.value(), "Recurso no encontrado", ex);
    }

    @ExceptionHandler(NullPointerException.class)
    public ModelAndView handleNullPointerException(NullPointerException ex, Model model) {
        return createErrorView(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error interno del servidor", ex);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDeniedException(AccessDeniedException ex, Model model) {
        return createErrorView(HttpStatus.FORBIDDEN.value(), "Acceso denegado", ex);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ModelAndView handleValidationException(Exception ex, Model model) {
        return createErrorView(HttpStatus.BAD_REQUEST.value(), "Solicitud inválida", ex);
    }

    @ExceptionHandler(DataAccessException.class)
    public ModelAndView handleDataAccessException(DataAccessException ex, Model model) {
        return createErrorView(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error al acceder a la base de datos", ex);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenericException(Exception ex, Model model) {
        return createErrorView(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error inesperado", ex);
    }
}