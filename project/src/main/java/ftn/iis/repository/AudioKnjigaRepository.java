package ftn.iis.repository;

import ftn.iis.model.AudioKnjiga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AudioKnjigaRepository extends JpaRepository<AudioKnjiga, String> {
}
