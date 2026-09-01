package me.lele.worldSafe.compat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MinecraftVersion implements Comparable<MinecraftVersion> {

    public static final MinecraftVersion V1_8_8 = new MinecraftVersion(1, 8, 8);
    public static final MinecraftVersion V1_13 = new MinecraftVersion(1, 13, 0);
    public static final MinecraftVersion V1_14 = new MinecraftVersion(1, 14, 0);
    public static final MinecraftVersion V1_16 = new MinecraftVersion(1, 16, 0);
    public static final MinecraftVersion V1_20_3 = new MinecraftVersion(1, 20, 3);
    public static final MinecraftVersion V1_21 = new MinecraftVersion(1, 21, 0);
    public static final MinecraftVersion V26_2 = new MinecraftVersion(26, 2, 0);

    private static final Pattern MC_VERSION = Pattern.compile("MC:\\s*(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern VERSION = Pattern.compile("(?:^|[^0-9])(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?");

    private final int major;
    private final int minor;
    private final int patch;

    public MinecraftVersion(int major, int minor, int patch) {
        if (major < 0 || minor < 0 || patch < 0) {
            throw new IllegalArgumentException("Version components must be non-negative");
        }
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public static MinecraftVersion parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Minecraft version is missing");
        }

        Matcher matcher = MC_VERSION.matcher(value);
        if (!matcher.find()) {
            matcher = VERSION.matcher(value.trim());
            if (!matcher.find()) {
                throw new IllegalArgumentException("Cannot parse Minecraft version from: " + value);
            }
        }

        return new MinecraftVersion(parsePart(matcher.group(1)), parsePart(matcher.group(2)),
                parsePart(matcher.group(3)));
    }

    private static int parsePart(String value) {
        return value == null ? 0 : Integer.parseInt(value);
    }

    public boolean isAtLeast(MinecraftVersion other) {
        return compareTo(other) >= 0;
    }

    @Override
    public int compareTo(MinecraftVersion other) {
        int result = Integer.compare(major, other.major);
        if (result != 0) {
            return result;
        }
        result = Integer.compare(minor, other.minor);
        return result != 0 ? result : Integer.compare(patch, other.patch);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MinecraftVersion)) {
            return false;
        }
        MinecraftVersion version = (MinecraftVersion) other;
        return major == version.major && minor == version.minor && patch == version.patch;
    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + patch;
        return result;
    }

    @Override
    public String toString() {
        if (patch != 0) {
            return major + "." + minor + "." + patch;
        }
        return major + "." + minor;
    }
}
