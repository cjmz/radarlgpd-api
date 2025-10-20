package com.br.radarlgpd.radarlgpd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade JPA para armazenar detalhes agregados dos dados encontrados.
 * Cada registro representa uma contagem de um tipo de dado em uma localização.
 */
@Entity
@Table(name = "data_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_result_id", nullable = false)
    private ScanResult scanResult;

    @Column(nullable = false, length = 50)
    private String dataType;

    @Column(nullable = false, length = 255)
    private String sourceLocation;

    @Column(nullable = false)
    private Integer count;
}
