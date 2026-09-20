package com.loki.lochat.renderer;

import com.loki.lochat.channel.Channel;
import com.loki.lochat.compat.Msg;
import com.loki.lochat.renderer.components.EmojiComponent;
import com.loki.lochat.renderer.components.GradientBuilder;
import com.loki.lochat.renderer.components.UrlProcessor;
import com.loki.lochat.translate.TranslationService;
import com.loki.lochat.utils.format.ChatFormatter;
import com.loki.lochat.utils.format.TextFormatter;
import com.loki.lochat.utils.player.MentionHandler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Рендерер чата — глобальный и локальный режимы
 */
public class EnhancedChatRenderer {

    private static final TextColor FALLBACK_COLOR = NamedTextColor.WHITE;

    private final JavaPlugin plugin;
    private final boolean isGlobal;
    private final Channel channel;
    private final MentionHandler mentionHandler;
    private final com.loki.lochat.config.ConfigManager cfg;

    public EnhancedChatRenderer(JavaPlugin plugin, boolean isGlobal) {
        this(plugin, isGlobal, null);
    }

    public EnhancedChatRenderer(JavaPlugin plugin, Channel channel) {
        this(plugin, channel.isGlobalScope(), channel);
    }

    private EnhancedChatRenderer(JavaPlugin plugin, boolean isGlobal, Channel channel) {
        this.plugin = plugin;
        this.isGlobal = isGlobal;
        this.channel = channel;
        this.mentionHandler = new MentionHandler(plugin);

        com.loki.lochat.LoChat loChat = (com.loki.lochat.LoChat) plugin;
        this.cfg = loChat.getConfigManager();
    }

    public Component render(Player source, Component sourceDisplayName, Component message, Player viewer) {
        // Обрабатываем сообщение
        Component processed = message;
        processed = TextFormatter.formatMarkdown(processed);
        processed = UrlProcessor.process(processed);
        processed = EmojiComponent.processEmojis(processed);

        if (viewer != null) {
            processed = mentionHandler.processMentions(processed, source, viewer);
            if (cfg.isTranslationEnabled()) {
                com.loki.lochat.LoChat loChat = (com.loki.lochat.LoChat) plugin;
                if (loChat.getTranslationService().isEnabled()) {
                    sendTranslationAsync(viewer, PlainTextComponentSerializer.plainText().serialize(processed));
                }
            }
        }

        // Получаем формат из конфига
        com.loki.lochat.config.AppearanceConfig appearanceCfg = cfg.getAppearanceConfig();
        String format = isGlobal ? appearanceCfg.getGlobalChatFormat() : appearanceCfg.getLocalChatFormat();

        // Строим компоненты
        Component emoji = buildEmojiComponent();
        Component prefix = buildPrefix();
        Component separator = buildSeparator();
        Component playerPrefix = buildPlayerPrefix(source);
        Component playerSuffix = buildPlayerSuffix(source);
        Component player = buildPlayerComponent(source, sourceDisplayName);

        return buildFormattedMessage(format, Map.of(
            "emoji", emoji,
            "prefix", prefix,
            "separator", separator,
            "player_prefix", playerPrefix,
            "player", player,
            "player_suffix", playerSuffix,
            "message", processed
        ));
    }

    private String emojiFor() {
        if (channel != null) {
            return channel.display() == null ? "" : channel.display();
        }
        com.loki.lochat.config.AppearanceConfig appearanceCfg = cfg.getAppearanceConfig();
        return isGlobal ? appearanceCfg.getGlobalEmoji() : appearanceCfg.getLocalEmoji();
    }

    private String prefixText() {
        if (channel != null) {
            return channel.prefix() == null ? channel.id() : channel.prefix();
        }
        com.loki.lochat.config.AppearanceConfig appearanceCfg = cfg.getAppearanceConfig();
        return isGlobal ? appearanceCfg.getGlobalText() : appearanceCfg.getLocalText();
    }

    private String channelColor() {
        return channel != null && channel.color() != null ? channel.color() : "&7";
    }

