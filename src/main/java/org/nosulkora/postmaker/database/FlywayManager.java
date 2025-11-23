package org.nosulkora.postmaker.database;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.output.MigrateResult;

public class FlywayManager {

    private static final Flyway flyway;

    static {
        // Инициализация Flyway для основной базы
        flyway = Flyway.configure().dataSource(
                        "jdbc:postgresql://localhost:5432/postmaker",
                        "postmakerAdmin",
                        "postmakerPassword")
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .validateOnMigrate(false)
                .load();
    }

    /**
     * Выполняет миграции
     */
    public static void runMigrations() {
        System.out.println("Запуск миграций базы данных...");
        MigrateResult result = flyway.migrate();
        System.out.println("Миграции успешно выполнены");
        logAppliedMigrations();
    }

    /**
     * Проверяет состояние миграций без их применения
     */
    public static void validate() {
        System.out.println("Проверка состояния миграций...");
        flyway.validate();
        System.out.println("Миграции валидны");
    }

    /**
     * Откатывает основную базу данных
     */
    public static void clean() {
        System.out.println("Очистка базы данных...");
        flyway.clean();
        System.out.println("база данных очищена");
    }

    private static void logAppliedMigrations() {
        MigrationInfo[] applied = flyway.info().applied();
        if (applied.length > 0) {
            System.out.println("Примененные миграции:");
            for (MigrationInfo info : applied) {
                System.out.printf("  %s - %s%n", info.getVersion(), info.getDescription());
            }
        }
    }
}
