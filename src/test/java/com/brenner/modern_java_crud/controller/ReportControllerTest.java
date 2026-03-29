package com.brenner.modern_java_crud.controller;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.brenner.modern_java_crud.config.SecurityConfig;
import com.brenner.modern_java_crud.config.UserConfig;
import com.brenner.modern_java_crud.dto.ReportDto;
import com.brenner.modern_java_crud.service.ReportService;

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

@WebMvcTest(ReportController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@EnableConfigurationProperties(UserConfig.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService service;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSummary_shouldReturn200_whenAdmin() throws Exception {
        final var dto = Instancio.create(ReportDto.class);
        when(service.getSummary()).thenReturn(dto);

        mockMvc.perform(get("/reports").accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(
                jsonPath("$.averageDurationInDays")
                    .value(dto.averageDurationInDays())
            )
            .andExpect(
                jsonPath("$.countUniqueMembersAllocated")
                    .value(dto.countUniqueMembersAllocated())
            )
            .andExpect(
                jsonPath("$.countUniqueManagersAllocated")
                    .value(dto.countUniqueManagersAllocated())
            );
    }

    @Test
    @WithMockUser(roles = "USER")
    void getSummary_shouldReturn403_whenUser() throws Exception {
        mockMvc.perform(get("/reports").accept(APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void getSummary_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/reports").accept(APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

}
