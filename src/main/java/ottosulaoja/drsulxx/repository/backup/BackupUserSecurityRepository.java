package ottosulaoja.drsulxx.repository.backup;

import org.springframework.data.jpa.repository.JpaRepository;
import ottosulaoja.drsulxx.model.security.UserSecurity;

public interface BackupUserSecurityRepository extends JpaRepository<UserSecurity, Long> {
}