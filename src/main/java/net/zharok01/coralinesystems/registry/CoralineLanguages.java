package net.zharok01.coralinesystems.registry;

import java.util.Set;

public class CoralineLanguages {

    public static final Set<String> SUPPORTED_LANGUAGES = Set.of(
            "en_us",
            "ru_ru"
    );

    public static boolean isLanguageSupported(String langCode) {
        return SUPPORTED_LANGUAGES.contains(langCode.toLowerCase());
    }
}