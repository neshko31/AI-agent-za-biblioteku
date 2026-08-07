package ftn.iis.repository;

import ftn.iis.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByJmbg(String jmbg);
    boolean existsByEmail(String email);
    boolean existsByJmbg(String jmbg);
}
