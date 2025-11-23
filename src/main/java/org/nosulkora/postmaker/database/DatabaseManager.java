package org.nosulkora.postmaker.database;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.nosulkora.postmaker.model.Label;
import org.nosulkora.postmaker.model.Post;
import org.nosulkora.postmaker.model.Writer;

public class DatabaseManager {
//    private static final HikariDataSource dataSource;
    private static final SessionFactory sessionFactory;

//    static {
//        // настройки подключения
//        HikariConfig config = new HikariConfig();
//        config.setJdbcUrl("jdbc:postgresql://localhost:5432/postmaker");
//        config.setUsername("postmakerAdmin");
//        config.setPassword("postmakerPassword");
//        config.setDriverClassName("org.postgresql.Driver");
//
//        // настройки пула
//        config.setMaximumPoolSize(10); // макс соединений
//        config.setMinimumIdle(5); // мин простаивающих соединений
//        config.setConnectionTimeout(30000); // 30сек таймаут на получ. соед.
//        config.setIdleTimeout(600000); // 10мин время жизни простаивающего соед.
//        config.setMaxLifetime(1800000); // 30мин макисмальное время жизни соед.
//        config.setAutoCommit(true); // автокомит по умолчанию
//
//        // оптимизация для PostgresSQL
//        config.addDataSourceProperty("cachePrepStmts", "true");
//        config.addDataSourceProperty("prepStmtCacheSize", "250");
//        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
//        config.addDataSourceProperty("useServerPrepStmts", "true");
//        config.addDataSourceProperty("reWriteBatchedInserts", "true");
//
//        config.addDataSourceProperty("useUnicode", "true");
//        config.addDataSourceProperty("characterEncoding", "UTF-8");
//        config.addDataSourceProperty("serverTimezone", "UTC");
//
//        dataSource = new HikariDataSource(config);
//
//        try {
//            // Создаем Properties для Hibernate
//            Properties hibernateProperties = new Properties();
//            hibernateProperties.put("hibernate.connection.datasource", dataSource);
//            hibernateProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
//            hibernateProperties.put("hibernate.default_schema", "postmaker");
//            hibernateProperties.put("hibernate.show_sql", "true");
//            hibernateProperties.put("hibernate.format_sql", "true");
//            hibernateProperties.put("hibernate.use_sql_comments", "true");
//            hibernateProperties.put("hibernate.hbm2ddl.auto", "validate");
//            hibernateProperties.put("hibernate.jdbc.batch_size", "20");
//            hibernateProperties.put("hibernate.order_inserts", "true");
//            hibernateProperties.put("hibernate.order_updates", "true");
//            hibernateProperties.put("hibernate.cache.use_second_level_cache", "false");
//            hibernateProperties.put("hibernate.cache.use_query_cache", "false");
//
//            sessionFactory = new Configuration()
//                    .addProperties(hibernateProperties)
//                    .addAnnotatedClass(Writer.class)
//                    .addAnnotatedClass(Post.class)
//                    .addAnnotatedClass(Label.class)
//                    .buildSessionFactory();
//        } catch (Throwable ex) {
//            if (dataSource != null && !dataSource.isClosed()) {
//                dataSource.close();
//            }
//            throw new ExceptionInInitializerError(ex);
//        }
//    }

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

//    public static Connection getConnection() throws SQLException {
//        return dataSource.getConnection();
//    }
//
//    // Для миграции
//    public static DataSource getDataSource() {
//        return dataSource;
//    }
//
//    public static void closeConnection() {
//        if (dataSource != null && !dataSource.isClosed()) {
//            dataSource.close();
//            System.out.println("HikariCP pool closed");
//        }
//    }
//
//    public static boolean isDatabaseAvailable() {
//        try (Connection conn = getConnection()) {
//            return conn.isValid(2);
//        } catch (SQLException e) {
//            return false;
//        }
//    }

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
