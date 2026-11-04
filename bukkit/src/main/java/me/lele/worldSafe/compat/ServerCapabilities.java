package me.lele.worldSafe.compat;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

public final class ServerCapabilities {

    public enum Capability {
        EXPLODED_BLOCK_STATE,
        RESPAWN_ANCHOR_CHARGES,
        PROJECTILE_HIT_BLOCK,
        PROJECTILE_HIT_CANCELLABLE
    }

    private final EnumSet<Capability> capabilities;
    private final Set<String> materialNames;
    private final Set<String> entityTypeNames;
    private final Method explodedBlockStateMethod;
    private final Method blockDataMethod;
    private final Method anchorChargesMethod;
    private final Method anchorMaximumChargesMethod;
    private final Method projectileHitBlockMethod;
    private final boolean paperServer;
    private final BiConsumer<Capability, Throwable> invocationFailureLogger;
    private final EnumSet<Capability> warnedInvocationFailures = EnumSet.noneOf(Capability.class);

    private ServerCapabilities(EnumSet<Capability> capabilities, Set<String> materialNames,
            Set<String> entityTypeNames, Method explodedBlockStateMethod, Method blockDataMethod,
            Method anchorChargesMethod, Method anchorMaximumChargesMethod, Method projectileHitBlockMethod,
            boolean paperServer, BiConsumer<Capability, Throwable> invocationFailureLogger) {
        this.capabilities = capabilities.clone();
        this.materialNames = Collections.unmodifiableSet(new LinkedHashSet<String>(materialNames));
        this.entityTypeNames = Collections.unmodifiableSet(new LinkedHashSet<String>(entityTypeNames));
        this.explodedBlockStateMethod = explodedBlockStateMethod;
        this.blockDataMethod = blockDataMethod;
        this.anchorChargesMethod = anchorChargesMethod;
        this.anchorMaximumChargesMethod = anchorMaximumChargesMethod;
        this.projectileHitBlockMethod = projectileHitBlockMethod;
        this.paperServer = paperServer;
        this.invocationFailureLogger = Objects.requireNonNull(invocationFailureLogger,
                "invocationFailureLogger");
    }

    public static ServerCapabilities detect() {
        return detect((capability, failure) -> { });
    }

    public static ServerCapabilities detect(BiConsumer<Capability, Throwable> invocationFailureLogger) {
        Method explodedState = findMethod(BlockExplodeEvent.class, "getExplodedBlockState");
        Method getBlockData = findMethod(Block.class, "getBlockData");
        Method getCharges = null;
        Method getMaximumCharges = null;
        try {
            Class<?> anchorClass = Class.forName("org.bukkit.block.data.type.RespawnAnchor", false,
                    ServerCapabilities.class.getClassLoader());
            getCharges = findMethod(anchorClass, "getCharges");
            getMaximumCharges = findMethod(anchorClass, "getMaximumCharges");
        } catch (ClassNotFoundException ignored) {
            // Expected on servers before 1.16.
        }
        Method getHitBlock = findMethod(ProjectileHitEvent.class, "getHitBlock");

        EnumSet<Capability> detected = EnumSet.noneOf(Capability.class);
        if (explodedState != null) {
            detected.add(Capability.EXPLODED_BLOCK_STATE);
        }
        if (getBlockData != null && getCharges != null && getMaximumCharges != null) {
            detected.add(Capability.RESPAWN_ANCHOR_CHARGES);
        }
        if (getHitBlock != null) {
            detected.add(Capability.PROJECTILE_HIT_BLOCK);
        }
        if (Cancellable.class.isAssignableFrom(ProjectileHitEvent.class)) {
            detected.add(Capability.PROJECTILE_HIT_CANCELLABLE);
        }

        Set<String> materials = new LinkedHashSet<String>();
        for (Material material : Material.values()) {
            materials.add(normalize(material.name()));
        }
        Set<String> entities = new LinkedHashSet<String>();
        for (EntityType type : EntityType.values()) {
            entities.add(normalize(type.name()));
        }
        return new ServerCapabilities(detected, materials, entities, explodedState, getBlockData,
                getCharges, getMaximumCharges, getHitBlock, classExists("io.papermc.paper.ServerBuildInfo"),
                invocationFailureLogger);
    }

    public static ServerCapabilities forTesting(Set<Capability> capabilities, Set<String> materialNames,
            Set<String> entityTypeNames) {
        EnumSet<Capability> copy = capabilities.isEmpty()
                ? EnumSet.noneOf(Capability.class) : EnumSet.copyOf(capabilities);
        return new ServerCapabilities(copy, normalizeAll(materialNames), normalizeAll(entityTypeNames),
                null, null, null, null, null, false, (capability, failure) -> { });
    }

