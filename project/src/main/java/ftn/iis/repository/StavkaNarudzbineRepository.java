package ftn.iis.repository;

import ftn.iis.model.StavkaNarudzbine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StavkaNarudzbineRepository extends JpaRepository<StavkaNarudzbine, Long> {
    List<StavkaNarudzbine> findAllByNarudzbinaId(Long narudzbinaId);
}
