package ftn.iis.repository;

import ftn.iis.model.Komentar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KomentarRepository extends JpaRepository<Komentar, Long> {
    // Svi komentari za jednu knjigu (samo top-level, bez odgovora)
    List<Komentar> findByKnjigaIsbnAndOdgovorNaIsNull(String isbn);

    // Svi komentari za jednu knjigu (uključujući odgovore) - za brisanje
    List<Komentar> findByKnjigaIsbn(String isbn);

    // Provjera da li clan vec lajkovao komentar
    @Query("SELECT COUNT(u) > 0 FROM Komentar k JOIN k.lajkovali u WHERE k.id = :komentarId AND u.jmbg = :jmbg")
    boolean daLiJeLajkovao(@Param("komentarId") Long komentarId, @Param("jmbg") String jmbg);
}
