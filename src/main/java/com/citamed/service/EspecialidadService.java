package com.citamed.service;

import com.citamed.domain.Especialidad;
import com.citamed.repository.EspecialidadRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EspecialidadService {

    // Operaciones básicas del catálogo de especialidades médicas.
    private final EspecialidadRepository especialidadRepository;

    public EspecialidadService(EspecialidadRepository especialidadRepository) {
        this.especialidadRepository = especialidadRepository;
    }

    @Transactional(readOnly = true)
    public List<Especialidad> getEspecialidades(boolean activo) {
        // Permite recuperar todas las especialidades o solamente las activas.
        if (activo) {
            return especialidadRepository.findByActivoTrue();
        }
        return especialidadRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Especialidad> getEspecialidad(Integer idEspecialidad) {
        return especialidadRepository.findById(idEspecialidad);
    }

    @Transactional
    public Especialidad save(Especialidad especialidad) {
        return especialidadRepository.save(especialidad);
    }
}
