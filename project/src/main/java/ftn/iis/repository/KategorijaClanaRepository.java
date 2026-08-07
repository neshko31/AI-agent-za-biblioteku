package ftn.iis.repository;

import ftn.iis.enums.TipKC;
import ftn.iis.model.KategorijaClana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KategorijaClanaRepository extends JpaRepository<KategorijaClana, Long> {
    Optional<KategorijaClana> findByTipKC(TipKC tipKC);
}
