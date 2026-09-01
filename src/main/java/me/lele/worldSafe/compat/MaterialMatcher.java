package me.lele.worldSafe.compat;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

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
        String normalizedMaterial = canonicalize(normalize(materialName));
        for (String alias : aliases) {
            if (alias != null && normalizedMaterial.equals(canonicalize(normalize(alias)))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBed(Block block) {
        return block != null && isBedName(block.getType().name());
    }

    private static String normalize(String name) {
        int namespaceSeparator = name.indexOf(':');
        String withoutNamespace = namespaceSeparator >= 0 ? name.substring(namespaceSeparator + 1) : name;
        return withoutNamespace.trim().replace('-', '_').toUpperCase(Locale.ROOT);
    }

    private static String canonicalize(String name) {
        if ("SOIL".equals(name) || "LEGACY_SOIL".equals(name) || "FARMLAND".equals(name)) {
            return "FARMLAND";
        }
        if ("GRASS".equals(name) || "LEGACY_GRASS".equals(name) || "GRASS_BLOCK".equals(name)) {
            return "GRASS_BLOCK";
        }
        if ("WEB".equals(name) || "LEGACY_WEB".equals(name) || "COBWEB".equals(name)) {
            return "COBWEB";
        }
        if (isBedName(name)) {
            return "BED_BLOCK";
        }
        return name;
    }

    private static boolean isBedName(String name) {
        return "BED".equals(name) || "BED_BLOCK".equals(name) || "LEGACY_BED".equals(name)
                || "LEGACY_BED_BLOCK".equals(name) || name.endsWith("_BED");
    }
}
