package com.br.radarlgpd.radarlgpd.repository;

import com.br.radarlgpd.radarlgpd.entity.DataResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository para operações de persistência de DataResultEntity.
 */
@Repository
public interface DataResultRepository extends JpaRepository<DataResultEntity, Long> {
}
