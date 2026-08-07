package ftn.iis.repository;

import ftn.iis.model.ElektronskaBazaPodataka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElektronskaBazaPodatakaRepository extends JpaRepository<ElektronskaBazaPodataka, Long> {
    List<ElektronskaBazaPodataka> findByNazivContainingIgnoreCase(String query);
}
