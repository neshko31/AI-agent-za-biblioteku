package ftn.iis.repository;

import ftn.iis.model.EKnjiga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EKnjigaRepository extends JpaRepository<EKnjiga, String> {
}
