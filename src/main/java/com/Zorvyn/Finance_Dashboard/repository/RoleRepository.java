package com.Zorvyn.Finance_Dashboard.repository;

import com.Zorvyn.Finance_Dashboard.model.Role;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    List<Role> findByNameIn(Collection<String> names);
}
