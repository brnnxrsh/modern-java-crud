package com.brenner.modern_java_crud.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.brenner.modern_java_crud.config.SecurityConfig;
import com.brenner.modern_java_crud.config.UserConfig;
import com.brenner.modern_java_crud.dto.MemberExternalCreateDto;
import com.brenner.modern_java_crud.dto.MemberExternalDto;
import com.brenner.modern_java_crud.service.MemberService;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(MemberController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@EnableConfigurationProperties(UserConfig.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService service;

    @Test
    @WithMockUser
    void findById_shouldReturn200_withMember() throws Exception {
        final var dto = Instancio.create(MemberExternalDto.class);
        when(service.findById(dto.id())).thenReturn(dto);

        mockMvc.perform(get("/members/{id}", dto.id()).accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(dto.id()))
            .andExpect(jsonPath("$.name").value(dto.name()))
            .andExpect(jsonPath("$.role").value(dto.role().name()));
    }

    @Test
    void findById_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/members/{id}", 1L).accept(APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void create_shouldReturn201_withCreatedMember() throws Exception {
        final var createDto = Instancio.create(MemberExternalCreateDto.class);
        final var responseDto = Instancio.create(MemberExternalDto.class);
        when(service.create(any(MemberExternalCreateDto.class)))
            .thenReturn(responseDto);

        mockMvc
            .perform(
                post("/members").contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDto))
                    .accept(APPLICATION_JSON)
            )
            .andExpect(status().isCreated())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(responseDto.id()))
            .andExpect(jsonPath("$.name").value(responseDto.name()))
            .andExpect(jsonPath("$.role").value(responseDto.role().name()));
    }

    @Test
    void create_shouldReturn401_whenUnauthenticated() throws Exception {
        final var createDto = Instancio.create(MemberExternalCreateDto.class);

        mockMvc
            .perform(
                post("/members").contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDto))
                    .accept(APPLICATION_JSON)
            )
            .andExpect(status().isUnauthorized());
    }

}
