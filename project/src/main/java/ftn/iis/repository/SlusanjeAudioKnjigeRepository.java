package ftn.iis.repository;

import ftn.iis.model.SlusanjeAudioKnjige;
import ftn.iis.model.id.SlusanjeAudioKnjigeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlusanjeAudioKnjigeRepository extends JpaRepository<SlusanjeAudioKnjige, SlusanjeAudioKnjigeId> {
    Optional<SlusanjeAudioKnjige> findTopByIdJmbgClanaAndIdIsbnAudioKnjigeOrderByDatumPoslednjegPristupaDesc(
            String jmbgClana,
            String isbnAudioKnjige
    );
    @Query("SELECT s FROM SlusanjeAudioKnjige s JOIN FETCH s.audioKnjiga ak JOIN FETCH ak.knjiga WHERE s.id.jmbgClana = :jmbg AND s.datumZavrsetka IS NULL AND s.id.datumPocetka >= :cutoff")
    List<SlusanjeAudioKnjige> findActiveByJmbg(@Param("jmbg") String jmbg, @Param("cutoff") LocalDate cutoff);

    @Query("SELECT s FROM SlusanjeAudioKnjige s WHERE s.datumZavrsetka IS NULL AND s.id.datumPocetka < :cutoff")
    List<SlusanjeAudioKnjige> findExpiredActive(@Param("cutoff") LocalDate cutoff);

    @Query("SELECT COUNT(s) > 0 FROM SlusanjeAudioKnjige s WHERE s.id.jmbgClana = :jmbg AND s.id.isbnAudioKnjige = :isbn AND s.datumZavrsetka IS NULL AND s.id.datumPocetka >= :cutoff")
    boolean hasActiveLoan(@Param("jmbg") String jmbg, @Param("isbn") String isbn, @Param("cutoff") LocalDate cutoff);
}
