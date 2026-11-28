package com.sis414.sis_414_parcial.controller;

import com.sis414.sis_414_parcial.dto.EntidadDto;
import com.sis414.sis_414_parcial.service.EntidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entidades")
@Hidden // Keep auxiliary API available without repeating CRUD in Swagger.
public class EntidadController {

    private final EntidadService service;

    public EntidadController(EntidadService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Entidad - Listar")
    public List<EntidadDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Entidad - Obtener por id")
    public EntidadDto findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Entidad - Nuevo")
    public EntidadDto create(@RequestBody EntidadDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Entidad - Modificar")
    public EntidadDto update(@PathVariable Long id, @RequestBody EntidadDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Entidad - Eliminar")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
