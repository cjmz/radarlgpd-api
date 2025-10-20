package com.br.radarlgpd.radarlgpd.repository;

import com.br.radarlgpd.radarlgpd.entity.ScanResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para operações de persistência de ScanResult.
 */
@Repository
public interface ScanResultRepository extends JpaRepository<ScanResult, Long> {
    
    /**
     * Busca um scan result pelo scanId.
     */
    Optional<ScanResult> findByScanId(String scanId);
    
    /**
     * Verifica se já existe um scan com o scanId informado.
     */
    boolean existsByScanId(String scanId);
}
