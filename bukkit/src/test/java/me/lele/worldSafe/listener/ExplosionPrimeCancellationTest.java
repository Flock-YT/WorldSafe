package me.lele.worldSafe.listener;

import me.lele.worldSafe.listener.blocks.explosioncancel.TNTExplosionCancelListener;
import me.lele.worldSafe.listener.entities.explosioncancel.CreeperExplosionCancelListener;
import me.lele.worldSafe.listener.entities.explosioncancel.EndCrystalExplosionCancelListener;
import me.lele.worldSafe.listener.entities.explosioncancel.GhastExplosionCancelListener;
import me.lele.worldSafe.listener.entities.explosioncancel.SulfurCubeExplosionCancelListener;
import me.lele.worldSafe.listener.entities.explosioncancel.WitherExplosionCancelListener;
import me.lele.worldSafe.listener.entities.other.BreezeWindChargeImpactCancelListener;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExplosionPrimeCancellationTest {

    @Test
    void tntAndMinecartAliasesCancelPrimeAndLateFallback() {
        World enabled = world("enabled");
        TNTExplosionCancelListener listener = new TNTExplosionCancelListener(Collections.singletonList("enabled"));

        for (String alias : Arrays.asList("PRIMED_TNT", "TNT", "MINECART_TNT", "TNT_MINECART")) {
            Entity entity = entity(enabled, alias);
            assertCancelled(listener::onTNTPrime, entity, true);
            assertLateCancelled(listener::onTNTExplode, entity, true);
        }
        assertCancelled(listener::onTNTPrime, entity(world("other"), "PRIMED_TNT"), false);
    }

    @Test
    void creeperCancelsPrimeAndLateFallbackOnlyInConfiguredWorld() {
        CreeperExplosionCancelListener listener = new CreeperExplosionCancelListener(
                Collections.singletonList("enabled"));

        assertCancelled(listener::onCreeperPrime, entity(world("enabled"), "CREEPER"), true);
        assertCancelled(listener::onCreeperPrime, entity(world("other"), "CREEPER"), false);
        assertLateCancelled(listener::onCreeperExplode, entity(world("enabled"), "CREEPER"), true);
    }

    @Test
    void endCrystalAliasesCancelPrimeAndLateFallback() {
        World enabled = world("enabled");
        EndCrystalExplosionCancelListener listener = new EndCrystalExplosionCancelListener(
                Collections.singletonList("enabled"));

        for (String alias : Arrays.asList("ENDER_CRYSTAL", "END_CRYSTAL")) {
            Entity entity = entity(enabled, alias);
            assertCancelled(listener::onExplosionPrime, entity, true);
            assertLateCancelled(listener::onEntityExplode, entity, true);
        }
        assertCancelled(listener::onExplosionPrime, entity(world("other"), "ENDER_CRYSTAL"), false);
    }

    @Test
    void ghastFireballsRequireGhastShooterAndKeepDirectDamageProtection() {
        World enabled = world("enabled");
        GhastExplosionCancelListener listener = new GhastExplosionCancelListener(
                Collections.singletonList("enabled"));
        Fireball ghastFireball = fireball(enabled, mock(Ghast.class));

        assertCancelled(listener::onFireballPrime, ghastFireball, true);
        assertLateCancelled(listener::onFireballExplode, ghastFireball, true);

        Entity victim = entity(enabled, "PLAYER");
        EntityDamageByEntityEvent damage = new EntityDamageByEntityEvent(
                ghastFireball, victim, DamageCause.PROJECTILE, 2.0);
        listener.onEntityDamageByEntity(damage);
        assertTrue(damage.isCancelled());

        Fireball playerFireball = fireball(enabled, mock(Player.class));
        assertCancelled(listener::onFireballPrime, playerFireball, false);
        assertLateCancelled(listener::onFireballExplode, playerFireball, false);

        assertCancelled(listener::onFireballPrime, fireball(world("other"), mock(Ghast.class)), false);

        Entity nonFireball = entity(enabled, "FIREBALL");
        assertLateCancelled(listener::onFireballExplode, nonFireball, false);
        EntityDamageByEntityEvent nonFireballDamage = new EntityDamageByEntityEvent(
                nonFireball, victim, DamageCause.PROJECTILE, 2.0);
        listener.onEntityDamageByEntity(nonFireballDamage);
        assertFalse(nonFireballDamage.isCancelled());
    }

    @Test
    void witherAndSkullCancelPrimeAndLateFallback() {
        World enabled = world("enabled");
        WitherExplosionCancelListener listener = new WitherExplosionCancelListener(
                Collections.singletonList("enabled"));

        for (String alias : Arrays.asList("WITHER", "WITHER_SKULL")) {
            Entity entity = entity(enabled, alias);
            assertCancelled(listener::onExplosionPrime, entity, true);
            assertLateCancelled(listener::onExplode, entity, true);
        }
        assertCancelled(listener::onExplosionPrime, entity(world("other"), "WITHER"), false);
    }

    @Test
    void breezeWindChargeCancelsPrimeAndLateFallback() {
        BreezeWindChargeImpactCancelListener listener = new BreezeWindChargeImpactCancelListener(
                Collections.singletonList("enabled"));

        assertCancelled(listener::onBreezeWindChargePrime,
                entity(world("enabled"), "BREEZE_WIND_CHARGE"), true);
        assertCancelled(listener::onBreezeWindChargePrime,
                entity(world("other"), "BREEZE_WIND_CHARGE"), false);
        assertLateCancelled(listener::onBreezeWindChargeExplosion,
                entity(world("enabled"), "BREEZE_WIND_CHARGE"), true);
    }

    @Test
    void sulfurCubeAliasesCancelPrimeAndLateFallback() {
        World enabled = world("enabled");
        SulfurCubeExplosionCancelListener listener = new SulfurCubeExplosionCancelListener(
                Collections.singletonList("enabled"));

        for (String alias : Arrays.asList("SULFUR_CUBE", "SULPHUR_CUBE")) {
            Entity entity = entity(enabled, alias);
            assertCancelled(listener::onSulfurCubePrime, entity, true);
            assertLateCancelled(listener::onSulfurCubeExplosion, entity, true);
        }
        assertCancelled(listener::onSulfurCubePrime, entity(world("other"), "SULFUR_CUBE"), false);
    }

    @Test
    void everyPrimeHandlerRunsAtHighestPriority() {
        for (Class<? extends Listener> listenerClass : Arrays.<Class<? extends Listener>>asList(
                TNTExplosionCancelListener.class,
                CreeperExplosionCancelListener.class,
                EndCrystalExplosionCancelListener.class,
                GhastExplosionCancelListener.class,
                WitherExplosionCancelListener.class,
                BreezeWindChargeImpactCancelListener.class,
                SulfurCubeExplosionCancelListener.class)) {
            int primeHandlers = 0;
            for (Method method : listenerClass.getDeclaredMethods()) {
                if (Arrays.equals(method.getParameterTypes(), new Class<?>[] {ExplosionPrimeEvent.class})) {
                    EventHandler annotation = method.getAnnotation(EventHandler.class);
                    assertEquals(EventPriority.HIGHEST, annotation.priority(), listenerClass.getSimpleName());
                    primeHandlers++;
                }
            }
            assertEquals(1, primeHandlers, listenerClass.getSimpleName());
        }
    }

    private void assertCancelled(Consumer<ExplosionPrimeEvent> handler, Entity entity, boolean expected) {
        ExplosionPrimeEvent event = new ExplosionPrimeEvent(entity, 4.0f, false);
        handler.accept(event);
        assertEquals(expected, event.isCancelled());
    }

    private void assertLateCancelled(Consumer<EntityExplodeEvent> handler, Entity entity, boolean expected) {
        EntityExplodeEvent event = new EntityExplodeEvent(entity, new Location(entity.getWorld(), 0, 64, 0),
                new ArrayList<org.bukkit.block.Block>(), 1.0f);
        handler.accept(event);
        assertEquals(expected, event.isCancelled());
    }

    private Entity entity(World world, String typeName) {
        Entity entity = mock(Entity.class);
        EntityType type = entityType(typeName);
        when(entity.getType()).thenReturn(type);
        when(entity.getWorld()).thenReturn(world);
        return entity;
    }

    private Fireball fireball(World world, org.bukkit.projectiles.ProjectileSource shooter) {
        Fireball fireball = mock(Fireball.class);
        when(fireball.getType()).thenReturn(EntityType.FIREBALL);
        when(fireball.getWorld()).thenReturn(world);
        when(fireball.getShooter()).thenReturn(shooter);
        return fireball;
    }

    private EntityType entityType(String name) {
        try {
            return EntityType.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            EntityType type = mock(EntityType.class);
            when(type.name()).thenReturn(name);
            return type;
        }
    }

    private World world(String name) {
        World world = mock(World.class);
        when(world.getName()).thenReturn(name);
        return world;
    }
}
