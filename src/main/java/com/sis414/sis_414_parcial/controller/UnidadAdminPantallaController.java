package com.sis414.sis_414_parcial.controller;

import com.sis414.sis_414_parcial.dto.UnidadSeleccionadaDto;
import com.sis414.sis_414_parcial.service.UnidadAdminPantallaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/unidades-admin")
@Hidden // Preserve frontend selection endpoints without listing them in Swagger.
public class UnidadAdminPantallaController {

    private final UnidadAdminPantallaService service;

    public UnidadAdminPantallaController(UnidadAdminPantallaService service) {
        this.service = service;
    }

    @PostMapping("/{id}/seleccionar")
    @Operation(summary = "Seleccionar (unidad de trabajo)")
    public UnidadSeleccionadaDto seleccionar(@PathVariable Long id) {
        return service.seleccionar(id);
    }

    @GetMapping("/seleccionada")
    @Operation(summary = "Unidad seleccionada actualmente")
    public UnidadSeleccionadaDto seleccionada() {
        return service.seleccionada();
    }
}
