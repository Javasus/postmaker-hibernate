package org.nosulkora.postmaker.repository.impl;

import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.repository.LabelRepository;
import org.nosulkora.postmaker.utils.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LabelRepositoryImpl implements LabelRepository {
    private static final Logger logger = LoggerFactory.getLogger(LabelRepositoryImpl.class);

    private static final String HQL_GET_ALL_LABELS = """
            FROM Label
            WHERE status = :status""";

    @Override
    public Label save(Label label) {
        return SessionManager.execute(session -> session.merge(label));
    }

    @Override
    public Label update(Label label) {
        return save(label);
    }

    @Override
    public Label getById(Long id) {
        return SessionManager.executeReadOnly(session -> {
                    Label label = session.get(Label.class, id);
                    if (label != null && label.getStatus() == Status.ACTIVE) {
                        return label;
                    } else {
                        logger.error("Лейбл по ID {} не найден.", id);
                        return null;
                    }
                }
        );
    }

    @Override
    public List<Label> getAll() {
        return SessionManager.executeReadOnly(session ->
                session.createQuery(HQL_GET_ALL_LABELS, Label.class)
                        .setParameter("status", Status.ACTIVE)
                        .getResultList()
        );
    }

    @Override
    public boolean deleteById(Long id) {
        try {
            boolean deleted = Boolean.TRUE.equals(SessionManager.execute(session -> {
                Label label = session.get(Label.class, id);

                if (label != null && label.getStatus() == Status.ACTIVE) {
                    label.setStatus(Status.DELETED);
                    return true;
                }
                return false;
            }));
            if (Boolean.TRUE.equals(deleted)) {
                logger.info("Лейбл с ID {} помечен как удаленный", id);
                return true;
            } else {
                logger.warn("Лейбл с ID {} не найден или уже удален", id);
                return false;
            }
        } catch (
                Exception e) {
            logger.error("Ошибка при удалении Лейбла с ID {}", id, e);
            return false;
        }
    }
}
