package com.starfantasy.goety.halo.client;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import org.joml.Matrix4f;

/** Optional YSM 2.6.5 adapter. No YSM types occur in class descriptors or static initializers. */
public final class YsmHaloBridge {
    private static final String PACKAGE = "com.elfmcys.yesstevemodel.";
    private static Api api;
    private static boolean attempted, warned;

    private static final class Api {
        final Class<?> renderer;
        final Capability<?> capability;
        final Field entityRoot, modelRoot;
        final Method currentModel, scaleX, scaleY, headChain;
        final HaloYsmMount.Bones bones;

        Api() throws ReflectiveOperationException {
            renderer = Class.forName(PACKAGE + "OOoo0o0oO000ooO0Oo00OoOo");
            capability = (Capability<?>) Class.forName(PACKAGE + "O0OooOo0oOOoOoOoOooO000o")
                    .getField("Oo0Oo0o00O00Oo0OOoOOoooo").get(null);
            entityRoot = renderer.getDeclaredField("O00OOOooOoooOoo0o0o0oO0O");
            modelRoot = renderer.getDeclaredField("oOOOo0OOO0ooooo0O00OO0o0");
            entityRoot.setAccessible(true);
            modelRoot.setAccessible(true);
            Class<?> data = Class.forName(PACKAGE + "o0000OoOooO0oo0o0oooo0Oo");
            currentModel = data.getMethod("OOOoOO000000o0o0oOooo0o0");
            scaleX = data.getMethod("c_");
            scaleY = data.getMethod("e_");
            headChain = Class.forName(PACKAGE + "OOOO0O0O000O000000oOOO0o")
                    .getMethod("oOOOooO00oo0oOooooOO0Oo0");
            bones = new HaloYsmMount.Bones(Class.forName(PACKAGE + "Oo0o00oOOo0OO000000O0oO0"),
                    Class.forName(PACKAGE + "o000OO00oooOo00000ooo0Oo"));
        }
    }

    public static HaloYsmMount.Frames capture(Object renderer, LivingEntity player) {
        try {
            if (!attempted) { attempted = true; api = new Api(); }
            if (api == null || !api.renderer.isInstance(renderer)) return null;
            Object data = player.getCapability(api.capability).resolve().orElse(null);
            if (data == null) return null;
            Object model = api.currentModel.invoke(data);
            if (model == null) return null;
            List<?> chain = (List<?>) api.headChain.invoke(model);
            if (chain.isEmpty()) return null;
            // Retain this render's setup rotations, crouch/ride offsets, model size and bone animation.
            Matrix4f root = new Matrix4f((Matrix4f) api.entityRoot.get(renderer)).invert()
                    .mul((Matrix4f) api.modelRoot.get(renderer));
            float x = ((Number) api.scaleX.invoke(data)).floatValue();
            float y = ((Number) api.scaleY.invoke(data)).floatValue();
            root.scale(x, y, x);
            return HaloYsmMount.capture(root, chain, api.bones);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError failure) {
            if (!warned) {
                warned = true;
                LogUtils.getLogger().warn("Cannot attach Star Fantasy halos to YSM's animated bones", failure);
            }
            return null;
        }
    }

    private YsmHaloBridge() {}
}
