package es.swapsounds.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class CustomErrorController {

    @ExceptionHandler(ResponseStatusException.class)
    public ModelAndView handleResponseStatusException(ResponseStatusException ex) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("status", ex.getStatusCode().value());
        mav.addObject("error", ex.getReason());
        mav.addObject("message", ex.getMessage());
        return mav;
    }
}