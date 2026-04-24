package com.doutor.controller;

import com.doutor.dto.response.SymptomCatalogResponse;
import com.doutor.service.SymptomsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/symptoms")
@RequiredArgsConstructor
public class SymptomsController {

    private final SymptomsService symptomsService;

    @GetMapping
    public ResponseEntity<List<SymptomCatalogResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoria) {
        return ResponseEntity.ok(symptomsService.search(q, categoria));
    }
}
