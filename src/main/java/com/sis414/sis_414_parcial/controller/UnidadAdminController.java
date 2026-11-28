package com.sis414.sis_414_parcial.controller;

import com.sis414.sis_414_parcial.dto.UnidadAdminDto;
import com.sis414.sis_414_parcial.service.UnidadAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unidades-admin")
@Tag(name = "Administrar Unidad")
public class UnidadAdminController {

    private final UnidadAdminService service;

    public UnidadAdminController(UnidadAdminService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "UnidadAdmin - Listar")
    public List<UnidadAdminDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "UnidadAdmin - Obtener por id")
    public UnidadAdminDto findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "UnidadAdmin - Nuevo")
    public UnidadAdminDto create(@RequestBody UnidadAdminRequest dto) {
        return service.create(dto.toDto());
    }

    @PutMapping("/{id}")
    @Operation(summary = "UnidadAdmin - Modificar")
    public UnidadAdminDto update(@PathVariable Long id, @RequestBody UnidadAdminRequest dto) {
        return service.update(id, dto.toDto());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "UnidadAdmin - Eliminar")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // The database assigns the ID; clients do not provide it in the request body.
    public record UnidadAdminRequest(
            String unidad,
            String descrip,
            String ciudad,
            Integer estadoUni,
            Long entidadId
    ) {
        UnidadAdminDto toDto() {
            return new UnidadAdminDto(null, unidad, descrip, ciudad, estadoUni, entidadId);
        }
    }
}
