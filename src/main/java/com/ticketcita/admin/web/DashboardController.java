package com.ticketcita.admin.web;

import com.ticketcita.admin.backend.BackendClient;
import com.ticketcita.admin.backend.Dtos.CitaDto;
import com.ticketcita.admin.backend.Dtos.DoctorDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

@Controller
public class DashboardController {

    private final BackendClient backend;

    public DashboardController(BackendClient backend) {
        this.backend = backend;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        List<DoctorDto> doctores = backend.doctores();
        List<CitaDto> citas = backend.citas();
        String hoy = LocalDate.now(ZoneId.of("America/Lima")).toString();

        List<CitaDto> pendientes = citas.stream()
                .filter(c -> "pendiente_pago".equals(c.estado))
                .sorted(Comparator.comparing((CitaDto c) -> c.creadaEn == null ? 0L : c.creadaEn).reversed())
                .toList();
        long citasHoy = citas.stream()
                .filter(c -> hoy.equals(c.fecha) && ("pagada".equals(c.estado) || "pendiente_pago".equals(c.estado)))
                .count();

        model.addAttribute("activo", "inicio");
        model.addAttribute("doctoresActivos", doctores.stream().filter(d -> d.activo).count());
        model.addAttribute("totalEspecialidades", backend.especialidades().size());
        model.addAttribute("citasHoy", citasHoy);
        model.addAttribute("totalPendientes", pendientes.size());
        model.addAttribute("pendientes", pendientes.stream().limit(5).toList());
        return "dashboard";
    }
}
