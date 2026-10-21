package br.com.unip.incendios.model;

import java.time.LocalDate;

public record FocoIncendio(
        String idBdq,
        String focoId,
        double latitude,
        double longitude,
        LocalDate data,
        String pais,
        String estado,
        String municipio,
        String bioma
) {}
