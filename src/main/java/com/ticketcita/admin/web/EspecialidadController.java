package com.ticketcita.admin.web;

import com.ticketcita.admin.backend.BackendClient;
import com.ticketcita.admin.backend.Dtos.EspecialidadDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class EspecialidadController {

    private final BackendClient backend;

    public EspecialidadController(BackendClient backend) {
        this.backend = backend;
    }

    @GetMapping("/especialidades")
    public String lista(@RequestParam(required = false) Long editar,
                        @RequestParam(required = false) String nuevo,
                        Model model) {
        List<EspecialidadDto> lista = backend.especialidades();
        EspecialidadDto form = null;
        if (editar != null) {
            form = lista.stream().filter(e -> editar.equals(e.id)).findFirst().orElse(null);
        } else if (nuevo != null) {
            form = new EspecialidadDto();
        }
        model.addAttribute("activo", "especialidades");
        model.addAttribute("especialidades", lista);
        model.addAttribute("form", form);
        return "especialidades";
    }

    @PostMapping("/especialidades/guardar")
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam String nombre,
                          @RequestParam BigDecimal precio,
                          @RequestParam(required = false) String descripcion,
                          RedirectAttributes ra) {
        if (nombre.isBlank() || precio.signum() <= 0) {
            Acciones.error(ra, "Indique un nombre y un precio mayor que cero.");
            return "redirect:/especialidades";
        }
        String desc = (descripcion == null || descripcion.isBlank()) ? null : descripcion.trim();
        Acciones.ejecutar(ra, () -> backend.guardarEspecialidad(id, nombre.trim(), precio, desc),
                "Especialidad guardada.", "No se pudo guardar la especialidad.");
        return "redirect:/especialidades";
    }

    @PostMapping("/especialidades/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        boolean enUso = backend.doctores().stream()
                .anyMatch(d -> d.especialidad != null && id.equals(d.especialidad.id));
        if (enUso) {
            Acciones.error(ra, "Hay doctores con esta especialidad: reasígnelos o elimínelos primero.");
            return "redirect:/especialidades";
        }
        Acciones.ejecutar(ra, () -> backend.eliminarEspecialidad(id), "Especialidad eliminada.",
                "No se pudo eliminar la especialidad.");
        return "redirect:/especialidades";
    }
}
