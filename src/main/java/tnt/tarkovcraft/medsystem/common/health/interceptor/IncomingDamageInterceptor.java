package tnt.tarkovcraft.medsystem.common.health.interceptor;

public interface IncomingDamageInterceptor {

    // TODO pre armor reduction processing - probably using writable context
    boolean preprocess(float rawDamage);

    // TODO damage result
    float get(float incomingDamage);
}
