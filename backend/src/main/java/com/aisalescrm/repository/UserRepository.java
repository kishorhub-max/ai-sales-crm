package com.aisalescrm.repository;

import com.aisalescrm.entity.User;
import com.aisalescrm.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByActiveTrue();

    @Query("SELECT u FROM User u WHERE u.active = true AND u.role IN ('SALES_MANAGER','SALES_REPRESENTATIVE')")
    List<User> findAllSalesUsers();
}