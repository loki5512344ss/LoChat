package com.loki.lochat.utils.text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Нормализация поискового текста для anti-swear.
 * Борется с обфускацией: leet/substitution (q→р, 0→о), повторами букв (сууука),
 * разделителями внутри слова (с.у.к.а, с у к а).
 */
public final class SwearNormalizer {

    /**
     * Таблица подстановок: «сырой» символ → каноническая (нижний регистр) русская буква.
     * Latin-кириллические похожие (a→а, e→е, p→р...) и leet-цифры/символы.
     */
    private static final Map<Character, Character> SUBSTITUTIONS = new HashMap<>();

    /**
     * Символы-разделители, которые вырезаются внутри слова (точки, дефисы, пробелы и т.п.).
     * Не должно пересекаться с ключами SUBSTITUTIONS.
     */
    private static final Set<Character> NOISE_CHARS = new HashSet<>();

    /**
     * Обратная карта: каноническая буква → все сырые варианты её написания (включая саму букву).
     */
    private static final Map<Character, Set<Character>> VARIANTS = new HashMap<>();

    private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

    static {
        // Latin-кириллические омоглифы
        put('a', 'а');
        put('e', 'е');
        put('o', 'о');
        put('p', 'р');
        put('c', 'с');
        put('y', 'у');
        put('x', 'х');
        put('k', 'к');
        put('m', 'м');
        put('h', 'н');
        put('n', 'н');
        put('t', 'т');
        put('b', 'в');
        put('r', 'г');
        put('d', 'д');
        put('u', 'у');
        put('g', 'г');
        put('i', 'и');
        put('l', 'л');
        put('s', 'с');
        put('z', 'з');
        put('q', 'р');

        // Leet-цифры и символы
        put('@', 'а');
        put('0', 'о');
        put('3', 'е');
        put('1', 'и');
        put('6', 'б');
        put('4', 'а');
        put('5', 'с');
        put('7', 'т');
        put('8', 'б');
        put('9', 'г');
        put('2', 'з');
        put('$', 'с');
        put('!', 'и');
        put('|', 'л');

        NOISE_CHARS.add(' ');
        NOISE_CHARS.add('\t');
        NOISE_CHARS.add('\n');
        NOISE_CHARS.add('\r');
        NOISE_CHARS.add('.');
        NOISE_CHARS.add(',');
        NOISE_CHARS.add(';');
        NOISE_CHARS.add(':');
        NOISE_CHARS.add('?');
        NOISE_CHARS.add('-');
        NOISE_CHARS.add('_');
        NOISE_CHARS.add('*');
        NOISE_CHARS.add('+');
        NOISE_CHARS.add('/');
        NOISE_CHARS.add('\\');
        NOISE_CHARS.add('~');
        NOISE_CHARS.add('^');
        NOISE_CHARS.add('#');
        NOISE_CHARS.add('&');
        NOISE_CHARS.add('%');
        NOISE_CHARS.add('=');
        NOISE_CHARS.add('\'');
        NOISE_CHARS.add('"');
        NOISE_CHARS.add('`');
        NOISE_CHARS.add('(');
        NOISE_CHARS.add(')');
        NOISE_CHARS.add('[');
        NOISE_CHARS.add(']');
        NOISE_CHARS.add('{');
        NOISE_CHARS.add('}');
        NOISE_CHARS.add('<');
        NOISE_CHARS.add('>');
        NOISE_CHARS.add('·');
        NOISE_CHARS.add('«');
        NOISE_CHARS.add('»');
        NOISE_CHARS.add('„');
        NOISE_CHARS.add('“');
        NOISE_CHARS.add('”');
        NOISE_CHARS.add('…');
    }

    private SwearNormalizer() {
    }

    private static void put(char raw, char letter) {
        SUBSTITUTIONS.put(raw, letter);
        VARIANTS.computeIfAbsent(letter, k -> new HashSet<>()).add(raw);
        VARIANTS.computeIfAbsent(letter, k -> new HashSet<>()).add(letter);
    }

    /**
     * Нормализация одного символа: нижний регистр + замена омоглифов/leet.
     */
    public static char normalize(char c) {
        char lower = Character.toLowerCase(c);
        return SUBSTITUTIONS.getOrDefault(lower, lower);
    }

    /**
     * Приводит текст к «поисковому» виду: подстановки + удаление разделителей + схлопывание
     * повторяющихся букв. Результат — поток канонических букв, по которому можно искать слова.
     */
    public static String normalizeForSearch(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        char prev = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (NOISE_CHARS.contains(c)) {
                continue;
            }
            char n = normalize(c);
            if (n == prev) {
                continue;
            }
            sb.append(n);
            prev = n;
        }
        return sb.toString();
    }

    /**
     * Строит tolerant-регулярку для поиска конкретного слова в оригинальном тексте:
     * каждая буква — «один или несколько» вариантов написания, между буквами допускаются любые
     * разделители (точки, пробелы и т.п.). Используется для маскирования в replace-режиме.
     */
    public static Pattern wordPattern(String normalizedWord) {
        return PATTERN_CACHE.computeIfAbsent(normalizedWord, SwearNormalizer::buildPattern);
    }

    private static Pattern buildPattern(String word) {
        StringBuilder sb = new StringBuilder("(?iu)");
        for (int i = 0; i < word.length(); i++) {
            char letter = word.charAt(i);
            Set<Character> variants = VARIANTS.getOrDefault(letter, Set.of(letter));
            sb.append("(?:");
            boolean first = true;
            for (char v : variants) {
                if (!first) {
                    sb.append('|');
                }
                sb.append(Pattern.quote(String.valueOf(v)));
                first = false;
            }
            sb.append(")+");
            if (i < word.length() - 1) {
                sb.append("(?:");
                boolean firstNoise = true;
                for (char n : NOISE_CHARS) {
                    if (!firstNoise) {
                        sb.append('|');
                    }
                    sb.append(Pattern.quote(String.valueOf(n)));
                    firstNoise = false;
                }
                sb.append(")*");
            }
        }
        return Pattern.compile(sb.toString());
    }
}