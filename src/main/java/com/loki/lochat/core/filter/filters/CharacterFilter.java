package com.loki.lochat.core.filter.filters;

import com.loki.lochat.config.FiltersConfig;

import java.util.regex.Pattern;

public class CharacterFilter {
    private final boolean enabled;
    private final int maxRepeating;

    public CharacterFilter(FiltersConfig filters) {
        this(filters.isRepeatEnabled(), filters.getRepeatMaxRepeats());
    }

    public CharacterFilter(boolean enabled, int maxRepeating) {
        this.enabled = enabled;
        this.maxRepeating = maxRepeating;
    }

    public String filter(String message) {
        if (!enabled || maxRepeating <= 0) {
            return message;
        }

        Pattern pattern = Pattern.compile("(.)\\1{" + maxRepeating + ",}");
        return pattern.matcher(message).replaceAll(m -> {
            String repeated = m.group(1);
            return repeated.repeat(maxRepeating);
        });
    }
}
