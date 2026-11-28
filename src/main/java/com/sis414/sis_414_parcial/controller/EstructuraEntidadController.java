package com.sis414.sis_414_parcial.controller;

import com.sis414.sis_414_parcial.dto.EstructuraEntidadDto;
import com.sis414.sis_414_parcial.service.EstructuraEntidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estructuras-entidad")
@Hidden // Keep auxiliary API available without repeating CRUD in Swagger.
public class EstructuraEntidadController {

    private final EstructuraEntidadService service;

    public EstructuraEntidadController(EstructuraEntidadService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "EstructuraEntidad - Listar")
    public List<EstructuraEntidadDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "EstructuraEntidad - Obtener por id")
    public EstructuraEntidadDto findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "EstructuraEntidad - Nuevo")
    public EstructuraEntidadDto create(@RequestBody EstructuraEntidadDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "EstructuraEntidad - Modificar")
    public EstructuraEntidadDto update(@PathVariable Long id, @RequestBody EstructuraEntidadDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "EstructuraEntidad - Eliminar")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
