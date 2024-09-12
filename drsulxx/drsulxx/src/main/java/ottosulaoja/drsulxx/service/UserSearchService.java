package ottosulaoja.drsulxx.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ottosulaoja.drsulxx.model.usermanagement.User;
import ottosulaoja.drsulxx.repository.usermanagement.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserSearchService {

    private final UserRepository userRepository;
    private final Cache<Long, User> userIdCache;
    private final Cache<String, List<User>> userEmailCache;
    private final Cache<String, List<User>> userUsernameCache;
    private final Cache<String, List<User>> userNameCache;
    private final Cache<String, List<User>> userFamilyNameCache;
    private static final Logger logger = LoggerFactory.getLogger(UserSearchService.class);

    @Autowired
    public UserSearchService(UserRepository userRepository) {
        this.userRepository = userRepository;

        this.userIdCache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();

        this.userEmailCache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();

        this.userUsernameCache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();

        this.userNameCache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();

        this.userFamilyNameCache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();
    }

    public List<User> getUsersByEmail(String email) {
        try {
            List<User> users = userEmailCache.getIfPresent(email.toLowerCase());
            if (users != null && !users.isEmpty()) {
                logger.info("Cache hit for email query");
                return users;
            }

            users = userRepository.findByEmailIgnoreCase(email);
            if (users != null && !users.isEmpty()) {
                userEmailCache.put(email.toLowerCase(), users);
                logger.info("Cache miss for email query. Fetched from database and cached.");
            } else {
                logger.info("No users found for email query");
            }
            return users;
        } catch (Exception e) {
            logger.error("Error retrieving users by email", e);
            return List.of(); // Return an empty list in case of error
        }
    }

    public List<User> searchUsersByUsername(String username) {
        try {
            List<User> users = userUsernameCache.getIfPresent(username.toLowerCase());
            if (users != null && !users.isEmpty()) {
                logger.info("Cache hit for username query");
                return users;
            }

            users = userRepository.findByUsernameContainingIgnoreCase(username);
            if (users != null && !users.isEmpty()) {
                userUsernameCache.put(username.toLowerCase(), users);
                logger.info("Cache miss for username query. Fetched from database and cached.");
            } else {
                logger.info("No users found for username query");
            }
            return users;
        } catch (Exception e) {
            logger.error("Error retrieving users by username", e);
            return List.of(); // Return an empty list in case of error
        }
    }

    public List<User> searchUsersByName(String name) {
        try {
            List<User> users = userNameCache.getIfPresent(name.toLowerCase());
            if (users != null && !users.isEmpty()) {
                logger.info("Cache hit for name query");
                return users;
            }

            users = userRepository.findByNameContainingIgnoreCase(name);
            if (users != null && !users.isEmpty()) {
                userNameCache.put(name.toLowerCase(), users);
                logger.info("Cache miss for name query. Fetched from database and cached.");
            } else {
                logger.info("No users found for name query");
            }
            return users;
        } catch (Exception e) {
            logger.error("Error retrieving users by name", e);
            return List.of(); // Return an empty list in case of error
        }
    }

    public List<User> searchUsersByFamilyName(String familyName) {
        try {
            List<User> users = userFamilyNameCache.getIfPresent(familyName.toLowerCase());
            if (users != null && !users.isEmpty()) {
                logger.info("Cache hit for family name query");
                return users;
            }

            users = userRepository.findByFamilyNameContainingIgnoreCase(familyName);
            if (users != null && !users.isEmpty()) {
                userFamilyNameCache.put(familyName.toLowerCase(), users);
                logger.info("Cache miss for family name query. Fetched from database and cached.");
            } else {
                logger.info("No users found for family name query");
            }
            return users;
        } catch (Exception e) {
            logger.error("Error retrieving users by family name", e);
            return List.of(); // Return an empty list in case of error
        }
    }

    public User getUserById(Long id) {
        try {
            User user = userIdCache.getIfPresent(id);
            if (user != null) {
                logger.info("Cache hit for ID query");
                return user;
            }

            user = userRepository.findById(id).orElse(null);
            if (user != null) {
                userIdCache.put(id, user);
                logger.info("Cache miss for ID query. Fetched from database and cached.");
            } else {
                logger.info("No user found for ID query");
            }
            return user;
        } catch (Exception e) {
            logger.error("Error retrieving user by ID", e);
            return null;
        }
    }

    public List<User> getAllUsers() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            logger.error("Error retrieving all users", e);
            return List.of(); // Return an empty list in case of error
        }
    }

    public List<User> searchUsersByQuery(String query) {
        try {
            // Search all fields
            List<User> usersByEmail = getUsersByEmail(query);
            List<User> usersByUsername = searchUsersByUsername(query);
            List<User> usersByName = searchUsersByName(query);
            List<User> usersByFamilyName = searchUsersByFamilyName(query);

            // Combine results and remove duplicates
            Set<User> allUsers = new HashSet<>();
            allUsers.addAll(usersByEmail);
            allUsers.addAll(usersByUsername);
            allUsers.addAll(usersByName);
            allUsers.addAll(usersByFamilyName);

            return allUsers.stream().collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving users by query", e);
            return List.of(); // Return an empty list in case of error
        }
    }
}