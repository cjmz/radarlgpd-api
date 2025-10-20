package com.br.radarlgpd.radarlgpd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Entidade JPA para persistir scan results agregados.
 * Armazena apenas contagens e metadados, NUNCA dados pessoais.
 */
@Entity
@Table(name = "scan_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String scanId;

    @Column(nullable = false, length = 64)
    private String siteId;

    @Column(nullable = false)
    private Boolean consentGiven;

    @Column(nullable = false)
    private OffsetDateTime scanTimestampUtc;

    @Column(nullable = false)
    private Integer scanDurationMs;

    @Column(nullable = false, length = 50)
    private String scannerVersion;

    @Column(length = 50)
    private String wpVersion;

    @Column(length = 50)
    private String phpVersion;

    @Column(nullable = false)
    private OffsetDateTime receivedAt;

    // Os resultados detalhados serão armazenados em outra tabela relacionada
}
