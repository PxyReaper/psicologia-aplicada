package com.tfg.proyectolibreria.psicologiaAplicada.users.repository;

import com.tfg.proyectolibreria.psicologiaAplicada.users.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<UsersEntity, Long> {

    Optional<UsersEntity> findByEmail(String email);
}
