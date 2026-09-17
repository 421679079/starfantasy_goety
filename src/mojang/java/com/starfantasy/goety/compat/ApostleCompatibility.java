package com.starfantasy.goety.compat;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.lang.reflect.Method;

/** Optional access to Revelation's synced Apollyon flag, without linking its classes. */
public final class ApostleCompatibility {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ClassValue<RevelationFlag> FLAGS = new ClassValue<>() {
        @Override protected RevelationFlag computeValue(Class<?> type) {
            try {
                return new RevelationFlag(type.getMethod("allTitlesApostle_1_20_1$isApollyon"));
            } catch (NoSuchMethodException ignored) {
                return new RevelationFlag(null);
            }
        }
    };

    private ApostleCompatibility() {}

    public static boolean original(Apostle entity) {
        return entity != null && entity.getClass() == Apostle.class && !isRevelationApollyon(entity);
    }

    public static boolean isRevelationApollyon(Object entity) {
        return entity != null && FLAGS.get(entity.getClass()).read(entity);
    }

    private static final class RevelationFlag {
        private final Method getter;
        private boolean warned;

        private RevelationFlag(Method getter) { this.getter = getter; }

        private boolean read(Object entity) {
            if (getter == null) return false;
            try {
                // Read the live synced field, not an unsynced persistent-NBT snapshot.
                return Boolean.TRUE.equals(getter.invoke(entity));
            } catch (ReflectiveOperationException exception) {
                if (!warned) {
                    warned = true;
                    LOGGER.warn("Cannot read Revelation's Apollyon flag; skipping Apostle changes", exception);
                }
                return true;
            }
        }
    }
}
