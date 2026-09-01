package me.lele.worldSafe.compat;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.Locale;

public final class EntityTypeMatcher {

    private EntityTypeMatcher() {
    }

    public static boolean matches(Entity entity, String... aliases) {
        return entity != null && matches(entity.getType(), aliases);
    }

    public static boolean matches(EntityType type, String... aliases) {
        return type != null && matchesName(type.name(), aliases);
    }

    public static boolean matchesName(String typeName, String... aliases) {
        if (typeName == null || aliases == null) {
            return false;
        }
        String normalizedType = normalize(typeName);
        return Arrays.stream(aliases)
                .filter(alias -> alias != null)
                .map(EntityTypeMatcher::normalize)
                .anyMatch(normalizedType::equals);
    }

    private static String normalize(String name) {
        int namespaceSeparator = name.indexOf(':');
        String withoutNamespace = namespaceSeparator >= 0 ? name.substring(namespaceSeparator + 1) : name;
        return withoutNamespace.trim().replace('-', '_').toUpperCase(Locale.ROOT);
    }
}
