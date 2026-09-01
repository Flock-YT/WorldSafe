package me.lele.worldSafe.compat;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

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
        String normalizedType = canonicalize(normalize(typeName));
        for (String alias : aliases) {
            if (alias != null && normalizedType.equals(canonicalize(normalize(alias)))) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String name) {
        int namespaceSeparator = name.indexOf(':');
        String withoutNamespace = namespaceSeparator >= 0 ? name.substring(namespaceSeparator + 1) : name;
        return withoutNamespace.trim().replace('-', '_').toUpperCase(Locale.ROOT);
    }

    private static String canonicalize(String name) {
        if ("SNOWMAN".equals(name) || "SNOW_GOLEM".equals(name)) {
            return "SNOW_GOLEM";
        }
        if ("ENDER_CRYSTAL".equals(name) || "END_CRYSTAL".equals(name)) {
            return "END_CRYSTAL";
        }
        if ("PRIMED_TNT".equals(name) || "TNT".equals(name)) {
            return "TNT";
        }
        if ("MINECART_TNT".equals(name) || "TNT_MINECART".equals(name)) {
            return "TNT_MINECART";
        }
        if ("SULPHUR_CUBE".equals(name) || "SULFUR_CUBE".equals(name)) {
            return "SULFUR_CUBE";
        }
        return name;
    }
}
