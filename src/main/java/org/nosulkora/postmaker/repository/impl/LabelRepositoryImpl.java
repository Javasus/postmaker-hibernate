package org.nosulkora.postmaker.repository.impl;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.nosulkora.postmaker.database.DatabaseManager;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.repository.LabelRepository;
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
        Transaction transaction = null;
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(label);
            transaction.commit();
            return label;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Не удалось сохранить лейбл в БД: {}", label.getName());
            return null;
        }
    }

    @Override
    public Label update(Label label) {
        Transaction transaction = null;
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Label updateLabel = session.merge(label);
            transaction.commit();
            return updateLabel;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Ошибка обновления лейбла.", e);
            return null;
        }
    }

    @Override
    public Label getById(Long id) {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            Label label = session.get(Label.class, id);
            if (label != null && label.getStatus() == Status.ACTIVE) {
                return label;
            } else {
                logger.error("Лейбл по ID {} не найден.", id);
                return null;
            }
        } catch (Exception e) {
            logger.error("Не удалось вернуть лейбл по ID: {}", id, e);
            return null;
        }
    }

    @Override
    public List<Label> getAll() {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            Query<Label> query = session.createQuery(HQL_GET_ALL_LABELS, Label.class);
            query.setParameter("status", Status.ACTIVE);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Ошибка при возврате всех лейблов.", e);
            return null;
        }
    }

    @Override
    public boolean deleteById(Long id) {
        Transaction transaction = null;
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Label label = session.get(Label.class, id);
            if (label != null) {
                label.setStatus(Status.DELETED);
                session.merge(label);
                transaction.commit();
                return true;
            } else {
                logger.error("Лейбл с ID " + id + " не найден для удаления.");
                return false;
            }
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Лейбл с ID " + id + " не удалось удалить.");
            return false;
        }
    }
}
