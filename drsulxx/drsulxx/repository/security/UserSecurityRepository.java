package ottosulaoja.drsulxx.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import ottosulaoja.drsulxx.model.security.UserSecurity;

import java.util.List;
import java.util.Optional;

public interface UserSecurityRepository extends JpaRepository<UserSecurity, Long> {

    // Find UserSecurity by user ID
    Optional<UserSecurity> findByUserId(Long userId);

    // Find all locked accounts
    List<UserSecurity> findByAccountNonLockedFalse();

    // Corrected query to fetch UserSecurity by the username of the associated User entity
    @Query("SELECT us FROM UserSecurity us WHERE us.userId = (SELECT u.id FROM User u WHERE LOWER(u.username) = LOWER(:username))")
    Optional<UserSecurity> findByUsername(@Param("username") String username);

    // Corrected query to fetch UserSecurity by the email of the associated User entity
    @Query("SELECT us FROM UserSecurity us WHERE us.userId = (SELECT u.id FROM User u WHERE LOWER(u.email) = LOWER(:email))")
    Optional<UserSecurity> findByEmail(@Param("email") String email);

    // Custom query method to update specific fields
    @Modifying
    @Transactional
    @Query("UPDATE UserSecurity us SET us.accountNonLocked = :accountNonLocked WHERE us.userId = :userId")
    void updateAccountNonLockedByUserId(@Param("userId") Long userId, @Param("accountNonLocked") Boolean accountNonLocked);

    @Modifying
    @Transactional
    @Query("UPDATE UserSecurity us SET us.enabled = :enabled WHERE us.userId = :userId")
    void updateEnabledByUserId(@Param("userId") Long userId, @Param("enabled") Boolean enabled);

    // Check if UserSecurity exists by user ID
    @Query("SELECT CASE WHEN COUNT(us) > 0 THEN true ELSE false END FROM UserSecurity us WHERE us.userId = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
}