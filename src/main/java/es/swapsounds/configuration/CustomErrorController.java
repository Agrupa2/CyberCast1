package es.swapsounds.configuration;


import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomErrorController implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);

    @GetMapping("/error")
    public String handleError(Model model, HttpServletRequest request) {
        // Obtener detalles del error
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String errorMessage = (String) request.getAttribute("javax.servlet.error.message");
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");

        // Usar valores predeterminados si no hay código de estado
        int code = statusCode != null ? statusCode : HttpStatus.INTERNAL_SERVER_ERROR.value();
        String errorName = ErrorMessages.getErrorName(code);
        String friendlyMessage = ErrorMessages.getErrorMessage(code);

        // Si hay un mensaje de error o excepción, personalizar el mensaje (sin exponer detalles)
        if (errorMessage != null && !errorMessage.isEmpty()) {
            friendlyMessage = code == HttpStatus.NOT_FOUND.value() ? "Recurso no encontrado" : friendlyMessage;
        } else if (throwable != null) {
            friendlyMessage = code == HttpStatus.NOT_FOUND.value() ? "Recurso no encontrado" : "Error interno del servidor";
        }

        // Registrar detalles del error para depuración
        logger.error("Error occurred: status={}, uri={}, message={}, exception={}",
                code, requestUri, errorMessage, throwable != null ? throwable.getMessage() : "none");

        // Añadir atributos al modelo
        model.addAttribute("status", code);
        model.addAttribute("error", errorName);
        model.addAttribute("message", friendlyMessage);
        model.addAttribute("path", requestUri != null ? requestUri : "N/A");

        return "error"; // Renderiza error.html
    }
}