    static ServerCapabilities forTestingWithMethods(Set<Capability> capabilities, Method explodedBlockStateMethod,
            Method blockDataMethod, Method anchorChargesMethod, Method anchorMaximumChargesMethod,
            Method projectileHitBlockMethod) {
        return forTestingWithMethods(capabilities, explodedBlockStateMethod, blockDataMethod,
                anchorChargesMethod, anchorMaximumChargesMethod, projectileHitBlockMethod,
                (capability, failure) -> { });
    }

    static ServerCapabilities forTestingWithMethods(Set<Capability> capabilities, Method explodedBlockStateMethod,
            Method blockDataMethod, Method anchorChargesMethod, Method anchorMaximumChargesMethod,
            Method projectileHitBlockMethod, BiConsumer<Capability, Throwable> invocationFailureLogger) {
        return new ServerCapabilities(capabilities.isEmpty() ? EnumSet.noneOf(Capability.class)
                : EnumSet.copyOf(capabilities), Collections.<String>emptySet(), Collections.<String>emptySet(),
                explodedBlockStateMethod, blockDataMethod, anchorChargesMethod, anchorMaximumChargesMethod,
                projectileHitBlockMethod, false, invocationFailureLogger);
    }

    public boolean has(Capability capability) {
        return capabilities.contains(capability);
    }

    public boolean isPaperServer() {
        return paperServer;
    }

    public boolean hasMaterial(String... aliases) {
        return containsAlias(materialNames, aliases, true);
    }

    public boolean hasEntityType(String... aliases) {
        return containsAlias(entityTypeNames, aliases, false);
    }

    public BlockState getExplodedBlockState(BlockExplodeEvent event) {
        Object value = invoke(Capability.EXPLODED_BLOCK_STATE, explodedBlockStateMethod, event);
        return value instanceof BlockState ? (BlockState) value : null;
    }

    public Block getProjectileHitBlock(ProjectileHitEvent event) {
        Object value = invoke(Capability.PROJECTILE_HIT_BLOCK, projectileHitBlockMethod, event);
        return value instanceof Block ? (Block) value : null;
    }

    public boolean cancelIfPossible(Event event) {
        if (!(event instanceof Cancellable)) {
            return false;
        }
        ((Cancellable) event).setCancelled(true);
        return true;
    }

    public int getRespawnAnchorCharges(Block block) {
        Object blockData = invoke(Capability.RESPAWN_ANCHOR_CHARGES, blockDataMethod, block);
        Object value = invoke(Capability.RESPAWN_ANCHOR_CHARGES, anchorChargesMethod, blockData);
        return value instanceof Number ? ((Number) value).intValue() : -1;
    }

    public int getRespawnAnchorMaximumCharges(Block block) {
        Object blockData = invoke(Capability.RESPAWN_ANCHOR_CHARGES, blockDataMethod, block);
        Object value = invoke(Capability.RESPAWN_ANCHOR_CHARGES, anchorMaximumChargesMethod, blockData);
        return value instanceof Number ? ((Number) value).intValue() : -1;
    }

    private static Method findMethod(Class<?> type, String name) {
        try {
            return type.getMethod(name);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className, false, ServerCapabilities.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        } catch (LinkageError ignored) {
            return false;
        }
    }

    private Object invoke(Capability capability, Method method, Object target) {
        if (method == null || target == null) {
            return null;
        }
        try {
            return method.invoke(target);
        } catch (IllegalAccessException exception) {
            reportInvocationFailure(capability, exception);
            return null;
        } catch (InvocationTargetException exception) {
            reportInvocationFailure(capability, exception);
            return null;
        } catch (IllegalArgumentException exception) {
            reportInvocationFailure(capability, exception);
            return null;
        }
    }

    private void reportInvocationFailure(Capability capability, Throwable failure) {
        synchronized (warnedInvocationFailures) {
            if (!warnedInvocationFailures.add(capability)) {
                return;
            }
        }
        invocationFailureLogger.accept(capability, failure);
    }

    private static boolean containsAlias(Set<String> names, String[] aliases, boolean material) {
        if (aliases == null) {
            return false;
        }
        for (String name : names) {
            if (material ? MaterialMatcher.matchesName(name, aliases) : EntityTypeMatcher.matchesName(name, aliases)) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> normalizeAll(Set<String> names) {
        Set<String> normalized = new LinkedHashSet<String>();
        for (String name : names) {
            if (name != null) {
                normalized.add(normalize(name));
            }
        }
        return normalized;
    }

    private static String normalize(String name) {
        return name.trim().toUpperCase(Locale.ROOT);
    }
}
