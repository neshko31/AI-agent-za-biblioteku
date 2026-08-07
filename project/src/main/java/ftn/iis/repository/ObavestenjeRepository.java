package ftn.iis.repository;

import ftn.iis.model.Obavestenje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObavestenjeRepository extends JpaRepository<Obavestenje, Long> {

    List<Obavestenje> findByClan_JmbgOrderByDatKreiranDesc(String jmbg);
    //List<Obavestenje> findByClan_JmbgAndProcitanoFalseOrderByDatKreiranDesc(String jmbg);
}
