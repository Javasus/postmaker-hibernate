package org.nosulkora.postmaker.repository.impl;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.nosulkora.postmaker.database.DatabaseManager;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.model.Writer;
import org.nosulkora.postmaker.repository.WriterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private static final String HQL_GET_POSTS_WITH_LABELS_BY_WRITER = """
            SELECT DISTINCT p
            FROM Post p
            LEFT JOIN FETCH p.labels l
            WHERE p.writerId = :writerId AND p.status = :status
            ORDER BY p.id DESC""";

    @Override
    public Writer save(Writer writer) {
        Transaction transaction = null;
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(writer);
            transaction.commit();
            return writer;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Не удалось сохранить писателя в БД: {}", writer, e);
            return null;
        }
    }

    @Override
    public Writer update(Writer writer) {
        Transaction transaction = null;
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Writer updateWriter = session.merge(writer);
            transaction.commit();
            return updateWriter;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Ошибка обновления писателя с ID {}", writer.getId(), e);
            return null;
        }
    }

    @Override
    public Writer getById(Long id) {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            Writer writer = session.createQuery(HQL_GET_WRITER_BY_ID, Writer.class)
                    .setParameter("id", id)
                    .setParameter("status", Status.ACTIVE)
                    .uniqueResult();
            if (writer == null) {
                logger.warn("Писатель с ID {} не найден", id);
                return null;
            }
            return loadLabelsForWriterPosts(session, writer);
        } catch (Exception e) {
            logger.error("Не удалось вернуть писателя по ID: {}", id, e);
            return null;
        }
    }

    @Override
    public List<Writer> getAll() {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            List<Writer> resultWriters = new ArrayList<>();
            List<Writer> writers = session.createQuery(HQL_GET_ALL_WRITERS, Writer.class)
                    .setParameter("status", Status.ACTIVE)
                    .getResultList();
            for (Writer writer : writers) {
                resultWriters.add(loadLabelsForWriterPosts(session, writer));
            }
            if (resultWriters.isEmpty()) {
                return null;
            }
            return resultWriters;
        } catch (Exception e) {
            logger.error("Ошибка при возврате всех писателей", e);
            return null;
        }
    }

    @Override
    public boolean deleteById(Long id) {
        Transaction transaction = null;
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Writer writer = session.get(Writer.class, id);
            if (writer != null) {
                writer.setStatus(Status.DELETED);
                session.merge(writer);
                return true;
            } else {
                logger.error("Писатель с ID {} не найден для удаления", id);
                return false;
            }
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Ошибка удаления писателя с ID {}", id, e);
            return false;
        }
    }

    /**
     * Добавляет Label в Post писателя.
     */
    private Writer loadLabelsForWriterPosts(Session session, Writer writer) {
        if (writer.getPosts() == null || writer.getPosts().isEmpty()) {
            return writer;
        }

        List<Post> postsWithLabels = session.createQuery(HQL_GET_POSTS_WITH_LABELS_BY_WRITER, Post.class)
                .setParameter("writerId", writer.getId())
                .setParameter("status", Status.ACTIVE)
                .getResultList();

        Map<Long, Post> postsMap = postsWithLabels.stream()
                .collect(Collectors.toMap(Post::getId, post -> post));

        writer.getPosts().forEach(post -> {
            Post postWithLabels = postsMap.get(post.getId());
            if (postWithLabels != null) {
                post.setLabels(postWithLabels.getLabels());
            }
        });
        return writer;
    }
}
