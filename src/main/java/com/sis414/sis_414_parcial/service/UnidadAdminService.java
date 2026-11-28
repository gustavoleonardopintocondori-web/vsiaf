package com.sis414.sis_414_parcial.service;

import com.sis414.sis_414_parcial.dto.UnidadAdminDto;
import com.sis414.sis_414_parcial.entity.UnidadAdmin;
import com.sis414.sis_414_parcial.entity.Entidad;
import com.sis414.sis_414_parcial.repository.EntidadRepository;
import com.sis414.sis_414_parcial.repository.UnidadAdminRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.LocalDate;

@Service
public class UnidadAdminService {

    @PersistenceContext
    private EntityManager entityManager;

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
        return create(dto, null, null);
    }

    @Transactional
    public UnidadAdminDto create(UnidadAdminDto dto, String entidadCodigo, String entidadNombre) {
        lockUnitWrites();
        UnidadAdmin entity = new UnidadAdmin();
        apply(dto, entity, entidadCodigo, entidadNombre);
        validateUniqueUnit(entity);
        return toDto(saveValidated(entity));
    }

    @Transactional
    public UnidadAdminDto update(Long id, UnidadAdminDto dto) {
        return update(id, dto, null, null);
    }

    @Transactional
    public UnidadAdminDto update(Long id, UnidadAdminDto dto, String entidadCodigo, String entidadNombre) {
        lockUnitWrites();
        UnidadAdmin entity = getOrThrow(id);
        apply(dto, entity, entidadCodigo, entidadNombre);
        validateUniqueUnit(entity);
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

    // PostgreSQL transaction-level lock serializes unit writes, including creation of a new parent entity.
    private void lockUnitWrites() {
        entityManager.createNativeQuery("SELECT 1 FROM pg_advisory_xact_lock(41420261128)")
                .getSingleResult();
    }

    private void validateUniqueUnit(UnidadAdmin unit) {
        Long matches = entityManager.createQuery(
                        "select count(u) from UnidadAdmin u where u.entidad.id = :entidadId "
                        + "and lower(trim(u.unidad)) = lower(:codigo) and u.id <> :currentId", Long.class)
                .setParameter("entidadId", unit.getEntidad().getId())
                .setParameter("codigo", unit.getUnidad())
                .setParameter("currentId", unit.getId() == null ? -1L : unit.getId())
                .getSingleResult();
        if (matches > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La unidad " + unit.getUnidad() + " ya existe en esta entidad. No se permiten duplicados.");
        }
    }

    private UnidadAdmin saveValidated(UnidadAdmin entity) {
        try {
            return repository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se pudo guardar la unidad: los datos incumplen una restriccion de la base de datos", ex);
        }
    }

    private void apply(UnidadAdminDto dto, UnidadAdmin e, String entidadCodigo, String entidadNombre) {
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
        } else if (entidadCodigo != null && !entidadCodigo.isBlank()) {
            e.setEntidad(resolveEntity(entidadCodigo, entidadNombre));
        } else if (e.getEntidad() == null) {
            List<Entidad> existing = entidadRepository.findAll(PageRequest.of(0, 2)).getContent();
            if (existing.size() == 1) {
                e.setEntidad(existing.get(0));
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Indique el codigo de la entidad y, si es nueva, su nombre");
            }
        }
        e.setUnidad(unidad);
        e.setDescrip(descrip);
        e.setCiudad(ciudad);
        e.setEstadoUni(dto.estadoUni());
    }

    private Entidad resolveEntity(String rawCode, String rawName) {
        String code = rawCode.trim();
        String name = rawName == null ? "" : rawName.trim();
        validateLength(code, 4, "Codigo de entidad");
        validateLength(name, 110, "Nombre de entidad");

        Entidad probe = new Entidad();
        probe.setCodigo(code);
        List<Entidad> matches = entidadRepository.findAll(Example.of(probe), PageRequest.of(0, 2)).getContent();
        if (matches.size() > 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El codigo identifica varias entidades; seleccione una entidad especifica");
        }
        if (matches.size() == 1) {
            return matches.get(0);
        }
        if (name.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La entidad no existe; indique su nombre para registrarla");
        }
        Entidad entidad = new Entidad();
        entidad.setCodigo(code);
        entidad.setDescEnt(name);
        entidad.setGestion(LocalDate.now().getYear());
        try {
            return entidadRepository.saveAndFlush(entidad);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se pudo registrar la entidad con los datos indicados", ex);
        }
    }

    private void validateLength(String value, int max, String field) {
        if (value != null && value.length() > max) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    field + " no debe superar " + max + " caracteres");
        }
    }
}
