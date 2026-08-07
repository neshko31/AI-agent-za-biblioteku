package ftn.iis.repository;

import ftn.iis.model.CetPoruka;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CetPorukaRepository extends JpaRepository<CetPoruka,Long> {
    // Pronalazi poruke u datim sesijama
    List<CetPoruka> findByCetSesijaIdIn(List<Long> ids);
}
