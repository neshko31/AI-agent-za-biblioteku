package ftn.iis.repository;
import ftn.iis.enums.StatusPredloga;
import ftn.iis.model.PredlogZaNabavku;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PredlogNabavkaRepository extends JpaRepository<PredlogZaNabavku, Long> {
    List<PredlogZaNabavku> findAllByKorisnikJmbgOrderByDatumPodnosenjaDesc(String jmbg);

    List<PredlogZaNabavku> findAllByOrderByDatumPodnosenjaDesc();

    List<PredlogZaNabavku> findAllByStatusOrderByDatumPodnosenjaDesc(StatusPredloga status);

    List<PredlogZaNabavku> findAllByStatus(StatusPredloga status);

    List<PredlogZaNabavku> findAllByDatumPodnosenjaBetween(LocalDate od, LocalDate datDo);
}
