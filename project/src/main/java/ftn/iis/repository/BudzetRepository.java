package ftn.iis.repository;

import ftn.iis.model.Budzet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BudzetRepository extends JpaRepository<Budzet,Long> {
}
