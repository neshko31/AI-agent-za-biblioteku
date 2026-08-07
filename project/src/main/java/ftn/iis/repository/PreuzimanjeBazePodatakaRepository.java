package ftn.iis.repository;

import ftn.iis.model.PreuzimanjeBazePodataka;
import ftn.iis.model.id.PreuzimanjeBazePodatakaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreuzimanjeBazePodatakaRepository extends JpaRepository<PreuzimanjeBazePodataka, PreuzimanjeBazePodatakaId> {
}
