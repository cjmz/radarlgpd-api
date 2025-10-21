package com.br.radarlgpd.radarlgpd.repository;

import com.br.radarlgpd.radarlgpd.entity.Instance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para operações de persistência de instâncias do plugin.
 */
@Repository
public interface InstanceRepository extends JpaRepository<Instance, Long> {

    /**
     * Busca uma instância pelo token de autenticação.
     * Usado no Fluxo Autenticado (RF-API-2.1).
     * 
     * @param instanceToken token UUIDv4 da instância
     * @return Optional contendo a instância se encontrada
     */
    Optional<Instance> findByInstanceToken(String instanceToken);

    /**
     * Verifica se já existe uma instância com o token informado.
     * Usado para garantir unicidade durante geração de novos tokens.
     * 
     * @param instanceToken token a verificar
     * @return true se o token já existe
     */
    boolean existsByInstanceToken(String instanceToken);

    /**
     * Busca instâncias pelo hash do site.
     * Útil para análise de múltiplas instalações no mesmo domínio.
     * 
     * @param siteId hash SHA256 do domínio
     * @return lista de instâncias do mesmo site
     */
    java.util.List<Instance> findBySiteId(String siteId);
}
