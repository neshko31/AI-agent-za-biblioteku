package ftn.iis.repository;

import ftn.iis.model.BudzetPoZanru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudzetPoZanruRepository extends JpaRepository<BudzetPoZanru, Long> {
    Optional<BudzetPoZanru> findByZanrId(Long zanrId);

    List<BudzetPoZanru> findAllByOrderByZanrNameAsc();

    boolean existsByZanrId(Long zanrId);
}
