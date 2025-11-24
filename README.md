# PostMaker Hibernate

PostMaker - это полнофункциональное CRUD приложение для управления авторами, постами и метками с использованием консольного интерфейса.

## 🏗️ Архитектура

```
src/main/java/org/nosulkora/postmaker/
├── controller/           # Контроллеры
│   ├── LabelController.java
│   ├── PostController.java
│   └── WriterController.java
├── database/            # Управление БД
│   ├── DatabaseManager.java
│   └── FlywayManager.java
├── model/               # Сущности
│   ├── Label.java
│   ├── Post.java
│   ├── Status.java
│   └── Writer.java
├── repository/impl/     # Репозитории
│   ├── GenericRepository.java
│   ├── LabelRepository.java
│   ├── PostRepository.java
│   └── WriterRepository.java
└── view/               # Консольный интерфейс
├── LabelView.java
├── MainView.java
├── PostView.java
└── WriterView.java
```

## 🛠️ Технологии

- **Java 24**
- **Hibernate 6.6.7** - ORM
- **PostgreSQL** - база данных
- **Flyway** - миграции БД
- **Maven** - система сборки
- **JUnit 5** - тестирование

## 📋 Сущности

- **Writer** - авторы с постами
- **Post** - посты с метками и статусами  
- **Label** - метки для категоризации
- **Status** - статусы постов (ACTIVE, DELETED)

## 🚀 Быстрый старт

### Предварительные требования
- Java 24
- PostgreSQL

### Настройка базы данных
1. Создайте базу данных `postmaker`
2. Настройте подключение в `src/main/resources/application.properties`

### Запуск приложения
```bash
# Применение миграций
mvn flyway:migrate

# Сборка и запуск
mvn clean package
```

### Тестирование
```bash
mvn test
```

## 📝 Использование

Консольное меню для управления:
- ✅ Создание, чтение, обновление и удаление авторов
- 📝 Управление постами и статусами
- 🏷️ Работа с метками
- 🔗 Связывание сущностей между собой

## 🔧 Команды Maven

```bash
mvn clean package      # Сборка проекта
mvn test              # Запуск тестов
mvn flyway:migrate    # Применение миграций БД
```

Приложение использует современный стек Java технологий с поддержкой Hibernate ORM для работы с базой данных.
```