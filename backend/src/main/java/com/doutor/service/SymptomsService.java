package com.doutor.service;

import com.doutor.dto.response.SymptomCatalogResponse;

import java.util.List;

public interface SymptomsService {

    List<SymptomCatalogResponse> search(String query, String categoria);
}
