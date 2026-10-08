package com.ticketcita.admin.web;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestClientException;

/** Si el backend no responde al abrir una pantalla, se muestra una pagina clara en vez de un error feo. */
@ControllerAdvice
public class ErroresGlobales {

    @ExceptionHandler(RestClientException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public String backendCaido(Model model) {
        model.addAttribute("activo", "");
        return "error-backend";
    }
}
