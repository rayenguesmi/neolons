package com.neo.app.service;

import com.neo.app.documents.UserEntity;
import com.neo.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    public UserEntity createUser(UserEntity user) {
        validateUserCreation(user);

        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());

        UserEntity savedUser = userRepository.save(user);

        auditLogService.logAction(getCurrentUserId(),
                "CREATE_USER", "users", savedUser.getId(), null,
                "User created: " + savedUser.getUsername());

        return savedUser;
    }

    public UserEntity updateUser(String userId, UserEntity updatedUser) {
        Optional<UserEntity> existingUserOpt = userRepository.findById(userId);
        if (!existingUserOpt.isPresent()) {
            throw new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId);
        }

        UserEntity existingUser = existingUserOpt.get();

        validateUserUpdate(existingUser, updatedUser);

        if (updatedUser.getFirstName() != null) {
            existingUser.setFirstName(updatedUser.getFirstName());
        }
        if (updatedUser.getLastName() != null) {
            existingUser.setLastName(updatedUser.getLastName());
        }
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getRoles() != null) {
            existingUser.setRoles(updatedUser.getRoles());
        }
        if (updatedUser.getProjects() != null) {
            existingUser.setProjects(updatedUser.getProjects());
        }
        if (updatedUser.getIsActive() != null) {
            existingUser.setIsActive(updatedUser.getIsActive());
        }

        existingUser.setUpdatedAt(new Date());

        UserEntity savedUser = userRepository.save(existingUser);

        auditLogService.logAction(getCurrentUserId(),
                "UPDATE_USER", "users", savedUser.getId(), 
                existingUser.toString(), "User updated: " + savedUser.getUsername());

        return savedUser;
    }

    public void deleteUser(String userId) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId);
        }

        UserEntity user = userOpt.get();

        if (user.getRoles().contains("ADMIN_GENERAL")) {
            long adminCount = userRepository.countAdministrators();
            if (adminCount <= 1) {
                throw new RuntimeException("Impossible de supprimer le dernier administrateur général");
            }
        }

        userRepository.deleteById(userId);

        auditLogService.logAction(getCurrentUserId(),
                "DELETE_USER", "users", userId,
                user.toString(), "User deleted: " + user.getUsername());
    }

    public Optional<UserEntity> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    public Optional<UserEntity> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<UserEntity> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<UserEntity> getUserByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId);
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public List<UserEntity> getActiveUsers() {
        return userRepository.findByIsActive(true);
    }

    public List<UserEntity> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    public List<UserEntity> getUsersByProject(String projectId) {
        return userRepository.findByProject(projectId);
    }

    public List<UserEntity> getAdministrators() {
        return userRepository.findAllAdministrators();
    }

    public List<UserEntity> getProjectManagers(String projectId) {
        return userRepository.findProjectManagers(projectId);
    }

    public List<UserEntity> getExecutors(String projectId) {
        return userRepository.findExecutors(projectId);
    }

    public void updateLastLogin(String userId) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            user.setLastLogin(new Date());
            userRepository.save(user);
        }
    }

    public UserEntity toggleUserStatus(String userId) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId);
        }

        UserEntity user = userOpt.get();
        user.setIsActive(!user.getIsActive());
        user.setUpdatedAt(new Date());

        UserEntity savedUser = userRepository.save(user);

        String action = savedUser.getIsActive() ? "ACTIVATE" : "DEACTIVATE";
        auditLogService.logAction(getCurrentUserId(), getCurrentUsername(),
                action, "USER", savedUser.getId(),
                "User " + action.toLowerCase() + "d: " + savedUser.getUsername());

        return savedUser;
    }

    private void validateUserCreation(UserEntity user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Le nom d'utilisateur est obligatoire");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new RuntimeException("L'email est obligatoire");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Un utilisateur avec ce nom d'utilisateur existe déjà");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new RuntimeException("Au moins un rôle doit être assigné");
        }
    }

    private void validateUserUpdate(UserEntity existingUser, UserEntity updatedUser) {
        if (updatedUser.getRoles() != null && !updatedUser.getRoles().equals(existingUser.getRoles())) {
            if (!hasAdminPermission()) {
                throw new RuntimeException("Seuls les administrateurs peuvent modifier les rôles");
            }
        }
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new RuntimeException("Un utilisateur avec cet email existe déjà");
            }
        }
    }

    private boolean hasAdminPermission() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) return false;

        Optional<UserEntity> currentUserOpt = userRepository.findById(currentUserId);
        if (!currentUserOpt.isPresent()) return false;

        UserEntity currentUser = currentUserOpt.get();
        return currentUser.getRoles().contains("ADMIN_GENERAL");
    }

    private String getCurrentUserId() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }

    private String getCurrentUsername() {
        try {
            String userId = getCurrentUserId();
            Optional<UserEntity> userOpt = userRepository.findById(userId);
            return userOpt.map(UserEntity::getUsername).orElse("system");
        } catch (Exception e) {
            return "system";
        }
    }
}
