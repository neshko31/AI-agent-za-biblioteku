package ftn.iis.repository;

import ftn.iis.model.Narudzbina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository

public interface NarudzbinaRepository extends JpaRepository<Narudzbina, Long> {
    List<Narudzbina> findAllByOrderByDatumKreiranjaDesc();

    List<Narudzbina> findAllByDobavljacIdOrderByDatumKreiranjaDesc(Long dobavljacId);

    List<Narudzbina> findAllByDatumKreiranjaBetween(LocalDate od, LocalDate datDo);
}
