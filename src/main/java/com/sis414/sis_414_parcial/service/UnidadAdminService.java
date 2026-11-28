package com.sis414.sis_414_parcial.service;

import com.sis414.sis_414_parcial.dto.UnidadAdminDto;
import com.sis414.sis_414_parcial.entity.UnidadAdmin;
import com.sis414.sis_414_parcial.repository.EntidadRepository;
import com.sis414.sis_414_parcial.repository.UnidadAdminRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UnidadAdminService {

    private final UnidadAdminRepository repository;
    private final EntidadRepository entidadRepository;

    public UnidadAdminService(UnidadAdminRepository repository, EntidadRepository entidadRepository) {
        this.repository = repository;
        this.entidadRepository = entidadRepository;
    }

    @Transactional(readOnly = true)
    public List<UnidadAdminDto> findAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public UnidadAdminDto findById(Long id) {
        return toDto(getOrThrow(id));
    }

    @Transactional
    public UnidadAdminDto create(UnidadAdminDto dto) {
        UnidadAdmin entity = new UnidadAdmin();
        apply(dto, entity);
        return toDto(saveValidated(entity));
    }

    @Transactional
    public UnidadAdminDto update(Long id, UnidadAdminDto dto) {
        UnidadAdmin entity = getOrThrow(id);
        apply(dto, entity);
        return toDto(saveValidated(entity));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getOrThrow(id));
    }

    public UnidadAdmin getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "UnidadAdmin no encontrado: " + id));
    }

    public UnidadAdminDto toDto(UnidadAdmin e) {
        return new UnidadAdminDto(
                e.getId(),
                e.getUnidad(),
                e.getDescrip(),
                e.getCiudad(),
                e.getEstadoUni(),
                e.getEntidad() == null ? null : e.getEntidad().getId()
        );
    }

    private UnidadAdmin saveValidated(UnidadAdmin entity) {
        try {
            return repository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se pudo guardar la unidad: los datos incumplen una restriccion de la base de datos", ex);
        }
    }

    private void apply(UnidadAdminDto dto, UnidadAdmin e) {
        String unidad = dto.unidad() == null ? null : dto.unidad().trim();
        String descrip = dto.descrip() == null ? null : dto.descrip().trim();
        String ciudad = dto.ciudad() == null ? null : dto.ciudad().trim();

        if (unidad == null || unidad.isEmpty() || unidad.length() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El codigo de unidad es obligatorio y debe tener entre 1 y 5 caracteres");
        }
        validateLength(descrip, 120, "Descripcion");
        validateLength(ciudad, 20, "Ciudad");

        if (dto.entidadId() != null) {
            e.setEntidad(entidadRepository.findById(dto.entidadId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "La entidad indicada no existe: " + dto.entidadId())));
        } else if (e.getEntidad() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe indicar una entidad existente (entidadId)");
        }
        // In an update, omitting entidadId keeps the existing entity association.
        e.setUnidad(unidad);
        e.setDescrip(descrip);
        e.setCiudad(ciudad);
        e.setEstadoUni(dto.estadoUni());
    }

    private void validateLength(String value, int max, String field) {
        if (value != null && value.length() > max) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    field + " no debe superar " + max + " caracteres");
        }
    }
}
