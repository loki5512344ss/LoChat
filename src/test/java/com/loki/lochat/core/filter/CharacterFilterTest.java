package com.loki.lochat.core.filter;

import com.loki.lochat.core.filter.filters.CharacterFilter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharacterFilterTest {

    @Test
    void filter_limitsRepeatingChars() {
        CharacterFilter filter = new CharacterFilter(true, 3);
        assertEquals("привееет", filter.filter("привеееееет"));
    }

    @Test
    void filter_noChangeWhenUnderLimit() {
        CharacterFilter filter = new CharacterFilter(true, 3);
        assertEquals("привет", filter.filter("привет"));
    }

    @Test
    void filter_shortRepeatsAllowed() {
        CharacterFilter filter = new CharacterFilter(true, 3);
        assertEquals("ууупс", filter.filter("ууупс"));
    }

    @Test
    void filter_disabledReturnsOriginal() {
        CharacterFilter filter = new CharacterFilter(false, 3);
        assertEquals("мноооого букв", filter.filter("мноооого букв"));
    }

    @Test
    void filter_zeroMaxDisabled() {
        CharacterFilter filter = new CharacterFilter(true, 0);
        assertEquals("теееекст", filter.filter("теееекст"));
    }

    @Test
    void filter_defaultConfig() {
        CharacterFilter filter = new CharacterFilter(true, 3);
        assertEquals("тееест", filter.filter("теееест"));
    }

    @Test
    void filter_differentMax() {
        CharacterFilter filter = new CharacterFilter(true, 1);
        assertEquals("ап", filter.filter("ааап"));
    }

    @Test
    void filter_multiplePatterns() {
        CharacterFilter filter = new CharacterFilter(true, 2);
        assertEquals("aa bb", filter.filter("aaa bbb"));
    }
}
