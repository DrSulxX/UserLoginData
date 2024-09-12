package ottosulaoja.drsulxx.repository.backup;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ottosulaoja.drsulxx.model.usermanagement.User;

@Repository
public interface BackupUserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsername(String username);
    // No extra methods required unless you want specific queries for the backup
}