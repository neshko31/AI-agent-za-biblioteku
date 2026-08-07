package ftn.iis.repository;

import ftn.iis.model.Pozajmica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PozajmicaRepository extends JpaRepository<Pozajmica, Long> {
    List<Pozajmica> findByClan_JmbgAndStatusPozTrue(String jmbg);

    List<Pozajmica> findByClan_Jmbg(String jmbg);
    @Query("SELECT p FROM Pozajmica p WHERE p.clan.jmbg = :jmbg AND p.statusPoz = true AND p.datOcVrac < :today")
    List<Pozajmica> findOverduePozajmiceByJmbg(@Param("jmbg") String jmbg, @Param("today") LocalDate today);
    @Query("SELECT COUNT(p) > 0 FROM Pozajmica p WHERE p.clan.jmbg = :jmbg AND p.statusPoz = true AND p.datOcVrac < :today")
    boolean hasOverduePozajmica(@Param("jmbg") String jmbg, @Param("today") LocalDate today);

    //koristim da proverim da li osobi istice pozajmica za 2 dana
    @Query("SELECT p FROM Pozajmica p WHERE p.statusPoz = true AND p.datOcVrac = :targetDate")
    List<Pozajmica> findPozajmiceDueOn(@Param("targetDate") LocalDate targetDate);
    @Query("SELECT COUNT(p) FROM Pozajmica p WHERE p.primerakKnjige.fizickaKnjiga.isbn = :isbn AND p.datPoz >= :od")
    Integer countByIsbnAndDatPozAfter(@Param("isbn") String isbn, @Param("od") LocalDate od);

    //vraca sve za bibliotekara
    @Query("SELECT p FROM Pozajmica p " +
            "JOIN FETCH p.primerakKnjige pk " +
            "JOIN FETCH pk.fizickaKnjiga fk " +
            "JOIN FETCH fk.knjiga k " +
            "JOIN FETCH p.clan c " +
            "WHERE p.statusPoz = true " +
            "ORDER BY p.datOcVrac ASC")
    List<Pozajmica> findAllActivePozajmice();

    @Query("SELECT DISTINCT p from Pozajmica p JOIN FETCH p.primerakKnjige pk JOIN FETCH pk.fizickaKnjiga fk JOIN FETCH fk.knjiga k LEFT JOIN FETCH k.zanr z JOIN FETCH p.clan c LEFT JOIN FETCH c.kategorijaClana kc WHERE p.datPoz BETWEEN :od AND :datDo ORDER BY p.datPoz")
    List<Pozajmica> findAllInPeriodWithDetails(@Param("od") LocalDate od, @Param("datDo") LocalDate datDo);

    @Query("SELECT DISTINCT p.primerakKnjige.fizickaKnjiga.isbn FROM Pozajmica p")
    List<String> findAllEverBorrowedIsbns();

    @Query("SELECT p FROM Pozajmica p " +
           "JOIN FETCH p.primerakKnjige pk " +
           "JOIN FETCH pk.fizickaKnjiga fk " +
           "JOIN FETCH fk.knjiga k " +
           "LEFT JOIN FETCH k.zanr z " +
           "JOIN FETCH p.clan c " +
           "WHERE p.statusPoz = true AND p.datOcVrac < :today " +
           "ORDER BY p.datOcVrac ASC")
    List<Pozajmica> findAllCurrentlyOverdue(@Param("today") LocalDate today);
}
