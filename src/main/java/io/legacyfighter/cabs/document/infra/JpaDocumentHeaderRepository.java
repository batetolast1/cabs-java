package io.legacyfighter.cabs.document.infra;

import io.legacyfighter.cabs.document.model.DocumentHeader;
import io.legacyfighter.cabs.document.model.DocumentHeaderRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

@Repository
public class JpaDocumentHeaderRepository<T extends DocumentHeader> implements DocumentHeaderRepository<T> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public T getOne(Long id, Class<T> clazz) {
        return entityManager.find(clazz, id, LockModeType.OPTIMISTIC);
    }

    @Override
    public void save(T header) {
        if (entityManager.contains(header)) {
            entityManager.lock(header, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        } else {
            entityManager.persist(header);
        }
    }
}
