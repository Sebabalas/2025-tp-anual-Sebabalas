package ar.edu.utn.dds.k3003.repository;

import ar.edu.utn.dds.k3003.model.PdI;
import ar.edu.utn.dds.k3003.model.EstadoPdi;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.domain.Pageable;

import jakarta.transaction.Transactional;

public interface PdIRepository {
    Optional<PdI> findById(Long id);
    List<PdI> findByHechoId(String id);
    PdI save(PdI pdi);
    List<PdI> findAll();
    void deleteAll();
    List<PdI> findByEstadoOrderByIdAsc(EstadoPdi estado, Pageable pageable);
    
    @Transactional
    @Modifying
    void deleteByHechoId(String hechoId);
}
