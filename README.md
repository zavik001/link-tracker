# 📌 Link Tracker

## 📝 Описание проекта

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

## 🤖 Бот

### 📌 Функционал

Бот поддерживает следующие команды:

- [**`/help`**](https://github.com/central-university-dev/java-zavik001/blob/homework1/bot/src/main/java/backend/academy/bot/command/HelpCommand.java) — список всех доступных команд.
- [**`/start`**](https://github.com/central-university-dev/java-zavik001/blob/homework1/bot/src/main/java/backend/academy/bot/command/StartCommand.java) — регистрация пользователя.
- [**`/stop`**](https://github.com/central-university-dev/java-zavik001/blob/homework1/bot/src/main/java/backend/academy/bot/command/StopCommand.java) — удаление всех данных о чате (ID чата, отслеживаемые ссылки и т. д.).
- [**`/track`**](https://github.com/central-university-dev/java-zavik001/blob/homework1/bot/src/main/java/backend/academy/bot/command/TrackCommand.java) — добавление ссылки для отслеживания (валидируется через [`LinkValidator`](https://github.com/central-university-dev/java-zavik001/blob/homework1/bot/src/main/java/backend/academy/bot/util/LinkValidator.java)).
- [**`/untrack`**](https://github.com/central-university-dev/java-zavik001/blob/homework1/bot/src/main/java/backend/academy/bot/command/UntrackCommand.java) — удаление ссылки из списка отслеживаемых.
- [**`/list`**](https://github.com/central-university-dev/java-zavik001/blob/homework1/bot/src/main/java/backend/academy/bot/command/ListCommand.java) — получение списка всех отслеживаемых ссылок.

### 🔄 Взаимодействие со Scrapper

Бот общается со Scrapper API через:
- [**`ChatClient`**](https://github.com/central-university-dev/java-zavik001/blob/homework1/bot/src/main/java/backend/academy/bot/client/ChatClient.java) — регистрация и удаление чатов.
- [**`LinkClient`**](https://github.com/central-university-dev/java-zavik001/blob/homework1/bot/src/main/java/backend/academy/bot/client/LinkClient.java) — управление ссылками (добавление, удаление, получение списка).

Scrapper API работает по OpenAPI-контракту. В случае ошибок логирование и обработку выполняет [`ErrorHandler`](https://github.com/central-university-dev/java-zavik001/blob/homework1/bot/src/main/java/backend/academy/bot/client/ErrorHandler.java).

### 📩 Получение обновлений

- Бот получает обновления о ссылках через [`UpdateController`](https://github.com/central-university-dev/java-zavik001/blob/homework1/bot/src/main/java/backend/academy/bot/controller/UpdateController.java).
- Scrapper отправляет данные по OpenAPI-контракту.
- Обновления рассылаются чатам через [`UpdateService`](https://github.com/central-university-dev/java-zavik001/blob/homework1/bot/src/main/java/backend/academy/bot/service/UpdateService.java).

### 📜 Дополнительно

- Бот поддерживает встроенное [**меню команд**](https://github.com/central-university-dev/java-zavik001/blob/homework1/bot/src/main/java/backend/academy/bot/Bot.java) в Telegram.

---

## 🗂️ Scrapper

### 📌 Функционал

Scrapper обрабатывает запросы от бота:
- **Работа с чатами** через [`ChatController`](https://github.com/central-university-dev/java-zavik001/blob/homework1/scrapper/src/main/java/backend/academy/scrapper/controller/ChatController.java) и [`ChatService`](https://github.com/central-university-dev/java-zavik001/blob/homework1/scrapper/src/main/java/backend/academy/scrapper/service/ChatService.java).
- **Работа с ссылками** через [`LinkController`](https://github.com/central-university-dev/java-zavik001/blob/homework1/scrapper/src/main/java/backend/academy/scrapper/controller/LinkController.java) и [`LinkService`](https://github.com/central-university-dev/java-zavik001/blob/homework1/scrapper/src/main/java/backend/academy/scrapper/service/LinkService.java).

Все контроллеры работают по OpenAPI-контракту.

### 🔄 Получение обновлений

- [**`UpdateScheduler`**](https://github.com/central-university-dev/java-zavik001/blob/homework1/scrapper/src/main/java/backend/academy/scrapper/scheduler/UpdateScheduler.java) вызывает [`UpdateService.checkUpdates()`](https://github.com/central-university-dev/java-zavik001/blob/homework1/scrapper/src/main/java/backend/academy/scrapper/service/UpdateService.java).
- **Источники данных:**
  - GitHub — через [`GitHubClient`](https://github.com/central-university-dev/java-zavik001/blob/homework1/scrapper/src/main/java/backend/academy/scrapper/client/GitHubClient.java)
  - Stack Overflow — через [`StackOverflowClient`](https://github.com/central-university-dev/java-zavik001/blob/homework1/scrapper/src/main/java/backend/academy/scrapper/client/StackOverflowClient.java)

### 📦 Хранение данных

Данные хранятся в двух репозиториях:
- [**`ChatRepository`**](https://github.com/central-university-dev/java-zavik001/blob/homework1/scrapper/src/main/java/backend/academy/scrapper/repository/ChatRepository.java) — содержит информацию о чатах, отслеживаемых ссылках, тегах и фильтрах.
- [**`UpdateRepository`**](https://github.com/central-university-dev/java-zavik001/blob/homework1/scrapper/src/main/java/backend/academy/scrapper/repository/UpdateRepository.java) — Хранилище данных организовано так, что каждая отслеживаемая ссылка хранится единожды, а все подписчики (чаты) ассоциируются с ней. Это позволяет быстро находить все подписанные чаты и уведомлять их за один запрос
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 🚀 Запуск проекта

### 🔄 Клонирование репозитория

```bash
git clone git@github.com:central-university-dev/java-zavik001.git
```

### 🏗️ Сборка

```bash
./mvnw clean verify
```

### ▶️ Запуск

#### 1️⃣ Запуск бота

```bash
cd bot/
mvn spring-boot:run
```

#### 2️⃣ Запуск Scrapper

```bash
cd scrapper/
mvn spring-boot:run
```

💡 **Примечание:** Рекомендуется запускать бота и Scrapper API в двух отдельных терминалах, чтобы удобно отслеживать их логи и управлять процессами. Можно запустить оба сервиса в одном терминале (например, через tmux или screen), но это менее удобно. Возможно, есть и другие варианты, но два терминала — наиболее комфортный способ работы.
