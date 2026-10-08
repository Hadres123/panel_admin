package com.ticketcita.admin.backend;

import com.ticketcita.admin.backend.Dtos.CitaDto;
import com.ticketcita.admin.backend.Dtos.DoctorDto;
import com.ticketcita.admin.backend.Dtos.EspecialidadDto;
import com.ticketcita.admin.backend.Dtos.HorarioDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Unica puerta hacia el backend del policlinico (API REST ya existente, protegida con Basic admin).
 * Las credenciales viven en variables de entorno del panel y nunca llegan al navegador.
 */
@Service
public class BackendClient {

    private final RestClient http;

    public BackendClient(@Value("${app.backend.url}") String url,
                         @Value("${app.backend.user}") String usuario,
                         @Value("${app.backend.password}") String clave) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(15_000);
        // El plan gratis de Render duerme el backend: el primer pedido puede tardar ~1 minuto.
        fabrica.setReadTimeout(90_000);
        String basic = Base64.getEncoder().encodeToString((usuario + ":" + clave).getBytes(StandardCharsets.UTF_8));
        this.http = RestClient.builder()
                .baseUrl(url.replaceAll("/+$", ""))
                .requestFactory(fabrica)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .build();
    }

    // ---------- lecturas ----------
    public List<EspecialidadDto> especialidades() {
        return http.get().uri("/api/especialidades").retrieve()
                .body(new ParameterizedTypeReference<List<EspecialidadDto>>() { });
    }

    public List<DoctorDto> doctores() {
        return http.get().uri("/api/doctores").retrieve()
                .body(new ParameterizedTypeReference<List<DoctorDto>>() { });
    }

    public List<HorarioDto> horarios() {
        return http.get().uri("/api/horarios").retrieve()
                .body(new ParameterizedTypeReference<List<HorarioDto>>() { });
    }

    public List<CitaDto> citas() {
        return http.get().uri("/api/citas").retrieve()
                .body(new ParameterizedTypeReference<List<CitaDto>>() { });
    }

    // ---------- especialidades ----------
    public void guardarEspecialidad(Long id, String nombre, BigDecimal precio, String descripcion) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("nombre", nombre);
        cuerpo.put("precio", precio);
        cuerpo.put("descripcion", descripcion);
        enviar(id, "/api/especialidades", cuerpo);
    }

    public void eliminarEspecialidad(Long id) {
        borrar("/api/especialidades/" + id);
    }

    // ---------- doctores ----------
    public void guardarDoctor(Long id, String nombre, String email, Long especialidadId, boolean activo) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("nombre", nombre);
        cuerpo.put("email", email);
        cuerpo.put("especialidad", Map.of("id", especialidadId));
        cuerpo.put("activo", activo);
        enviar(id, "/api/doctores", cuerpo);
    }

    public void eliminarDoctor(Long id) {
        borrar("/api/doctores/" + id);
    }

    // ---------- horarios ----------
    public void guardarHorario(Long id, Long doctorId, String dia, String inicio, String fin, boolean disponible) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("doctor", Map.of("id", doctorId));
        cuerpo.put("diaSemana", dia);
        cuerpo.put("horaInicio", inicio + ":00");
        cuerpo.put("horaFin", fin + ":00");
        cuerpo.put("disponible", disponible);
        enviar(id, "/api/horarios", cuerpo);
    }

    public void eliminarHorario(Long id) {
        borrar("/api/horarios/" + id);
    }

    // ---------- citas ----------
    public void cambiarEstadoCita(Long id, String estado) {
        http.put().uri("/api/citas/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("estado", estado))
                .retrieve().toBodilessEntity();
    }

    // ---------- ayudas ----------
    /** Si hay id actualiza (PUT), si no crea (POST). */
    private void enviar(Long id, String ruta, Map<String, Object> cuerpo) {
        if (id == null) {
            http.post().uri(ruta).contentType(MediaType.APPLICATION_JSON).body(cuerpo)
                    .retrieve().toBodilessEntity();
        } else {
            http.put().uri(ruta + "/" + id).contentType(MediaType.APPLICATION_JSON).body(cuerpo)
                    .retrieve().toBodilessEntity();
        }
    }

    private void borrar(String ruta) {
        http.delete().uri(ruta).retrieve().toBodilessEntity();
    }
}
