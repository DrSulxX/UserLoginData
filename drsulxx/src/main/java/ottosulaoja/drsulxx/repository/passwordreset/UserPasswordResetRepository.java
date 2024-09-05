package ottosulaoja.drsulxx.repository.passwordreset;

import org.springframework.data.jpa.repository.JpaRepository;

import ottosulaoja.drsulxx.model.passwordreset.UserPasswordReset;

import java.util.Optional;

public interface UserPasswordResetRepository extends JpaRepository<UserPasswordReset, Long> {

    Optional<UserPasswordReset> findByUserId(Long userId);

    Optional<UserPasswordReset> findByResetToken(String resetToken);
}