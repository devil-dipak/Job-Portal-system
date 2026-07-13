package com.devSoft.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devSoft.Model.User;

public interface UserRepository extends JpaRepository<User, Long>{

	   Optional<User> findFirstByEmail(String email);

	   List<User> findByRole(String role);
}
