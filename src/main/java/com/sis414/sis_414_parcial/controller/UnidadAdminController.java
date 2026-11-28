package com.sis414.sis_414_parcial.controller;

import com.sis414.sis_414_parcial.dto.UnidadAdminDto;
import com.sis414.sis_414_parcial.service.UnidadAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.server.ResponseStatusException;
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
        return service.create(dto.toDto(), dto.entidadCodigo(), dto.entidadNombre());
    }

    @PutMapping("/{id}")
    @Operation(summary = "UnidadAdmin - Modificar")
    public UnidadAdminDto update(@PathVariable Long id, @RequestBody UnidadAdminRequest dto) {
        return service.update(id, dto.toDto(), dto.entidadCodigo(), dto.entidadNombre());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "UnidadAdmin - Eliminar")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleValidationError(ResponseStatusException ex) {
        return ProblemDetail.forStatusAndDetail(ex.getStatusCode(),
                ex.getReason() == null ? "No se pudo procesar la unidad" : ex.getReason());
    }

    // The database assigns the ID; clients do not provide it in the request body.
    public record UnidadAdminRequest(
            @Schema(example = "001", minLength = 1, maxLength = 5, requiredMode = Schema.RequiredMode.REQUIRED) String unidad,
            @Schema(example = "Administracion", maxLength = 120) String descrip,
            @Schema(example = "Potosi", maxLength = 20) String ciudad,
            @Schema(example = "1") Integer estadoUni,
            @Schema(hidden = true) Long entidadId,
            @Schema(example = "0001", maxLength = 4, description = "Codigo de la entidad; al modificar puede omitirse para conservar la entidad actual") String entidadCodigo,
            @Schema(example = "Entidad de prueba", maxLength = 110, description = "Nombre real de la entidad si su codigo aun no existe; se registrara junto con la unidad") String entidadNombre
    ) {
        UnidadAdminDto toDto() {
            return new UnidadAdminDto(null, unidad, descrip, ciudad, estadoUni, entidadId);
        }
    }
}
