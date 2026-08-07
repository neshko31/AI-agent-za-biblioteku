package ftn.iis.repository;

import ftn.iis.model.CetSesija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CetSesijaRepository extends JpaRepository<CetSesija, Long> {
    // Pronalazi samo arhivirane i one kojima je datum kreiranja pre granice, koristi se za brisanje posle 30 dana
    List<CetSesija> findByArhiviranoIsTrueAndDatumArhiviranjaCSBefore(LocalDateTime granica);

    // Pronalazi sve grane od date roditeljske sesije
    List<CetSesija> findByRoditeljskaSesija(CetSesija roditeljskaSesija);

    // Pretraga onih koji su kreirani u vremenskom opsegu, potrebno za izvestaj
    List<CetSesija> findByDatumKreiranjaCSBetween(LocalDateTime odDatum, LocalDateTime doDatum);

    // Prebrojavanje onih koji su arhivirani u tom vremenskom opsegu
    long countByArhiviranoIsTrueAndDatumArhiviranjaCSBetween(LocalDateTime odDatum, LocalDateTime doDatum);
}
