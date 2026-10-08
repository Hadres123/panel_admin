package com.ticketcita.admin.backend;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/** Formas de los datos que devuelve el backend. Campos publicos: Jackson y Thymeleaf los leen directo. */
public final class Dtos {

    private Dtos() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EspecialidadDto {
        public Long id;
        public String nombre;
        public BigDecimal precio;
        public String descripcion;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DoctorDto {
        public Long id;
        public String nombre;
        public String email;
        public EspecialidadDto especialidad;
        public boolean activo;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HorarioDto {
        public Long id;
        public DoctorDto doctor;
        public String diaSemana;
        public String horaInicio;
        public String horaFin;
        public boolean disponible;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CitaDto {
        public Long id;
        public String codigo;
        public String pacienteDni;
        public String pacienteNombre;
        public DoctorDto doctor;
        public EspecialidadDto especialidad;
        public String fecha;
        public String hora;
        public String metodoPago;
        public String yapeCelular;
        public String yapeCodigo;
        public String estado;
        public Long creadaEn;
    }
}
