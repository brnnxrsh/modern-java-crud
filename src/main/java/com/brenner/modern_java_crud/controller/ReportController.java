package com.brenner.modern_java_crud.controller;

import com.brenner.modern_java_crud.dto.ReportDto;
import com.brenner.modern_java_crud.service.ReportService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService service;

    @GetMapping
    public ReportDto getSummary() {
        log.debug("[REPORT-CONTROLLER] Gerando relatório de resumo");

        return service.getSummary();
    }

}
