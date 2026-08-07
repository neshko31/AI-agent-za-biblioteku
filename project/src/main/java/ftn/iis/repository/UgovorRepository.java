package ftn.iis.repository;

import ftn.iis.enums.StatusUgovora;
import ftn.iis.model.Ugovor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UgovorRepository extends JpaRepository<Ugovor, Long> {

    // Provera da li dobavljač već ima aktivan ugovor
    boolean existsByDobavljacIdAndStatus(Long dobavljacId, StatusUgovora status);
    List<Ugovor> findAllByDobavljacId(Long dobavljacId);
    List<Ugovor> findAllByStatus(StatusUgovora statusUgovora);
}
