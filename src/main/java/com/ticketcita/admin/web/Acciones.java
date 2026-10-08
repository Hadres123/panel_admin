package com.ticketcita.admin.web;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Ejecuta una accion contra el backend y deja un aviso (ok / error) para la siguiente pantalla. */
final class Acciones {

    private Acciones() {
    }

    static void ejecutar(RedirectAttributes ra, Runnable accion, String mensajeOk, String errorSiFalla) {
        try {
            accion.run();
            ra.addFlashAttribute("ok", mensajeOk);
        } catch (RestClientResponseException e) {
            int estado = e.getStatusCode().value();
            String msg;
            if (estado == 401 || estado == 403) {
                msg = "El panel no pudo autenticarse con el backend: revise BACKEND_USER y BACKEND_PASSWORD.";
            } else if (estado == 400) {
                msg = "El backend rechazo los datos. Revise los campos e intente de nuevo.";
            } else {
                msg = errorSiFalla;
            }
            ra.addFlashAttribute("error", msg);
        } catch (RestClientException e) {
            ra.addFlashAttribute("error",
                    "No se pudo conectar con el backend. Si estaba dormido, espere 1 minuto e intente de nuevo.");
        }
    }

    static void error(RedirectAttributes ra, String mensaje) {
        ra.addFlashAttribute("error", mensaje);
    }
}
