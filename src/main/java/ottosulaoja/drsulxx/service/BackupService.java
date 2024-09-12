package ottosulaoja.drsulxx.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ottosulaoja.drsulxx.model.usermanagement.User;
import ottosulaoja.drsulxx.repository.backup.BackupUserRepository;
import ottosulaoja.drsulxx.repository.usermanagement.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class BackupService {

    @Autowired
    private UserRepository userRepository;  // Main database repository
    
    @Autowired
    private BackupUserRepository backupUserRepository;  // Backup database repository

    /**
     * Scheduled task to back up data every 2 minutes.
     * The cron expression runs the task every 2 minutes.
     */
    @Transactional(transactionManager = "backupTransactionManager")  // Specify backup transaction manager
    @Scheduled(cron = "0 0 0 * * ?")  // Runs every day at midnight
   // @Scheduled(fixedRate = 120000)  // Runs every 2 minutes (120,000 milliseconds)
    
    public void backupData() {
        // Fetch all users that are not marked as deleted from the main database
        List<User> users = userRepository.findByDeletedFalse();
        
        // Log the number of users to back up
        System.out.println("Number of users to backup: " + users.size());
        
        // Backup logic: check if the user exists in the backup database, and update or insert accordingly
        for (User user : users) {
            Optional<User> existingBackupUser = backupUserRepository.findByUsername(user.getUsername());

            if (existingBackupUser.isPresent()) {
                // If the user already exists in the backup database, update their information
                User backupUser = existingBackupUser.get();
                backupUser.setUsername(user.getUsername());
                backupUser.setName(user.getName());
                backupUser.setFamilyName(user.getFamilyName());
                backupUser.setEmail(user.getEmail());
                backupUser.setPassword(user.getPassword());
                backupUser.setCreatedAt(user.getCreatedAt());
                backupUser.setUpdatedAt(user.getUpdatedAt());
                backupUser.setDeleted(user.getDeleted());
                
                // Save the updated user
                backupUserRepository.save(backupUser);
                System.out.println("Updated backup user: " + user.getUsername());
            } else {
                // If the user does not exist in the backup database, insert them
                User newBackupUser = new User();
                newBackupUser.setId(user.getId());  // Keep the same ID
                newBackupUser.setUsername(user.getUsername());
                newBackupUser.setName(user.getName());
                newBackupUser.setFamilyName(user.getFamilyName());
                newBackupUser.setEmail(user.getEmail());
                newBackupUser.setPassword(user.getPassword());
                newBackupUser.setCreatedAt(user.getCreatedAt());
                newBackupUser.setUpdatedAt(user.getUpdatedAt());
                newBackupUser.setDeleted(user.getDeleted());

                // Save the new user
                backupUserRepository.save(newBackupUser);
                System.out.println("Inserted new backup user: " + user.getUsername());
            }
        }
    }
}