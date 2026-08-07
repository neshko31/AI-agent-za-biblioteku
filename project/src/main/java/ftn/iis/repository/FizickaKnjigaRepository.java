package ftn.iis.repository;

import ftn.iis.model.FizickaKnjiga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FizickaKnjigaRepository extends JpaRepository<FizickaKnjiga, String> {
    @Query("SELECT COUNT(pk) FROM PrimerakKnjige pk WHERE pk.fizickaKnjiga.isbn = :isbn")
    Integer countPrimerciByIsbn(@Param("isbn") String isbn);
}
