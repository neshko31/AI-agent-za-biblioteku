package ftn.iis.repository;

import ftn.iis.model.Kazna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KaznaRepository extends JpaRepository<Kazna, Long> {

    List<Kazna> findByClanJmbgOrderByDatNastankaDesc(String jmbg);

    @Query("SELECT k from Kazna k WHERE k.pozajmica.idP= :idP AND k.placena= false AND k.brojDanaPrekoracenja IS NOT NULL")
    Optional<Kazna> findActiveOverdueKaznaForPozajmica(@Param("idP") Long idP);


    @Query("SELECT k FROM Kazna k WHERE k.pozajmica.idP = :idP AND k.brojDanaPrekoracenja IS NULL")
    Optional<Kazna> findLostBookKaznaForPozajmica(@Param("idP") Long idP);

    boolean existsByPozajmica_IdPAndPlacenaFalseAndBrojDanaPrekoracenjaIsNotNull(Long idP);
    boolean existsByPozajmica_IdPAndPlacenaTrueAndBrojDanaPrekoracenjaIsNotNull(Long idP);

    @Query("SELECT k FROM Kazna k " +
           "JOIN FETCH k.pozajmica poz " +
           "JOIN FETCH poz.primerakKnjige pk " +
           "JOIN FETCH pk.fizickaKnjiga fk " +
           "JOIN FETCH fk.knjiga kn " +
           "LEFT JOIN FETCH kn.zanr " +
           "JOIN FETCH k.clan c " +
           "WHERE k.brojDanaPrekoracenja IS NULL " +
           "ORDER BY k.datNastanka DESC")
    List<Kazna> findAllLostBookKazne();

}
