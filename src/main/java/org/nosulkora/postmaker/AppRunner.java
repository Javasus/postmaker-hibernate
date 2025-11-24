package org.nosulkora.postmaker;

import org.nosulkora.postmaker.database.DatabaseManager;
import org.nosulkora.postmaker.database.FlywayManager;
import org.nosulkora.postmaker.view.MainView;

public class AppRunner {
    public static void main(String[] args) {
        try {
            // Проверяем доступность базы данных
            if (!DatabaseManager.isDatabaseAvailable()) {
                throw new RuntimeException("База данных недоступна");
            }
            // Выполняем миграции
            FlywayManager.runMigrations();

            // Запускаем приложение
            MainView mainView = new MainView();
            mainView.start();

        } catch (Exception e) {
            System.out.println("Ошибка запуска приложения: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.shutdown();
        }
    }
}