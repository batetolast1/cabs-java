package io.legacyfighter.cabs.document.model;

public interface DocumentHeaderRepository<T extends DocumentHeader> {

    T getOne(Long id, Class<T> clazz);

    void save(T header);
}
