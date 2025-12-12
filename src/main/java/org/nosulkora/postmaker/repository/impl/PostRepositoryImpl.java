package org.nosulkora.postmaker.repository.impl;

import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Status;
import org.nosulkora.postmaker.repository.PostRepository;
import org.nosulkora.postmaker.utils.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        return SessionManager.execute(session -> session.merge(post));
    }

    @Override
    public Post update(Post post) {
        return save(post);
    }

    @Override
    public Post getById(Long id) {
        return SessionManager.executeReadOnly(session -> session.createQuery(HQL_GET_POST_BY_ID, Post.class)
                .setParameter("id", id)
                .setParameter("status", Status.ACTIVE)
                .uniqueResult());
    }

    @Override
    public List<Post> getAll() {
        return SessionManager.executeReadOnly(session -> session.createQuery(HQL_GET_ALL_POSTS, Post.class)
                .setParameter("status", Status.ACTIVE)
                .getResultList());
    }

    @Override
    public boolean deleteById(Long id) {
        try {
            boolean deleted = Boolean.TRUE.equals(SessionManager.execute(session -> {
                        Post post = session.createQuery(HQL_GET_POST_BY_ID, Post.class)
                                .setParameter("id", id)
                                .setParameter("status", Status.ACTIVE)
                                .uniqueResult();
                        if (post != null) {
                            post.setStatus(Status.DELETED);
                            return true;
                        }
                        return false;
                    })
            );
            if (Boolean.TRUE.equals(deleted)) {
                logger.info("Пост с ID {} помечен как удаленный", id);
                return true;
            } else {
                logger.warn("Пост с ID {} не найден или уже удален", id);
                return false;
            }
        } catch (
                Exception e) {
            logger.error("Ошибка при удалении поста с ID {}", id, e);
            return false;
        }
    }
}
