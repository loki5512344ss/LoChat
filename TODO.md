# LoChat Development TODO

> Last updated: 2026-08-04

---

## ✅ CRITICAL FIXES (blocking release — from code review 2026-08-04, all DONE)

### 1. Translation: async + bounded cache, no String.intern(), out of render() ✅
- `renderer/EnhancedChatRenderer.java` — removed blocking `applyTranslation()` from `render()` (zero network I/O in render path). Translation is now delivered via async action bar (`sendTranslationAsync` → `translateAsync().thenAccept`).
- `translate/TranslationService.java` — no more `String.intern()`; bounded LRU cache (2000 entries, synchronized LinkedHashMap); `translateAsync` runs on a dedicated 2-thread daemon executor; failures return original text, never throw.

### 2. Atomic file writes (temp + rename, per-file lock) for all persistence ✅
- `utils/persistence/FilePersistence.java` — `saveYaml`/`saveJson` now write `<name>.tmp` + `Files.move(ATOMIC_MOVE, REPLACE_EXISTING)` under a per-file `ReentrantLock`.
- `core/service/PlayerServiceImpl.java` — `statsLock` serializes async `persistPlayerMessages` and main-thread `saveAll()`.
- `core/service/PunishmentServiceImpl.java` — removed `saveAsync()` from `isBanned()` read path.
- `core/service/mute/MuteDataStorage.java` — uses `FilePersistence.saveJson` (atomic), dropped raw FileWriter.
- `core/service/mute/MuteHistoryManager.java` — atomic temp+rename write under per-file lock, no `e.printStackTrace()`.

### 3. Route /g and /l through the same filter pipeline + escape MiniMessage ✅
- `core/service/ChatServiceImpl.java` — `String` messages now run length check → `MessageService.processMessage` (mute/cooldown) → `AdvancedMessageFilter` → `MiniMessage.escapeTags()` → component (colors only with `lochat.chat.colors`). `Component` messages bypass (trusted internal). Local radius from `AppearanceConfig`.
- `commands/chat/GlobalChatCommand.java`, `LocalChatCommand.java` — pass plain `String`, no more `MM.deserialize(raw input)`.
- `LoChat.java`/`PluginInitializer.java` — filter instance stored on plugin; `ServiceRegistry` builds `MessageService` before `ChatService`.

### 4. Remove duplicate Discord send; single global/local detection ✅
- `listener/DiscordEventListener.java` — deleted the `onChat(AsyncChatEvent)` handler; `ChatEventListener` is now the single send path with correct `isGlobal`.

### 5. Fix config key mismatches (underscore vs dash / nesting) ✅
- `config/AppearanceConfig.java` — `chat.local-radius`, `chat.clear-lines`, `chat.min-message-length`, `chat.max-message-length`, `chat.colors.*` (dash), `prefixes.*.separator` (string), `discord.avatar-urls.*`, `discord.event-titles.*` (+ `_`→`-` normalization).
- `integrations/discord/DiscordConfig.java` — `webhook.avatar-url`, `chat.global-only`, `chat.use-embed`, `chat.embed-color`, `filter.*` (dash), `performance.retry-*`.
- `config/MuteConfig.java` — `settings.*`, `notifications.*`.
- `config/SoundsConfig.java` — `messages.mention.*`, `messages.private-message.*`, `commands.success.*`.
- `config/FiltersConfig.java` + filters — `config/filters.yml` is now the single source of truth (`caps.max-caps-percent`, `swear.replace`, `hidden-urls.*`, `spam.max-repeating-chars`, `flood.max-same-messages`).
- `gradient/config/GradientConfig.java` — nested keys `gradient.limits.*`, `gradient.pricing.*`, `gradient.cooldowns.*`, `gradient.formatting.*`, `gradient.storage.type` → **SQLite now detected**.
- `managers/AutoMessageManager.java` + `config.yml` — reads `automessages.messages` (new `automessages:` section added to config.yml), honors `automessages.random`.

---

## ✅ COMPLETED THIS SESSION

