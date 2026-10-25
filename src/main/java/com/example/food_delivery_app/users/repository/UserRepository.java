package com.example.food_delivery_app.users.repository;

import com.example.food_delivery_app.users.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("""
       select distinct u from User u
       left join fetch u.roleEntities r
       left join fetch r.permissions
       where u.email = :email
       """)
    Optional<User> findByEmailWithAuthorities(@Param("email") String email);

    @Query("""
       select distinct u from User u
       left join fetch u.roleEntities r
       left join fetch r.permissions
       where u.id = :id
       """)
    Optional<User> findByIdWithAuthorities(@Param("id") Integer id);

}
