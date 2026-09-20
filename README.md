<div align="center">

# LoChat

Advanced chat plugin with gradient nicknames, moderation and Discord integration for Paper/Folia servers.

![Java](https://img.shields.io/badge/Java-21+-orange?style=flat-square&logo=openjdk&logoColor=white)
![Paper](https://img.shields.io/badge/Paper-1.19.2+-blue?style=flat-square)
![Folia](https://img.shields.io/badge/Folia-supported-purple?style=flat-square)
![License](https://img.shields.io/badge/license-GPLv3-blue?style=flat-square&logo=gnu&logoColor=white)
![version](https://img.shields.io/badge/version-1.5.5-green?style=flat-square)

[English](#english) | [Русский](#russian)

</div>

---

<a name="english"></a>

## English

### Overview

LoChat is a powerful Minecraft chat plugin for Paper 1.19.2+ and Folia servers. It features gradient nicknames, advanced moderation, flexible configuration, Discord integration, and extensive API.

### Features

| Feature | Description |
|---------|-------------|
| Global & Local chat | Cross-server and radius-based proximity chat |
| Private messages | PM system with reply support and ignore list |
| Gradient nicknames | Up to 7 colors with built-in presets (fire, ocean, rainbow) |
| Mentions | @player, @everyone, @here, @role with sounds |
| Moderation | Mute with history, silent mode, escalation |
| Chat filters | Swear, spam, flood, CAPS, URL/IP, hidden links |
| RP commands | /me, /do, /try, /roll with configurable radius |
| Custom join/quit/death | Messages with weapon name and hover events |
| Discord integration | Webhook relay, join/quit/death events, avatars |
| Custom commands | Define your own commands with placeholders |
| MiniMessage support | Full MiniMessage formatting with HEX colors |
| LuckPerms integration | Gradient prefixes and suffixes |
| PlayerPoints | Purchase system for gradients and prefixes |

### Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/g <message>` | `chat.global.use` | Send global message |
| `/l <message>` | `chat.local.use` | Send local message |
| `/msg <player> <message>` | `chat.pm.use` | Private message |
| `/reply <message>` | `chat.pm.use` | Reply to last PM |
| `/ignore <player>` | `chat.pm.ignore` | Ignore player |
| `/color <hex1> [hex2]` | `gradient.color` | Set gradient nickname |
| `/prefix <text>` | `gradient.prefix` | Set custom prefix |
| `/lmute <player> [time] [-s] [reason]` | `lochat.mute` | Mute player |
| `/lunmute <player> [-s]` | `lochat.unmute` | Unmute player |
| `/chatspy` | `chat.spy` | Toggle spy mode |
| `/clearchat` | `chat.clear` | Clear chat |
| `/lochat reload` | `lochat.admin` | Reload config |

### Dependencies

- Required: Paper 1.19.2+, Java 21+
- Optional: PlaceholderAPI, LuckPerms, PlayerPoints, LibertyBans, SkinsRestorer

### Installation

1. Drop the jar into `plugins/`
2. Restart the server
3. Configure `plugins/LoChat/config.yml`

---

<a name="russian"></a>

## Русский

### Обзор

LoChat - мощный чат-плагин для Paper 1.19.2+ и Folia серверов. Включает градиентные ники, продвинутую модерацию, гибкую конфигурацию, Discord интеграцию и обширный API.

### Возможности

| Возможность | Описание |
|-------------|----------|
| Глобальный и локальный чат | Обще-серверный и чат в радиусе |
| Личные сообщения | PM с ответом и списком игнора |
| Градиентные ники | До 7 цветов с пресетами (огонь, океан, радуга) |
| Упоминания | @player, @everyone, @here, @role со звуками |
| Модерация | Мут с историей, тихий режим, эскалация |
| Фильтры чата | Мат, спам, флуд, CAPS, URL/IP, скрытые ссылки |
| RP команды | /me, /do, /try, /roll с настраиваемым радиусом |
| Кастомные вход/выход/смерть | Сообщения с названием оружия и hover-событиями |
| Discord интеграция | Webhook, события входа/выхода/смерти, аватары |
| Кастомные команды | Создавайте свои команды с плейсхолдерами |
| MiniMessage | Полная поддержка MiniMessage с HEX цветами |
| LuckPerms | Градиентные префиксы и суффиксы |
| PlayerPoints | Покупка градиентов и префиксов |

### Команды

| Команда | Право | Описание |
|---------|-------|----------|
| `/g <сообщение>` | `chat.global.use` | Глобальное сообщение |
| `/l <сообщение>` | `chat.local.use` | Локальное сообщение |
| `/msg <игрок> <сообщение>` | `chat.pm.use` | Личное сообщение |
| `/color <hex1> [hex2]` | `gradient.color` | Установить градиент |
| `/lmute <игрок> [время] [-s] [причина]` | `lochat.mute` | Замутить |
| `/lochat reload` | `lochat.admin` | Перезагрузить конфиг |

### Зависимости

- Обязательные: Paper 1.19.2+, Java 21+
- Опциональные: PlaceholderAPI, LuckPerms, PlayerPoints, LibertyBans, SkinsRestorer

### Установка

1. Положите jar в папку `plugins/`
2. Перезапустите сервер
3. Настройте `plugins/LoChat/config.yml`

---

### Links

- [Releases](../../releases)
- [Issues](../../issues)
- [License](LICENSE)

### License

GNU General Public License v3.0