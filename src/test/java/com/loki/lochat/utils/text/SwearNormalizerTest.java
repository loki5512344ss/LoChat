package com.loki.lochat.utils.text;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwearNormalizerTest {

    @Test
    void normalizeForSearch_stripsSeparatorsInsideWord() {
        assertEquals("сука", SwearNormalizer.normalizeForSearch("с.у.к.а"));
        assertEquals("сука", SwearNormalizer.normalizeForSearch("с-у-к-а"));
        assertEquals("сука", SwearNormalizer.normalizeForSearch("с у к а"));
    }

    @Test
    void normalizeForSearch_collapsesRepeatedLetters() {
        assertEquals("сука", SwearNormalizer.normalizeForSearch("сууука"));
        assertEquals("сука", SwearNormalizer.normalizeForSearch("сссуккаа"));
    }

    @Test
    void normalizeForSearch_appliesLeetSubstitution() {
        assertEquals("сока", SwearNormalizer.normalizeForSearch("с0ка"));
        assertEquals("мет", SwearNormalizer.normalizeForSearch("м3т"));
        assertEquals("игра", SwearNormalizer.normalizeForSearch("1гра"));
        assertEquals("пизда", SwearNormalizer.normalizeForSearch("п!зда"));
        assertEquals("бан", SwearNormalizer.normalizeForSearch("6ан"));
        assertEquals("хуй", SwearNormalizer.normalizeForSearch("хyй"));
        assertEquals("хуй", SwearNormalizer.normalizeForSearch("ХУЙ"));
    }

    @Test
    void normalizeForSearch_mixedObfuscation() {
        assertEquals("сука", SwearNormalizer.normalizeForSearch("с-уу.к.а"));
        assertEquals("сука", SwearNormalizer.normalizeForSearch("СУ К А"));
    }

    @Test
    void normalizeForSearch_preservesCleanText() {
        assertEquals("приветмир", SwearNormalizer.normalizeForSearch("привет мир"));
    }

    @Test
    void ahoCorasick_detectsObfuscatedWord() {
        AhoCorasick ac = new AhoCorasick(List.of("сука"));
        assertTrue(ac.matches(SwearNormalizer.normalizeForSearch("с.у.к.а")));
        assertTrue(ac.matches(SwearNormalizer.normalizeForSearch("сууука")));
        assertTrue(ac.matches(SwearNormalizer.normalizeForSearch("СУ К А")));
        assertTrue(ac.matches(SwearNormalizer.normalizeForSearch("эта с.у.к.а сволочь")));
        assertFalse(ac.matches(SwearNormalizer.normalizeForSearch("чистое сообщение")));
    }

    @Test
    void wordPattern_matchesObfuscatedForms() {
        var pattern = SwearNormalizer.wordPattern("сука");
        assertTrue(pattern.matcher("с.у.к.а").find());
        assertTrue(pattern.matcher("сука").find());
        assertTrue(pattern.matcher("СУУУКА").find());
        assertTrue(pattern.matcher("с у к а").find());
        assertTrue(pattern.matcher("это с-у-к-а сообщение").find());
    }

    @Test
    void wordPattern_masksOriginalText() {
        var pattern = SwearNormalizer.wordPattern("сука");
        Matcher matcher = pattern.matcher("это с.у.к.а сообщение");
        String masked = matcher.replaceAll("***");
        assertEquals("это *** сообщение", masked);
    }

    @Test
    void wordPattern_doesNotMatchSimilarWord() {
        var pattern = SwearNormalizer.wordPattern("сука");
        assertFalse(pattern.matcher("сучка").find());
        assertFalse(pattern.matcher("сук").find());
    }
}