package ottosulaoja.drsulxx.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ottosulaoja.drsulxx.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Query method to find users by email, case insensitive
    List<User> findByEmailIgnoreCase(String email);

    // Query method to find users by username, case insensitive
    List<User> findByUsernameContainingIgnoreCase(String username);

    // Query method to find users by name, case insensitive
    List<User> findByNameContainingIgnoreCase(String name);

    // Query method to find users by family name, case insensitive
    List<User> findByFamilyNameContainingIgnoreCase(String familyName);

    // Combined search query for username, name, or family name
    @Query("SELECT u FROM User u WHERE lower(u.username) LIKE lower(concat('%', :query, '%')) " +
           "OR lower(u.name) LIKE lower(concat('%', :query, '%')) " +
           "OR lower(u.familyName) LIKE lower(concat('%', :query, '%'))")
    List<User> searchByUsernameOrNameOrFamilyName(String query);

    // Custom query method to delete a user by email
    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.email = ?1")
    void deleteByEmail(String email);

    // Custom query method to delete a user by username
    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.username = ?1")
    void deleteByUsername(String username);

    // Check if a user exists by email
    boolean existsByEmail(String email);

    // Check if a user exists by username
    boolean existsByUsername(String username);

    // Check if a user exists by ID
    boolean existsById(Long id);

    // Find user by username for authentication
    Optional<User> findByUsername(String username);

    // Find user by email for authentication
    Optional<User> findByEmail(String email);

    // Find all users whose accounts are locked (account_non_locked is false)
    List<User> findByAccountNonLockedFalse();

    // Find user by reset token
    Optional<User> findByResetToken(String resetToken);

    // Find users by reset token expiration time
    @Query("SELECT u FROM User u WHERE u.resetTokenExpiration < CURRENT_TIMESTAMP")
    List<User> findExpiredResetTokens();

    // **New methods added below:**

    // Find all users that are currently active (not deleted)
    List<User> findByDeletedFalse();

    // Update user's password by ID
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.password = :password WHERE u.id = :userId")
    void updatePasswordById(@Param("userId") Long userId, @Param("password") String password);

    // Activate or deactivate user account
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.deleted = :status WHERE u.id = :userId")
    void updateUserStatusById(@Param("userId") Long userId, @Param("status") Boolean status);
}