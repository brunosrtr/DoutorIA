package com.doutor.repository;

import com.doutor.domain.entity.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, UUID> {
}
