package com.brenner.modern_java_crud.repository;

import com.brenner.modern_java_crud.domain.Project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project>, ProjectRepositoryCustom {}
