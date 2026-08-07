package ftn.iis.repository;

import ftn.iis.model.OcenaCetPoruke;
import ftn.iis.model.id.OcenaCetPorukeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OcenaCetPorukeRepository extends JpaRepository<OcenaCetPoruke, OcenaCetPorukeId>  {
}
