package org.nosulkora.postmaker.repository.impl;

import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.repository.WriterRepository;
import org.nosulkora.postmaker.utils.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class WriterRepositoryImpl implements WriterRepository {
    private static final Logger logger = LoggerFactory.getLogger(WriterRepositoryImpl.class);

    private static final String HQL_GET_WRITER_BY_ID = """
            SELECT w
            FROM Writer w
            LEFT JOIN FETCH w.posts p
            WHERE w.id = :id AND w.status = :status
            ORDER BY p.id DESC""";

    private static final String HQL_GET_ALL_WRITERS = """
            SELECT w
            FROM Writer w
            LEFT JOIN FETCH w.posts p
            WHERE w.status = :status
            ORDER BY w.id, p.id DESC""";

    @Override
    public Writer save(Writer writer) {
        return SessionManager.execute(session -> session.merge(writer));
    }

    @Override
    public Writer update(Writer writer) {
        return save(writer);
    }

    @Override
    public Writer getById(Long id) {
        return SessionManager.executeReadOnly(session ->
                session.createQuery(HQL_GET_WRITER_BY_ID, Writer.class)
                        .setParameter("id", id)
                        .setParameter("status", Status.ACTIVE)
                        .uniqueResult()
        );
    }

    @Override
    public List<Writer> getAll() {
        return SessionManager.executeReadOnly(session ->
                session.createQuery(HQL_GET_ALL_WRITERS, Writer.class)
                        .setParameter("status", Status.ACTIVE)
                        .getResultList()
        );
    }

    @Override
    public boolean deleteById(Long id) {
        try {
            boolean deleted = Boolean.TRUE.equals(SessionManager.execute(session -> {
                        Writer writer = session.createQuery(HQL_GET_WRITER_BY_ID, Writer.class)
                                .setParameter("id", id)
                                .setParameter("status", Status.ACTIVE)
                                .uniqueResult();
                        if (writer != null) {
                            writer.setStatus(Status.DELETED);
                            return true;
                        }
                        return false;
                    })
            );
            if (Boolean.TRUE.equals(deleted)) {
                logger.info("Писатель с ID {} помечен как удаленный", id);
                return true;
            } else {
                logger.warn("Писатель с ID {} не найден или уже удален", id);
                return false;
            }
        } catch (
                Exception e) {
            logger.error("Ошибка при удалении писателя с ID {}", id, e);
            return false;
        }
    }
}
