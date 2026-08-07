package ftn.iis.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import ftn.iis.model.Rezervacija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RezervacijaRepository extends JpaRepository<Rezervacija, Long> {
    List<Rezervacija> findByClan_Jmbg(String jmbg);

    @Query("SELECT r FROM Rezervacija r WHERE r.fizickaKnjiga.isbn = :isbn AND r.datObavR IS NULL ORDER BY r.datR ASC")
    List<Rezervacija> findActiveRezervacijeByIsbn(@Param("isbn") String isbn);


    @Query("SELECT COUNT(r) > 0 FROM Rezervacija r WHERE r.clan.jmbg = :jmbg AND r.fizickaKnjiga.isbn = :isbn AND r.datObavR IS NULL")
    boolean hasActiveRezervacija(@Param("jmbg") String jmbg, @Param("isbn") String isbn);

    @Query("SELECT MIN(p.datOcVrac) FROM Pozajmica p WHERE p.primerakKnjige.fizickaKnjiga.isbn = :isbn AND p.statusPoz = true")
    Optional<LocalDate> findFirstActivePozajmicaReturnDate(@Param("isbn") String isbn);

    @Query("SELECT COUNT(r) FROM Rezervacija r WHERE r.fizickaKnjiga.isbn = :isbn AND r.datR >= :od")
    Integer countByFizickaKnjigaIsbnAndDatRAfter(@Param("isbn") String isbn, @Param("od") LocalDate od);
}
