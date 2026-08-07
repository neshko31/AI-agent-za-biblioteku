package ftn.iis.repository;

import ftn.iis.model.Reklamacija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReklamacijaRepository extends JpaRepository<Reklamacija, Long> {
    List<Reklamacija> findAllByOrderByDatumPodnosenjaDesc();
    Optional<Reklamacija> findByNarudzbinaId(Long narudzbinaId);
}
