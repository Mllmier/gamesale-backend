package com.backend.gamesales.Config;

import org.springframework.stereotype.Component;
import org.springframework.core.convert.converter.Converter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class StringToLongSetConverter implements Converter<String, Set<Long>> {

    @Override
    public Set<Long> convert(String source) {
        if (source == null || source.isBlank()) return Set.of();

        String cleaned = source.replaceAll("[\\[\\]\\s]", "");
        return Arrays.stream(cleaned.split(","))
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }
}