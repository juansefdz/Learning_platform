package com.simulacro.aprendizaje.domain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.simulacro.aprendizaje.domain.entities.Enrollment;

public interface EnrrollmentRepository extends JpaRepository<Enrollment, Long> {

}
