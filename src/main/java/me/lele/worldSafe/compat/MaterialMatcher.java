package me.lele.worldSafe.compat;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.Arrays;
import java.util.Locale;

public final class MaterialMatcher {

    private MaterialMatcher() {
    }

    public static boolean matches(Block block, String... aliases) {
        return block != null && matches(block.getType(), aliases);
    }

    public static boolean matches(BlockState state, String... aliases) {
        return state != null && matches(state.getType(), aliases);
    }

    public static boolean matches(Material material, String... aliases) {
        return material != null && matchesName(material.name(), aliases);
    }

    public static boolean matchesName(String materialName, String... aliases) {
        if (materialName == null || aliases == null) {
            return false;
        }
        String normalizedMaterial = normalize(materialName);
        return Arrays.stream(aliases)
                .filter(alias -> alias != null)
                .map(MaterialMatcher::normalize)
                .anyMatch(normalizedMaterial::equals);
    }

    public static boolean isBed(Block block) {
        return block != null && block.getType().name().endsWith("_BED");
    }

    private static String normalize(String name) {
        int namespaceSeparator = name.indexOf(':');
        String withoutNamespace = namespaceSeparator >= 0 ? name.substring(namespaceSeparator + 1) : name;
        return withoutNamespace.trim().replace('-', '_').toUpperCase(Locale.ROOT);
    }
}
