package com.ticketcita.admin.web;

import com.ticketcita.admin.backend.BackendClient;
import com.ticketcita.admin.backend.Dtos.CitaDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class CitaController {

    private static final Set<String> ESTADOS_PERMITIDOS = Set.of("pagada", "atendida", "cancelada");

    private static final Map<String, String> ESTADOS = Map.of(
            "pendiente_pago", "Pago pendiente",
            "pagada", "Pagada",
            "atendida", "Atendida",
            "cancelada", "Cancelada",
            "liberada", "Cupo liberado");

    private final BackendClient backend;

    public CitaController(BackendClient backend) {
        this.backend = backend;
    }

    @GetMapping("/citas")
    public String lista(@RequestParam(defaultValue = "pendiente_pago") String estado, Model model) {
        List<CitaDto> citas = backend.citas().stream()
                .filter(c -> "todas".equals(estado) || estado.equals(c.estado))
                .sorted(Comparator.comparing((CitaDto c) -> c.creadaEn == null ? 0L : c.creadaEn).reversed())
                .toList();
        model.addAttribute("activo", "citas");
        model.addAttribute("citas", citas);
        model.addAttribute("filtro", estado);
        model.addAttribute("nombresEstado", ESTADOS);
        return "citas";
    }

    @PostMapping("/citas/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam String estado,
                                @RequestParam(defaultValue = "pendiente_pago") String filtro,
                                RedirectAttributes ra) {
        if (!ESTADOS_PERMITIDOS.contains(estado)) {
            Acciones.error(ra, "Estado no permitido.");
        } else {
            Acciones.ejecutar(ra, () -> backend.cambiarEstadoCita(id, estado), "Cita actualizada.",
                    "No se pudo actualizar la cita.");
        }
        return "redirect:/citas?estado=" + filtro;
    }
}
