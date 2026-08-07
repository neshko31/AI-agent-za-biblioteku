package ftn.iis.repository;

import ftn.iis.model.CitanjeEKnjige;
import ftn.iis.model.id.CitanjeEKnjigeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitanjeEKnjigeRepository extends JpaRepository<CitanjeEKnjige, CitanjeEKnjigeId> {
    Optional<CitanjeEKnjige> findTopByIdJmbgClanaAndIdIsbnEKnjigeOrderByDatumPoslednjegPristupaDesc(
            String jmbgClana,
            String isbnEKnjige
    );
    @Query("SELECT c FROM CitanjeEKnjige c JOIN FETCH c.eKnjiga ek JOIN FETCH ek.knjiga WHERE c.id.jmbgClana = :jmbg AND c.datumZavrsetka IS NULL AND c.id.datumPocetka >= :cutoff")
    List<CitanjeEKnjige> findActiveByJmbg(@Param("jmbg") String jmbg, @Param("cutoff") LocalDate cutoff);

    @Query("SELECT c FROM CitanjeEKnjige c WHERE c.datumZavrsetka IS NULL AND c.id.datumPocetka < :cutoff")
    List<CitanjeEKnjige> findExpiredActive(@Param("cutoff") LocalDate cutoff);

    @Query("SELECT COUNT(c) > 0 FROM CitanjeEKnjige c WHERE c.id.jmbgClana = :jmbg AND c.id.isbnEKnjige = :isbn AND c.datumZavrsetka IS NULL AND c.id.datumPocetka >= :cutoff")
    boolean hasActiveLoan(@Param("jmbg") String jmbg, @Param("isbn") String isbn, @Param("cutoff") LocalDate cutoff);
}
