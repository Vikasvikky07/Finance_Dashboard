package com.Zorvyn.Finance_Dashboard.repository;

import com.Zorvyn.Finance_Dashboard.model.AppUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsernameIgnoreCaseAndDeletedFalse(String username);

    Optional<AppUser> findByEmailIgnoreCaseAndDeletedFalse(String email);

    List<AppUser> findAllByDeletedFalse();
}
