package com.Zorvyn.Finance_Dashboard.service;

import com.Zorvyn.Finance_Dashboard.dto.CreateUserRequest;
import com.Zorvyn.Finance_Dashboard.dto.UpdateUserPasswordRequest;
import com.Zorvyn.Finance_Dashboard.dto.UpdateUserRequest;
import com.Zorvyn.Finance_Dashboard.dto.UserResponse;
import com.Zorvyn.Finance_Dashboard.exception.ConflictException;
import com.Zorvyn.Finance_Dashboard.exception.ResourceNotFoundException;
import com.Zorvyn.Finance_Dashboard.model.AppUser;
import com.Zorvyn.Finance_Dashboard.model.Role;
import com.Zorvyn.Finance_Dashboard.repository.AppUserRepository;
import com.Zorvyn.Finance_Dashboard.repository.RoleRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers() {
        return appUserRepository.findAllByDeletedFalse()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        validateUsernameUniqueness(request.username(), null);
        validateEmailUniqueness(request.email(), null);

        AppUser user = new AppUser();
        user.setName(request.name().trim());
        user.setUsername(request.username().trim().toLowerCase());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(resolveRoles(request.roles()));
        user.setStatus(request.status());
        return toResponse(appUserRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        AppUser user = getEntity(id);
        validateUsernameUniqueness(request.username(), id);
        validateEmailUniqueness(request.email(), id);

        user.setName(request.name().trim());
        user.setUsername(request.username().trim().toLowerCase());
        user.setEmail(request.email().trim().toLowerCase());
        user.setRoles(resolveRoles(request.roles()));
        user.setStatus(request.status());
        return toResponse(appUserRepository.save(user));
    }

    @Transactional
    public void updatePassword(Long id, UpdateUserPasswordRequest request) {
        AppUser user = getEntity(id);
        user.setPassword(passwordEncoder.encode(request.password()));
        appUserRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        AppUser user = getEntity(id);
        user.setDeleted(true);
        user.setStatus(com.Zorvyn.Finance_Dashboard.model.UserStatus.INACTIVE);
        appUserRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> getUserEntityByUsername(String username) {
        return appUserRepository.findByUsernameIgnoreCaseAndDeletedFalse(username);
    }

    private void validateUsernameUniqueness(String username, Long existingUserId) {
        appUserRepository.findByUsernameIgnoreCaseAndDeletedFalse(username.trim())
                .filter(user -> !user.getId().equals(existingUserId))
                .ifPresent(user -> {
                    throw new ConflictException("Username is already in use");
                });
    }

    private void validateEmailUniqueness(String email, Long existingUserId) {
        appUserRepository.findByEmailIgnoreCaseAndDeletedFalse(email.trim())
                .filter(user -> !user.getId().equals(existingUserId))
                .ifPresent(user -> {
                    throw new ConflictException("Email is already in use");
                });
    }

    private AppUser getEntity(Long id) {
        return appUserRepository.findById(id)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    private Set<Role> resolveRoles(Set<String> requestedRoles) {
        Set<String> normalizedRoles = requestedRoles.stream()
                .map(role -> role.trim().toUpperCase())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        List<Role> roles = roleRepository.findByNameIn(normalizedRoles);
        if (roles.size() != normalizedRoles.size()) {
            throw new IllegalArgumentException("One or more roles are invalid");
        }
        return new LinkedHashSet<>(roles);
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)),
                user.getStatus()
        );
    }
}
