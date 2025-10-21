package com.br.radarlgpd.radarlgpd.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Entidade JPA para representar uma instalação anônima do plugin WordPress.
 * Cada instância possui um token único para autenticação futura.
 * 
 * Compliance LGPD:
 * - Não armazena IP do servidor
 * - site_id é um hash SHA256 (anônimo)
 * - instance_token é um UUIDv4 gerado pela API
 */
@Entity
@Table(name = "instances", indexes = {
    @Index(name = "idx_instance_token", columnList = "instance_token", unique = true),
    @Index(name = "idx_site_id", columnList = "site_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Instance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Token único e anônimo para autenticação da instância.
     * UUIDv4 gerado no momento do registro (RF-API-3.1).
     */
    @Column(nullable = false, unique = true, length = 36)
    private String instanceToken;

    /**
     * Hash SHA256 do domínio do site (recebido do plugin).
     * Mantém a anonimização conforme LGPD.
     */
    @Column(nullable = false, length = 64)
    private String siteId;

    /**
     * Timestamp do primeiro registro (primeiro scan enviado).
     */
    @Column(nullable = false)
    private OffsetDateTime createdAt;

    /**
     * Timestamp da última telemetria recebida desta instância.
     */
    @Column
    private OffsetDateTime lastSeenAt;

    /**
     * Status da instância:
     * - 'active': Instância ativa e funcional
     * - 'inactive': Instância inativa (não envia telemetria há muito tempo)
     * - 'banned': Instância banida por comportamento suspeito (RF-API-2.1)
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "active";

    /**
     * Versão do scanner no momento do registro.
     * Útil para análise de compatibilidade.
     */
    @Column(length = 50)
    private String scannerVersionAtRegistration;

    /**
     * Contador de scans recebidos desta instância.
     * Útil para métricas e detecção de anomalias.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer scanCount = 0;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (status == null) {
            status = "active";
        }
        if (scanCount == null) {
            scanCount = 0;
        }
    }

    /**
     * Incrementa o contador de scans e atualiza o timestamp de última atividade.
     */
    public void recordScan() {
        this.scanCount++;
        this.lastSeenAt = OffsetDateTime.now();
    }

    /**
     * Verifica se a instância está ativa e pode enviar telemetria.
     */
    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }

    /**
     * Verifica se a instância está banida.
     */
    public boolean isBanned() {
        return "banned".equalsIgnoreCase(status);
    }
}
