package me.lele.worldSafe.fabric.driver;

public interface NativeFixture {
    String game();
    Object server();
    void prepare() throws Exception;
    java.util.Map<String, Boolean> managementChecks() throws Exception;
    Object primaryWorld();
    Object secondaryWorld();
    int command(String command) throws Exception;
    BlastObservation blast(Object world, String source) throws Exception;
    java.util.Map<String, String> explosionFeatures();
    java.util.Set<String> mutationFeatures();
    boolean mutation(Object world, String feature) throws Exception;
    boolean hasWindCharges();
    BlastObservation windImpact(Object world, boolean breeze) throws Exception;

    final class BlastObservation {
        public final int destroyedBlocks;
        public final double initialHealth;
        public final double health;
        public final double knockback;
        public final Boolean sourceRetained;

        public BlastObservation(int destroyedBlocks, double initialHealth, double health, double knockback, Boolean sourceRetained) {
            this.destroyedBlocks = destroyedBlocks;
            this.initialHealth = initialHealth;
            this.health = health;
            this.knockback = knockback;
            this.sourceRetained = sourceRetained;
        }
    }
}
