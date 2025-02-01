package me.despical.kotl.language;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 31.01.2025
 */
public enum Locale {

    ENGLISH("English", "en_GB", "default", "english", "en"),
    TURKISH("Turkish", "tr_TR", "turkish", "türkçe", "turkce", "tr"),
    GERMAN("German", "de_DE", "deutsch", "german", "de");

    private final String name;
    private final String prefix;
    private final List<String> aliases;

    Locale(String name, String prefix, String... aliases) {
        this.name = name;
        this.prefix = prefix;
        this.aliases = List.of(aliases);
    }

    public static Locale getByName(String name) {
        for (Locale locale : values()) {
            if (locale.isSameWith(name)) {
                return locale;
            }
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public boolean isSameWith(String name) {
        return name.equalsIgnoreCase(this.name) || aliases.contains(name);
    }
}
