package ftn.iis.repository;

import ftn.iis.model.Biblioteka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BibliotekaRepository extends JpaRepository<Biblioteka, String> {

}
