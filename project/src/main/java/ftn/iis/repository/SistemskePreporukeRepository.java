package ftn.iis.repository;

import ftn.iis.enums.StatusSistemskePreporuke;
import ftn.iis.model.SistemskaPreporuka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SistemskePreporukeRepository extends JpaRepository<SistemskaPreporuka, Long> {
    boolean existsByFizickaKnjigaIsbnAndStatus(String isbn, StatusSistemskePreporuke status);

    List<SistemskaPreporuka> findAllByStatusOrderByDatumGenerisanjaDesc(StatusSistemskePreporuke status);

    Optional<SistemskaPreporuka> findByFizickaKnjigaIsbnAndStatus(String isbn, StatusSistemskePreporuke status);

    List<SistemskaPreporuka> findAllByStatus(StatusSistemskePreporuke status);

    Optional<SistemskaPreporuka> findByFizickaKnjigaIsbn(String isbn);
}
