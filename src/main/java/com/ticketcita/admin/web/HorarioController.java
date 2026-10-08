package com.ticketcita.admin.web;

import com.ticketcita.admin.backend.BackendClient;
import com.ticketcita.admin.backend.Dtos.DoctorDto;
import com.ticketcita.admin.backend.Dtos.HorarioDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HorarioController {

    /** Valores que espera el backend -> texto para mostrar. El orden es el de la semana. */
    private static final Map<String, String> DIAS = new LinkedHashMap<>();

    static {
        DIAS.put("LUNES", "Lunes");
        DIAS.put("MARTES", "Martes");
        DIAS.put("MIERCOLES", "Miércoles");
        DIAS.put("JUEVES", "Jueves");
        DIAS.put("VIERNES", "Viernes");
        DIAS.put("SABADO", "Sábado");
        DIAS.put("DOMINGO", "Domingo");
    }

    private final BackendClient backend;

    public HorarioController(BackendClient backend) {
        this.backend = backend;
    }

    @GetMapping("/horarios")
    public String lista(@RequestParam(required = false) Long doctorId,
                        @RequestParam(required = false) Long editar,
                        @RequestParam(required = false) String nuevo,
                        Model model) {
        List<DoctorDto> doctores = backend.doctores();
        Long seleccionado = doctorId;
        if (seleccionado == null && !doctores.isEmpty()) {
            seleccionado = doctores.get(0).id;
        }
        final Long idDoctor = seleccionado;

        List<String> orden = List.copyOf(DIAS.keySet());
        List<HorarioDto> horarios = backend.horarios().stream()
                .filter(h -> h.doctor != null && h.doctor.id != null && h.doctor.id.equals(idDoctor))
                .sorted(Comparator.comparingInt((HorarioDto h) -> orden.indexOf(h.diaSemana))
                        .thenComparing(h -> h.horaInicio == null ? "" : h.horaInicio))
                .toList();

        HorarioDto form = null;
        if (editar != null) {
            form = horarios.stream().filter(h -> editar.equals(h.id)).findFirst().orElse(null);
        } else if (nuevo != null) {
            form = new HorarioDto();
            form.disponible = true;
            form.diaSemana = "LUNES";
            form.horaInicio = "08:00:00";
            form.horaFin = "13:00:00";
        }

        model.addAttribute("activo", "horarios");
        model.addAttribute("doctores", doctores);
        model.addAttribute("doctorId", idDoctor);
        model.addAttribute("horarios", horarios);
        model.addAttribute("dias", DIAS);
        model.addAttribute("form", form);
        return "horarios";
    }

    @PostMapping("/horarios/guardar")
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam Long doctorId,
                          @RequestParam String dia,
                          @RequestParam String inicio,
                          @RequestParam String fin,
                          @RequestParam(defaultValue = "false") boolean disponible,
                          RedirectAttributes ra) {
        String volver = "redirect:/horarios?doctorId=" + doctorId;
        if (!DIAS.containsKey(dia) || inicio.length() != 5 || fin.length() != 5) {
            Acciones.error(ra, "Dia u hora no validos.");
            return volver;
        }
        if (fin.compareTo(inicio) <= 0) {
            Acciones.error(ra, "La hora final debe ser mayor que la inicial.");
            return volver;
        }
        Acciones.ejecutar(ra, () -> backend.guardarHorario(id, doctorId, dia, inicio, fin, disponible),
                "Horario guardado.", "No se pudo guardar el horario.");
        return volver;
    }

    @PostMapping("/horarios/{id}/eliminar")
    public String eliminar(@PathVariable Long id, @RequestParam Long doctorId, RedirectAttributes ra) {
        Acciones.ejecutar(ra, () -> backend.eliminarHorario(id), "Horario eliminado.",
                "No se pudo eliminar el horario.");
        return "redirect:/horarios?doctorId=" + doctorId;
    }
}
