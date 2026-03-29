package com.brenner.modern_java_crud.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.brenner.modern_java_crud.config.SecurityConfig;
import com.brenner.modern_java_crud.config.UserConfig;
import com.brenner.modern_java_crud.dto.ProjectCreateDto;
import com.brenner.modern_java_crud.dto.ProjectDto;
import com.brenner.modern_java_crud.dto.ProjectNextStepDto;
import com.brenner.modern_java_crud.dto.ProjectUpdateDto;
import com.brenner.modern_java_crud.service.ProjectService;

import java.math.BigDecimal;
import java.util.List;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProjectController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@EnableConfigurationProperties(UserConfig.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService service;

    @Test
    @WithMockUser
    void findAll_shouldReturn200_withPageOfProjects() throws Exception {
        final var dto = Instancio.create(ProjectDto.class);
        when(service.findAll(any(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/projects").accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void findAll_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/projects").accept(APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void find_shouldReturn200_withProject() throws Exception {
        final var dto = Instancio.create(ProjectDto.class);
        when(service.find(dto.id())).thenReturn(dto);

        mockMvc
            .perform(get("/projects/{id}", dto.id()).accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(dto.id()));
    }

    @Test
    @WithMockUser
    void create_shouldReturn201_withCreatedProject() throws Exception {
        final var createDto = Instancio.of(ProjectCreateDto.class)
            .generate(
                field(ProjectCreateDto::name),
                gen -> gen.string().alphaNumeric().length(10)
            )
            .generate(
                field(ProjectCreateDto::totalBudget),
                gen -> gen.math().bigDecimal().min(BigDecimal.ONE).scale(2)
            )
            .generate(
                field(ProjectCreateDto::members),
                gen -> gen.collection().size(1)
            )
            .create();
        final var responseDto = Instancio.create(ProjectDto.class);

        when(service.create(any(ProjectCreateDto.class)))
            .thenReturn(responseDto);

        mockMvc
            .perform(
                post("/projects").contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDto))
                    .accept(APPLICATION_JSON)
            )
            .andExpect(status().isCreated())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(responseDto.id()));
    }

    @Test
    @WithMockUser
    void create_shouldReturn400_whenBodyIsInvalid() throws Exception {
        mockMvc
            .perform(
                post("/projects").contentType(APPLICATION_JSON)
                    .content("{}")
                    .accept(APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void update_shouldReturn200_withUpdatedProject() throws Exception {
        final var id = 1L;
        final var updateDto = Instancio.of(ProjectUpdateDto.class)
            .generate(
                field(ProjectUpdateDto::name),
                gen -> gen.string().alphaNumeric().length(10)
            )
            .generate(
                field(ProjectUpdateDto::totalBudget),
                gen -> gen.math().bigDecimal().min(BigDecimal.ONE).scale(2)
            )
            .generate(
                field(ProjectUpdateDto::members),
                gen -> gen.collection().size(1)
            )
            .create();
        final var responseDto = Instancio.create(ProjectDto.class);

        when(service.update(eq(id), any(ProjectUpdateDto.class)))
            .thenReturn(responseDto);

        mockMvc
            .perform(
                put("/projects/{id}", id).contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto))
                    .accept(APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(responseDto.id()));
    }

    @Test
    @WithMockUser
    void update_shouldReturn400_whenBodyIsInvalid() throws Exception {
        mockMvc
            .perform(
                put("/projects/{id}", 1L).contentType(APPLICATION_JSON)
                    .content("{}")
                    .accept(APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void delete_shouldReturn204() throws Exception {
        final var id = 1L;
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/projects/{id}", id))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void advanceStep_shouldReturn200_withUpdatedProject() throws Exception {
        final var id = 1L;
        final var nextStepDto = Instancio.create(ProjectNextStepDto.class);
        final var responseDto = Instancio.create(ProjectDto.class);

        when(service.advanceStep(eq(id), any(ProjectNextStepDto.class)))
            .thenReturn(responseDto);

        mockMvc.perform(
            patch("/projects/{id}/next-step", id).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nextStepDto))
                .accept(APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(responseDto.id()));
    }

    @Test
    @WithMockUser
    void cancel_shouldReturn200_withCancelledProject() throws Exception {
        final var id = 1L;
        final var responseDto = Instancio.create(ProjectDto.class);

        when(service.cancel(id)).thenReturn(responseDto);

        mockMvc
            .perform(
                patch("/projects/{id}/cancel", id).accept(APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(responseDto.id()));
    }

}
