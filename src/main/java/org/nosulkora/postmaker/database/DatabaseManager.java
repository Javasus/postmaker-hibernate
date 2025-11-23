package org.nosulkora.postmaker.database;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Writer;

public class DatabaseManager {
    private static final SessionFactory sessionFactory;

    static {
        try {
            // Используем конфигурацию из hibernate.cfg.xml
            sessionFactory = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .addAnnotatedClass(Writer.class)
                    .addAnnotatedClass(Post.class)
                    .addAnnotatedClass(Label.class)
                    .buildSessionFactory();

        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Получить SessionFactory Hibernate
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    public static boolean isDatabaseAvailable() {
        try (Session session = sessionFactory.openSession()) {
            // Простой запрос для проверки соединения
            session.createQuery("SELECT 1", Integer.class).getSingleResult();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
