# Link Tracker

## Описание проекта

Проект сделан в рамках курса Академия Бэкенда.

Приложение для отслеживания обновлений контента по ссылкам.
При появлении новых событий отправляется уведомление в Telegram.

Проект написан на `Java 23` с использованием `Spring Boot 3`.

Проект состоит из 2-х приложений:
* Bot
* Scrapper

Для работы требуется БД `PostgreSQL`. Присутствует опциональная зависимость на `Kafka`.

Для дополнительной справки: [HELP.md](./HELP.md)

---

## Бот

### Функционал

Бот поддерживает следующие команды:

- [**`/help`**](https://github.com/zavik001/link-tracker/blob/master/bot/src/main/java/backend/academy/bot/command/HelpCommand.java) — список всех доступных команд.
- [**`/start`**](https://github.com/zavik001/link-tracker/blob/master/bot/src/main/java/backend/academy/bot/command/StartCommand.java) — регистрация пользователя.
- [**`/stop`**](https://github.com/zavik001/link-tracker/blob/master/bot/src/main/java/backend/academy/bot/command/StopCommand.java) — удаление всех данных о чате (ID чата, отслеживаемые ссылки и т. д.).
- [**`/track`**](https://github.com/zavik001/link-tracker/blob/master/bot/src/main/java/backend/academy/bot/command/TrackCommand.java) — добавление ссылки для отслеживания (валидируется через [`LinkValidator`](https://github.com/zavik001/link-tracker/blob/master/bot/src/main/java/backend/academy/bot/util/LinkValidator.java)).
- [**`/untrack`**](https://github.com/zavik001/link-tracker/blob/master/bot/src/main/java/backend/academy/bot/command/UntrackCommand.java) — удаление ссылки из списка отслеживаемых.
- [**`/list`**](https://github.com/zavik001/link-tracker/blob/master/bot/src/main/java/backend/academy/bot/command/ListCommand.java) — получение списка всех отслеживаемых ссылок.
- [**`/tag`**](https://github.com/zavik001/link-tracker/blob/master/bot/src/main/java/backend/academy/bot/command/TagCommand.java) — для просмотра всех тегов (/tag) и ссылок под конкретным тегом (/tag < tag >)

### Взаимодействие со Scrapper

Бот общается со Scrapper API через:
- [**`ChatClient`**](https://github.com/zavik001/link-tracker/blob/master/bot/src/main/java/backend/academy/bot/client/ChatClient.java) — регистрация и удаление чатов.
- [**`LinkClient`**](https://github.com/zavik001/link-tracker/blob/master/bot/src/main/java/backend/academy/bot/client/LinkClient.java) — управление ссылками (добавление, удаление, получение списка).
- [**`TagClient`**](https://github.com/zavik001/link-tracker/blob/master/bot/src/main/java/backend/academy/bot/client/TagClient.java) — для получения списка тегов и ссылок по тегу.

Scrapper API работает по OpenAPI-контракту. В случае ошибок ошибки логируются, корректную обработку ошибок и пересылку сообщений в чат выполняет [`ErrorHandler`](https://github.com/zavik001/link-tracker/blob/master/bot/src/main/java/backend/academy/bot/client/ErrorHandler.java).

### Получение обновлений

- Бот получает обновления о ссылках через [`UpdateController`](https://github.com/zavik001/link-tracker/blob/master/bot/src/main/java/backend/academy/bot/controller/UpdateController.java) по HTTP либо через [`KafkaUpdateListener`](https://github.com/zavik001/link-tracker/blob/master/bot/src/main/java/backend/academy/bot/kafka/KafkaUpdateListener.java) по Kafka.
- Scrapper отправляет данные по OpenAPI-контракту.
- Обновления рассылаются чатам через [`UpdateService`](https://github.com/zavik001/link-tracker/blob/master/bot/src/main/java/backend/academy/bot/service/UpdateService.java).

### Дополнительно

- Бот поддерживает встроенное [**меню команд**](https://github.com/zavik001/link-tracker/blob/master/bot/src/main/java/backend/academy/bot/Bot.java) в Telegram.

### Кеширование

Бот кеширует ответы для следующих команд:
- /tag
- /tag <tag>
- /list

Кеш автоматически сбрасывается в следующих случаях:
- При добавлении или удалении ссылки (/track, /untrack)
- При удалении чата (/stop)

При вызове команд бот сначала проверяет наличие ответа в кеше. Если данные найдены — используется кеш. В противном случае происходит обращение к Scrapper API, и результат сохраняется в кеш.

---

## Scrapper

### Функционал

Scrapper обрабатывает запросы от бота:
- **Работа с чатами** через [`ChatController`](https://github.com/zavik001/link-tracker/blob/master/scrapper/src/main/java/backend/academy/scrapper/controller/ChatController.java) и [`ChatService`](https://github.com/zavik001/link-tracker/blob/master/scrapper/src/main/java/backend/academy/scrapper/service/ChatService.java).
- **Работа с ссылками** через [`LinkController`](https://github.com/zavik001/link-tracker/blob/master/scrapper/src/main/java/backend/academy/scrapper/controller/LinkController.java) и [`LinkService`](https://github.com/zavik001/link-tracker/blob/master/scrapper/src/main/java/backend/academy/scrapper/service/LinkService.java).
- **Работа с тегами** через [`TagController`](https://github.com/zavik001/link-tracker/blob/master/scrapper/src/main/java/backend/academy/scrapper/controller/TagController.java) и [`TagService`](https://github.com/zavik001/link-tracker/blob/master/scrapper/src/main/java/backend/academy/scrapper/service/TagService.java).

Все контроллеры работают по OpenAPI-контракту.

### Получение обновлений

- [**`UpdateScheduler`**](https://github.com/zavik001/link-tracker/blob/master/scrapper/src/main/java/backend/academy/scrapper/scheduler/UpdateScheduler.java) периодически вызывает метод [`UpdateService.checkUpdates()`](https://github.com/zavik001/link-tracker/blob/master/scrapper/src/main/java/backend/academy/scrapper/service/UpdateService.java).
- **Источники данных:**
  - GitHub — через [`GitHubClient`](https://github.com/zavik001/link-tracker/blob/master/scrapper/src/main/java/backend/academy/scrapper/client/GitHubClient.java)
  - Stack Overflow — через [`StackOverflowClient`](https://github.com/zavik001/link-tracker/blob/master/scrapper/src/main/java/backend/academy/scrapper/client/StackOverflowClient.java)
- **Обработка полученных данных**
  1. Запрос обновлений
     - Для каждого URL запрашивается обновление через API
  2. Фильтрация обновлений
     - Определяются подписанные пользователи (чаты), которые отслеживают данный URL.
     - Для каждого пользователя применяется его список фильтров:
     1. Ключевые фильтры — фильтрация по ключевым словам (issue, commit и т.д).
     2. Анти-фильтры по пользователям — если указано user=username, то обновления от этого пользователя игнорируются.
  3. Парсинг ответа
     - Полученный JSON-ответ анализируется, извлекаются значения, соответствующие фильтрам.
  4. Проверка актуальности
     - Обновление считается релевантным, если оно произошло после последнего запуска шедулера.
  5. Формирование уведомлений
     - Отобранные обновления, соответствующие фильтрам, отправляются пользователям либо через HTTP, либо через Kafka.
- **Обработка батчей и многопоточность**
  - Ссылки на обновления запрашиваются партиями (batch) заданного размера. Каждый батч делится между потоками. Количество потоков настраивается через конфигурацию.

## Хранение данных

Для хранения данных используются **четыре основные таблицы** и **три вспомогательные таблицы** для связи.

### Основные таблицы

- `chats` — таблица чатов.
- `links` — таблица ссылок.
- `tags` — таблица тегов.
- `filters` — таблица фильтров.

### Связующие таблицы

Связи между чатами, ссылками, тегами и фильтрами реализованы через **промежуточные таблицы**:

- `chat_links` — связь между чатами и ссылками.
- `chat_link_tags` — связь между ссылками и тегами в контексте чата.
- `chat_link_filters` — связь между ссылками и фильтрами в контексте чата.

**Один чат может отслеживать несколько ссылок, а одна ссылка может быть отслеживаемой несколькими чатами.**  
**Каждая ссылка может иметь несколько тегов и фильтров в рамках одного чата.**

### Способы работы с базой данных:

- **SQL** (`JdbcTemplate`, `SqlRepository`).
- **ORM** (`Hibernate`, `OrmRepository`).  
  Оба репозитория (`SqlRepository` и `OrmRepository`) наследуются от `DbRepository` и работают одинаково.  
  Выбор зависит от **настроек** (`database.access-type`).

----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## Запуск проекта

### Клонирование репозитория

```bash
git clone git@github.com:link-tracker.git
```

### Сборка

```bash
./mvnw clean verify
```

### Запуск

####  Запуск базы

```bash
docker-compose up -d postgres
```

#### Запуск миграции

```bash
docker-compose up -d migrate
```

#### Запуск redis

```bash
docker-compose up -d redis
```

#### Запуск zookeeper

```bash
docker-compose up -d zookeeper
```

#### Запуск kafka

```bash
docker-compose up -d kafka

docker exec -it java-zavik001-kafka-1 bash

$> kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --replication-factor 1 \
  --partitions 1 \
  --topic updates-dlq
$> kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --replication-factor 1 \
  --partitions 1 \
  --topic updates
```

#### Запуск скраппер

```bash
docker-compose up -d scrapper
```

#### Запуск  бота

```bash
docker-compose up -d bot
```