    private Component buildFormattedMessage(String format, Map<String, Component> parts) {
        Component result = Component.empty();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{([^}]+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(format);

        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                result = result.append(Component.text(format.substring(lastEnd, matcher.start())));
            }

            String placeholder = matcher.group(1);
            result = result.append(parts.getOrDefault(placeholder, Component.text("{" + placeholder + "}")));

            lastEnd = matcher.end();
        }

        if (lastEnd < format.length()) {
            result = result.append(Component.text(format.substring(lastEnd)));
        }

        return result;
    }

    private Component buildEmojiComponent() {
        String emoji = emojiFor();
        return emoji.isEmpty() ? Component.empty() : Component.text(emoji);
    }

    private Component buildPlayerPrefix(Player player) {
        com.loki.lochat.LoChat loChat = (com.loki.lochat.LoChat) plugin;
        com.loki.lochat.gradient.GradientModule gradientModule = loChat.getGradientModule();

        if (gradientModule != null && gradientModule.isEnabled()) {
            String prefix = gradientModule.getPrefix(player);
            if (!prefix.isEmpty()) {
                return com.loki.lochat.utils.format.ChatFormatter.parse(prefix);
            }
        }

        return Component.empty();
    }

    private Component buildPlayerSuffix(Player player) {
        com.loki.lochat.LoChat loChat = (com.loki.lochat.LoChat) plugin;
        com.loki.lochat.gradient.GradientModule gradientModule = loChat.getGradientModule();

        if (gradientModule != null && gradientModule.isEnabled()) {
            String suffix = gradientModule.getSuffix(player);
            if (!suffix.isEmpty()) {
                return com.loki.lochat.utils.format.ChatFormatter.parse(suffix);
            }
        }

        return Component.empty();
    }

    private Component buildPrefix() {
        String text = prefixText();
        if (channel != null) {
            return ChatFormatter.parse(channelColor() + text);
        }
        com.loki.lochat.config.AppearanceConfig appearanceCfg = cfg.getAppearanceConfig();
        List<String> colors = isGlobal ? appearanceCfg.getGlobalColors() : appearanceCfg.getLocalColors();

        return GradientBuilder.build(text, colors);
    }

    private Component buildSeparator() {
        com.loki.lochat.config.AppearanceConfig appearanceCfg = cfg.getAppearanceConfig();
        String text = isGlobal ? appearanceCfg.getGlobalSeparatorText() : appearanceCfg.getLocalSeparatorText();
        String color = channel != null
                ? channelColor()
                : (isGlobal ? appearanceCfg.getGlobalSeparatorColor() : appearanceCfg.getLocalSeparatorColor());
        return Component.text(text, parseColor(color));
    }

    private Component buildPlayerComponent(Player player, Component sourceDisplayName) {
        Component displayName = sourceDisplayName != null ? sourceDisplayName : Component.text(player.getName());

        com.loki.lochat.config.AppearanceConfig appearanceCfg = cfg.getAppearanceConfig();

        if (!appearanceCfg.isHoverEnabled()) {
            return displayName.clickEvent(ClickEvent.suggestCommand("/msg " + player.getName() + " "));
        }

        List<String> hoverLines = appearanceCfg.getHoverFormat();
        if (hoverLines.isEmpty()) {
            hoverLines = List.of(
                "&#7858E9▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "&#B798A8✦ &f{player}",
                "",
                "&#9878C9⏱ &fПинг: &#7858E9{ping}ms",
                "&#9878C9♥ &fЗдоровье: &#7858E9{health}/20",
                "&#9878C9🍖 &fГолод: &#7858E9{food}/20",
                "",
                "&#7858E9▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "&#B798A8✦ &fНажми чтобы написать"
            );
        }

        net.kyori.adventure.text.TextComponent.Builder hoverBuilder = Component.text();

        for (int i = 0; i < hoverLines.size(); i++) {
            String line = hoverLines.get(i)
                    .replace("{player}",   player.getName())
                    .replace("{world}",    player.getWorld().getName())
                    .replace("{ping}",     String.valueOf(player.getPing()))
                    .replace("{gamemode}", player.getGameMode().name())
                    .replace("{health}",   String.valueOf(Math.round(player.getHealth())))
                    .replace("{food}",     String.valueOf(player.getFoodLevel()));

            hoverBuilder.append(com.loki.lochat.utils.format.ChatFormatter.parse(line));
            if (i < hoverLines.size() - 1) {
                hoverBuilder.append(Component.newline());
            }
        }

        return displayName
                .hoverEvent(HoverEvent.showText(hoverBuilder.build()))
                .clickEvent(ClickEvent.suggestCommand("/msg " + player.getName() + " "));
    }

    private void sendTranslationAsync(Player viewer, String plainText) {
        if (plainText.isBlank()) {
            return;
        }
        com.loki.lochat.LoChat loChat = (com.loki.lochat.LoChat) plugin;
        TranslationService ts = loChat.getTranslationService();
        String localeStr = viewer.getLocale();
        String viewerLang = "en";
        if (localeStr != null && !localeStr.isBlank()) {
            viewerLang = localeStr.split("[_\\-]")[0].toLowerCase();
        }
        final String lang = viewerLang;
        ts.translateAsync(plainText, lang)
                .thenAccept(translated -> {
                    if (translated.equals(plainText)) {
                        return;
                    }
                    String langName = LANGUAGE_NAMES.getOrDefault(lang, lang.toUpperCase(java.util.Locale.ROOT));
                    Component component = Component.text()
                            .append(com.loki.lochat.utils.format.ChatFormatter.parse(
                                    "&#AAAAAA[" + langName + "]&#FFFFFF "))
                            .append(Component.text(translated))
                            .build();
                    Msg.actionBar(viewer, component);
                });
    }

    private static final Map<String, String> LANGUAGE_NAMES = new HashMap<>();
    static {
        LANGUAGE_NAMES.put("en", "EN");
        LANGUAGE_NAMES.put("ru", "RU");
        LANGUAGE_NAMES.put("de", "DE");
        LANGUAGE_NAMES.put("fr", "FR");
        LANGUAGE_NAMES.put("es", "ES");
        LANGUAGE_NAMES.put("pt", "PT");
        LANGUAGE_NAMES.put("it", "IT");
        LANGUAGE_NAMES.put("nl", "NL");
        LANGUAGE_NAMES.put("pl", "PL");
        LANGUAGE_NAMES.put("uk", "UA");
        LANGUAGE_NAMES.put("zh", "CN");
        LANGUAGE_NAMES.put("ja", "JP");
        LANGUAGE_NAMES.put("ko", "KR");
        LANGUAGE_NAMES.put("ar", "AR");
        LANGUAGE_NAMES.put("tr", "TR");
        LANGUAGE_NAMES.put("vi", "VN");
        LANGUAGE_NAMES.put("th", "TH");
        LANGUAGE_NAMES.put("cs", "CS");
        LANGUAGE_NAMES.put("sv", "SV");
        LANGUAGE_NAMES.put("da", "DA");
        LANGUAGE_NAMES.put("fi", "FI");
        LANGUAGE_NAMES.put("el", "EL");
        LANGUAGE_NAMES.put("hu", "HU");
        LANGUAGE_NAMES.put("ro", "RO");
        LANGUAGE_NAMES.put("no", "NO");
        LANGUAGE_NAMES.put("sk", "SK");
        LANGUAGE_NAMES.put("bg", "BG");
        LANGUAGE_NAMES.put("hr", "HR");
        LANGUAGE_NAMES.put("sr", "SR");
    }

    private TextColor parseColor(String hex) {
        if (hex == null || hex.isBlank()) {
            return FALLBACK_COLOR;
        }
        String n = hex.startsWith("#") ? hex : "#" + hex;
        TextColor c = TextColor.fromHexString(n);
        return c != null ? c : FALLBACK_COLOR;
    }
}
