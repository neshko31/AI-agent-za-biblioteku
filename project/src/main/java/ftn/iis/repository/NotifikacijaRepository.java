package ftn.iis.repository;
import ftn.iis.model.Notifikacija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotifikacijaRepository extends JpaRepository<Notifikacija, Long> {
    List<Notifikacija> findAllByKorisnikJmbgOrderByDatumDesc(String jmbg);
    Integer countByKorisnikJmbgAndProcitanaFalse(String jmbg);
}