package ftn.iis.repository;

import ftn.iis.model.ProduzenjePozajmice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProduzenjePozajmiceRepository extends JpaRepository<ProduzenjePozajmice, Long> {

    //List<ProduzenjePozajmice> findByPozajmica_IdP(Long pozajmicaId);


    @Query("SELECT pp FROM ProduzenjePozajmice pp WHERE pp.statusPP IS NULL ORDER BY pp.datKrePP ASC")
    List<ProduzenjePozajmice> findPendingExtensions();
}
