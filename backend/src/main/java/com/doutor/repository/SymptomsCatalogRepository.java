package com.doutor.repository;

import com.doutor.domain.entity.SymptomsCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SymptomsCatalogRepository extends JpaRepository<SymptomsCatalog, UUID> {

    @Query("SELECT s FROM SymptomsCatalog s WHERE LOWER(s.nome) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY s.nome")
    List<SymptomsCatalog> findByNomeContainingIgnoreCase(@Param("query") String query);

    @Query("SELECT s FROM SymptomsCatalog s WHERE LOWER(s.nome) LIKE LOWER(CONCAT('%', :query, '%')) AND s.categoria = :categoria ORDER BY s.nome")
    List<SymptomsCatalog> findByNomeContainingIgnoreCaseAndCategoria(@Param("query") String query, @Param("categoria") String categoria);
}
