package com.brenner.modern_java_crud.mapper;

import com.brenner.modern_java_crud.domain.Project;
import com.brenner.modern_java_crud.dto.ProjectCreateDto;
import com.brenner.modern_java_crud.dto.ProjectDto;
import com.brenner.modern_java_crud.dto.ProjectNextStepDto;
import com.brenner.modern_java_crud.dto.ProjectUpdateDto;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProjectMapper {

    ProjectDto from(Project entity);

    Project from(ProjectCreateDto dto);

    Project from(ProjectUpdateDto dto);

    void merge(ProjectUpdateDto dto, @MappingTarget Project entity);

    void merge(ProjectNextStepDto dto, @MappingTarget Project entity);

}
