package ftn.iis.repository;

import ftn.iis.model.Katalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KatalogRepository extends JpaRepository<Katalog,Long> {
    Optional<Katalog> findByKatId(Long katId);
}
