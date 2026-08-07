package ftn.iis.repository;

import ftn.iis.model.PrimerakKnjige;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrimerakKnjigeRepository extends JpaRepository<PrimerakKnjige, Long> {

    //List<PrimerakKnjige> findByFizickaKnjiga_Isbn(String isbn);
    @Query("SELECT p FROM PrimerakKnjige p " +
            "JOIN FETCH p.fizickaKnjiga fk " +
            "WHERE fk.isbn = :isbn " +
            "AND NOT EXISTS (" +
            "  SELECT poz FROM Pozajmica poz " +
            "  WHERE poz.primerakKnjige = p AND poz.statusPoz = true" +
            ")")
    List<PrimerakKnjige> findAvailablePrimerciByIsbn(@Param("isbn") String isbn);

    @Query("SELECT COUNT(p) FROM PrimerakKnjige p")
    long countAllPrimerci();

    @Query("SELECT COUNT(p) FROM PrimerakKnjige p WHERE NOT EXISTS " +
           "(SELECT poz FROM Pozajmica poz WHERE poz.primerakKnjige = p AND poz.statusPoz = true)")
    long countAvailablePrimerci();

}
