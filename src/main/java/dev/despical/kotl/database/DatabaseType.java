package dev.despical.kotl.database;

import java.util.Arrays;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 14.12.2025
 */
public enum DatabaseType {

    FLAT_FILE("Flat File", "flat", "flat_file", "default"),
    MYSQL("MySQL", "mysql");

    private final List<String> names;

    DatabaseType(String... names) {
        this.names = Arrays.asList(names);
    }

    public String getName() {
        return names.getFirst();
    }

    public static DatabaseType getByName(String name) {
        return Arrays.stream(values())
            .filter(type -> type.names.contains(name))
            .findFirst()
            .orElse(null);
    }
}
