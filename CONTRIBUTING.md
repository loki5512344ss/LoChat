# Contributing to LoChat | Вклад в LoChat

First off, thank you for considering contributing! We welcome all types of contributions — bug reports, feature requests, documentation, and code changes.

Прежде всего, спасибо за желание внести вклад! Мы приветствуем любые виды вклада — сообщения об ошибках, запросы функций, документацию и изменения кода.

---

## 🇬🇧 English

### How to Contribute

1. **Fork** the repository
2. **Create a branch** (`git checkout -b feature/your-feature`)
3. **Make your changes**
4. **Test** your changes (`./gradlew build`)
5. **Commit** (`git commit -m 'Add some feature'`)
6. **Push** (`git push origin feature/your-feature`)
7. **Open a Pull Request**

### Pull Request Guidelines

- Keep PRs focused — one feature/fix per PR
- Write descriptive commit messages
- Update documentation if needed
- Ensure code passes `./gradlew build`
- Follow Google Java Style (see [checkstyle config](config/checkstyle/README.md))

### Development Setup

```bash
# Clone and build
git clone https://github.com/loki5512344ss/LoChat.git
cd LoChat
./gradlew build

# Run local test server
./gradlew runServer
```

### Code Style

This project follows **Google Java Style Guide** with:
- 2-space indentation
- Javadoc on public APIs
- No wildcard imports
- Max 100 characters per line

The build includes Checkstyle verification. Run `./gradlew check` to validate.

### Report Bugs

Create an issue via [Bug Report template](.github/ISSUE_TEMPLATE/bug_report.md) with:
- Server version and plugin version
- Steps to reproduce
- Expected vs actual behavior
- Relevant logs and configs

### Feature Requests

Create an issue via [Feature Request template](.github/ISSUE_TEMPLATE/feature_request.md) describing:
- What you want to add and why
- How it should work
- Example configuration/usage

---

## 🇷🇺 Русский

### Как внести вклад

1. **Сделайте форк** репозитория
2. **Создайте ветку** (`git checkout -b feature/your-feature`)
3. **Внесите изменения**
4. **Протестируйте** (`./gradlew build`)
5. **Закоммитьте** (`git commit -m 'Add some feature'`)
6. **Запушьте** (`git push origin feature/your-feature`)
7. **Откройте Pull Request**

### Рекомендации по Pull Request

- Один PR — одна функция/исправление
- Пишите информативные сообщения коммитов
- Обновляйте документацию при необходимости
- Убедитесь, что код проходит `./gradlew build`
- Следуйте Google Java Style (см. [конфиг checkstyle](config/checkstyle/README.md))

### Настройка окружения

```bash
# Клонирование и сборка
git clone https://github.com/loki5512344ss/LoChat.git
cd LoChat
./gradlew build

# Запуск локального тестового сервера
./gradlew runServer
```

### Стиль кода

Проект следует **Google Java Style Guide**:
- Отступы: 2 пробела
- Javadoc для публичных API
- Без *-импортов
- Максимум 100 символов на строку

Сборка включает проверку Checkstyle. Запустите `./gradlew check` для проверки.

### Сообщение об ошибке

Создайте issue по [шаблону](.github/ISSUE_TEMPLATE/bug_report.md) с:
- Версией сервера и плагина
- Шагами для воспроизведения
- Ожидаемым и фактическим поведением
- Релевантными логами и конфигами

### Запрос функции

Создайте issue по [шаблону](.github/ISSUE_TEMPLATE/feature_request.md) с описанием:
- Что вы хотите добавить и зачем
- Как это должно работать
- Пример конфигурации/использования

---

## Contribution Types | Типы вклада

| Type | Description |
|------|-------------|
| 🐛 Bug fix | Error correction |
| ✨ Feature | New functionality |
| 📝 Docs | Documentation improvements |
| ⚡ Performance | Optimization |
| 🔒 Security | Security improvements |
| 🎨 UI/UX | Visual improvements |

## Need Help? | Нужна помощь?

- [Issues](https://codeberg.org/loki5512344/Lochat/issues)
- [Discussions](https://codeberg.org/loki5512344/Lochat/issues)
