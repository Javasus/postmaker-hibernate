package org.nosulkora.postmaker.repository.impl;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.nosulkora.postmaker.database.DatabaseManager;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PostRepositoryImpl implements PostRepository {
    private static final Logger logger = LoggerFactory.getLogger(PostRepositoryImpl.class);

    private static final String HQL_GET_POST_BY_ID = """
            SELECT DISTINCT p
            FROM Post p
            LEFT JOIN FETCH p.labels l
            WHERE p.id = :id AND p.status = :status""";

    private static final String HQL_GET_ALL_POSTS = """
            SELECT DISTINCT p
            FROM Post p
            LEFT JOIN FETCH p.labels l
            WHERE p.status = :status
            ORDER BY p.id""";

    @Override
    public Post save(Post post) {
        Transaction transaction = null;
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            // Если лейблы из нового поста уже существуют в БД, то добавляем их в список managedLabels,
            // если не существуют, то добавляем их в бд, получаем их id и тоже добалвяем в список. Обновляем список
            // лейблов.
            if (post.getLabels() != null && !post.getLabels().isEmpty()) {
                List<Label> managedLabels = new ArrayList<>();
                for (Label label : post.getLabels()) {
                    if (label.getId() != null) {
                        Label managedLabel = session.get(Label.class, label.getId());
                        if (managedLabel != null) {
                            managedLabels.add(managedLabel);
                        }
                    } else {
                        session.persist(label);
                        managedLabels.add(label);
                    }
                }
                post.setLabels(managedLabels);
            }

            session.persist(post);
            transaction.commit();
            return post;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    logger.warn("Ошибка при откате транзакции: {}", rollbackEx.getMessage());
                }
            }
            logger.error("Не удалось сохранить пост в БД: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Post update(Post post) {
        Transaction transaction = null;
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Post existingPost = session.get(Post.class, post.getId());
            if (existingPost == null) {
                logger.error("Пост с ID {} не найден для обновления", post.getId());
                transaction.rollback();
                return null;
            }

            existingPost.setTitle(post.getTitle());
            existingPost.setContent(post.getContent());

            if (post.getLabels() != null) {
                List<Label> managedLabels = new ArrayList<>();
                for (Label label : post.getLabels()) {
                    if (label.getId() != null) {
                        Label managedLabel = session.get(Label.class, label.getId());
                        if (managedLabel != null) {
                            managedLabels.add(managedLabel);
                        }
                    } else {
                        session.persist(label);
                        managedLabels.add(label);
                    }
                }
                existingPost.setLabels(managedLabels);
            } else {
                existingPost.setLabels(new ArrayList<>());
            }

            Post updatedPost = session.merge(existingPost);
            transaction.commit();
            return updatedPost;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    logger.warn("Ошибка при откате транзакции: {}", rollbackEx.getMessage());
                }
            }
            logger.error("Ошибка обновления поста с ID {}", post.getId(), e);
            return null;
        }
    }

    @Override
    public Post getById(Long id) {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            Post post = session.createQuery(HQL_GET_POST_BY_ID, Post.class)
                    .setParameter("id", id)
                    .setParameter("status", Status.ACTIVE)
                    .uniqueResult();

            if (post == null) {
                logger.warn("Пост с ID {} не найден", id);
                return null;
            }
            return post;
        } catch (Exception e) {
            logger.error("Не удалось вернуть пост по ID: {}", id, e);
            return null;
        }
    }

    @Override
    public List<Post> getAll() {
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            return session.createQuery(HQL_GET_ALL_POSTS, Post.class)
                    .setParameter("status", Status.ACTIVE)
                    .getResultList();
        } catch (Exception e) {
            logger.error("Ошибка при возврате всех постов.", e);
            return null;
        }
    }

    @Override
    public boolean deleteById(Long id) {
        Transaction transaction = null;
        try (Session session = DatabaseManager.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Post post = session.get(Post.class, id);
            if (post != null) {
                post.setStatus(Status.DELETED);
                session.merge(post);
                transaction.commit();
                return true;
            } else {
                logger.error("Пост с ID {} не найден для удаления.", id);
                transaction.rollback();
                return false;
            }
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    logger.warn("Ошибка при откате транзакции: {}", rollbackEx.getMessage());
                }
            }
            logger.error("Ошибка удаления поста с ID {}", id, e);
            return false;
        }
    }
}
