package me.lele.worldSafe.fabric;

/** The only boundary between the Java 8 service layer and a selected game module. */
public interface NativeAccess {
    Object server(Object world);
    String dimension(Object world);
    String entityType(Object entity);
    Object projectileOwner(Object entity);
    String blockType(Object world, Object position);
    String stateType(Object state);
    boolean isWitherRoseItem(Object entity);
    boolean isProjectile(Object entity);
    Object commandServer(Object source);
    boolean mayManage(Object source);
    void reply(Object source, String message);
}