### Architecture (SOLID/KISS/DRY)
- [x] `AppearanceConfig` — fixed double-call bug
- [x] `config/` — 9→3 files per folder (subfolders: chat/, filter/, mute/, manager/)
- [x] `api/service/` + `core/service/` — 8→3 files per folder (subfolders: chat/, player/, moderation/, pm/, spy/, ignore/)
- [x] `ConfigManager` — god class → facade + 6 managers (Chat/Pm/Mention/ClearChat/CustomMessages/Filters)
- [x] `MessagingService` — fat interface → 3 services (PrivateMessageService/SpyService/IgnoreService)
- [x] `AdvancedMessageFilter` — hardcoded pipeline → `FilterPipeline` with dynamic registration
- [x] 25 commands → `BaseCommand`/`AdminCommand`/`PlayerCommand` hierarchy
- [x] `MuteCommand` — decomposed into helper methods (KISS fix)

### Encapsulation
- [x] `ChatEventListener` — removed instanceof cast to `PlayerServiceImpl`
- [x] `PunishmentSnapshot` — mutable fields → private + getters
- [x] `BaseConfig` — protected fields → private + protected getters
- [x] 29 files with `import.*` → explicit imports

### Checkstyle
- [x] Config: 4-space indent, 140-char lines, Google-style naming/quality
- [x] Warnings: **520 → 0** (both main and test)
- [x] Wired into `build.gradle.kts`

### Configs
- [x] All rewritten: clean, English comments, no Spacelegacy palette

---

## 🟡 NEXT: NEW FEATURES

### 1. Channels (like VentureChat / Carbon)
- `/join`, `/leave`, `/channel` — full channel system: global, local, staff, trade, party.
- Per-channel colors, aliases, permissions, cooldowns, spy mode.
- Party chat: private groups `/party`.
- Network channels (Bungee/Velocity) sync.

### 2. Anti-swear hardening + filter statistics
- Bypass obfuscation: `q→р` leet/Unicode substitution, repeated chars between letters, spaces/dots inside words.
- Track filter stats (how many messages blocked per filter) → expose via command/placeholder.

### 3. Stable plugin API + API events
- Publish `api/` modules to a repo so third-party plugins can depend on it.
- Add `api-version` hint / `provides` in `plugin.yml` for dependency resolution.
- Dedicated API events: `LoChatMessageEvent`, `LoChatFilterEvent`, `LoChatPunishEvent` etc.

### 4. DiscordSRV-style 2-way bridge (Discord → chat)
- Read messages from a Discord channel (bot or webhook listener) and relay into Minecraft chat.
- Channel ↔ Discord channel mapping.

### 5. Full spy + auto-mute for spam
- PM spy + command spy with custom formats (like VentureChat).
- Auto-mute repeat offenders (spam/flood) with configurable threshold and duration.

---

## 🔵 LOWER PRIORITY (carry-over)

### 1. Shared Persistence Utility
5 services repeat load/save/ensureDir pattern:
- `NickServiceImpl`, `PlayerServiceImpl`, `ChatServiceImpl`, `PunishmentServiceImpl`, `IgnoreServiceImpl`

**Fix:** Extract `FilePersistenceUtil` or base class

### 2. Persist nicknames on change ✅
`NickServiceImpl` now saves asynchronously (`FoliaUtil.runAsync → save()`) on every `setNickname`/`resetNickname` — nicknames survive crashes.

### 3. DIP: Service Locator Anti-Pattern
`ServiceRegistry.get(Xxx.class)` used everywhere instead of constructor DI. Big refactor.

### 4. DRY: PM Send Logic ✅
PM cooldown + actual send centralized in `PrivateMessageServiceImpl.sendPrivateMessage`; both `/msg` and `/reply` delegate to it.

### 5. Fix cooldown map collision ✅
`PlayerServiceImpl` now keeps a separate `ConcurrentHashMap` per chat type (`global`, `local`, `rp_me`, `rp_do`, `rp_try`, `pm`) — no more cross-blocking. Covered by `PlayerServiceCooldownTest`.

### 6. Thread-safe flood/spam deques ✅
`FloodFilter`/`SpamFilter` now use `ConcurrentLinkedDeque` instead of plain `ArrayDeque`.

### 7. Tests
Only 11 test files for 155 main files. Very low coverage (JaCoCo wired, CI uploads report).

### 8. No PM cooldown ✅
`chat.pm.cooldown` (default 2s) is now enforced in `PrivateMessageServiceImpl.sendPrivateMessage`, honoring `chat.bypass.cooldown`.

---

## 📋 KNOWN ISSUES

- Gradients may not display correctly on clients < 1.16
- Some filters may block legitimate messages (configurable in `config/filters.yml`)
