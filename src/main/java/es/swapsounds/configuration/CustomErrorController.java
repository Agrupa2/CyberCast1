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
        // Get error details
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String errorMessage = (String) request.getAttribute("javax.servlet.error.message");
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");

        // Use default values if there is no status code
        int code = statusCode != null ? statusCode : HttpStatus.INTERNAL_SERVER_ERROR.value();
        String errorName = ErrorMessages.getErrorName(code);
        String friendlyMessage = ErrorMessages.getErrorMessage(code);

        // If there is an error message or exception, customize the message (without
        // exposing details)
        if (errorMessage != null && !errorMessage.isEmpty()) {
            friendlyMessage = code == HttpStatus.NOT_FOUND.value() ? "Resource not found" : friendlyMessage;
        } else if (throwable != null) {
            friendlyMessage = code == HttpStatus.NOT_FOUND.value() ? "Resource not found" : "Internal server error";
        }

        // Log error details for debugging
        logger.error("Error occurred: status={}, uri={}, message={}, exception={}",
                code, requestUri, errorMessage, throwable != null ? throwable.getMessage() : "none");

        // Add attributes to the model
        model.addAttribute("status", code);
        model.addAttribute("error", errorName);
        model.addAttribute("message", friendlyMessage);
        model.addAttribute("path", requestUri != null ? requestUri : "N/A");

        return "error"; // Render error.html
    }
}