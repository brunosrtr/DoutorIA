package com.doutor.service;

import com.doutor.dto.response.HealthSummaryResponse;

public interface HealthSummaryService {

    HealthSummaryResponse getSummary(boolean forceRefresh);
}
