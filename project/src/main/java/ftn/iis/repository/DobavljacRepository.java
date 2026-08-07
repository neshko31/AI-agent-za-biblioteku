package ftn.iis.repository;

import ftn.iis.model.Dobavljac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DobavljacRepository extends JpaRepository<Dobavljac, Long> {
    boolean existsByEmail(String email);
    boolean existsByPib(String pib);
    boolean existsByNaziv(String naziv);
    boolean existsByTel(String tel);
}
