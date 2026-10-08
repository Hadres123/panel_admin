package com.ticketcita.admin.web;

import com.ticketcita.admin.backend.BackendClient;
import com.ticketcita.admin.backend.Dtos.DoctorDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class DoctorController {

    private final BackendClient backend;

    public DoctorController(BackendClient backend) {
        this.backend = backend;
    }

    @GetMapping("/doctores")
    public String lista(@RequestParam(required = false) Long editar,
                        @RequestParam(required = false) String nuevo,
                        Model model) {
        List<DoctorDto> doctores = backend.doctores();
        model.addAttribute("activo", "doctores");
        model.addAttribute("doctores", doctores);
        model.addAttribute("especialidades", backend.especialidades());

        DoctorDto form = null;
        if (editar != null) {
            form = doctores.stream().filter(d -> editar.equals(d.id)).findFirst().orElse(null);
        } else if (nuevo != null) {
            form = new DoctorDto();
            form.activo = true;
        }
        model.addAttribute("form", form);
        return "doctores";
    }

    @PostMapping("/doctores/guardar")
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam String nombre,
                          @RequestParam(required = false) String email,
                          @RequestParam Long especialidadId,
                          @RequestParam(defaultValue = "false") boolean activo,
                          RedirectAttributes ra) {
        if (nombre.isBlank()) {
            Acciones.error(ra, "El nombre del doctor es obligatorio.");
            return "redirect:/doctores";
        }
        String correo = (email == null || email.isBlank()) ? null : email.trim();
        Acciones.ejecutar(ra, () -> backend.guardarDoctor(id, nombre.trim(), correo, especialidadId, activo),
                "Doctor guardado.", "No se pudo guardar el doctor.");
        return "redirect:/doctores";
    }

    @PostMapping("/doctores/{id}/alternar")
    public String alternar(@PathVariable Long id, RedirectAttributes ra) {
        DoctorDto d = backend.doctores().stream().filter(x -> id.equals(x.id)).findFirst().orElse(null);
        if (d == null || d.especialidad == null) {
            Acciones.error(ra, "No se encontro el doctor.");
            return "redirect:/doctores";
        }
        Acciones.ejecutar(ra,
                () -> backend.guardarDoctor(d.id, d.nombre, d.email, d.especialidad.id, !d.activo),
                d.activo ? "Doctor desactivado." : "Doctor activado.", "No se pudo cambiar el estado.");
        return "redirect:/doctores";
    }

    @PostMapping("/doctores/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        // Con citas registradas no se puede borrar (se perderia el historial): se pide desactivar.
        boolean tieneCitas = backend.citas().stream().anyMatch(c -> c.doctor != null && id.equals(c.doctor.id));
        if (tieneCitas) {
            Acciones.error(ra, "Este doctor ya tiene citas registradas y no se puede eliminar. Use «Desactivar».");
            return "redirect:/doctores";
        }
        Acciones.ejecutar(ra, () -> {
            backend.horarios().stream()
                    .filter(h -> h.doctor != null && id.equals(h.doctor.id))
                    .forEach(h -> backend.eliminarHorario(h.id));
            backend.eliminarDoctor(id);
        }, "Doctor eliminado.", "No se pudo eliminar el doctor.");
        return "redirect:/doctores";
    }
}
