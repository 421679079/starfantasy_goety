package com.starfantasy.goety.halo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.lang.reflect.Method;
import java.util.List;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** Current-frame YSM attachment poses, relative to RenderLiving.Post's entity origin. */
public final class HaloYsmMount {
    public static final float APOSTLE_SIZE = .65F;
    public static final double APOSTLE_LOWERING = .25D;
    // Apollyon's eye plane center minus AllHead's resting pivot, at its renderer's 0.65 scale.
    private static final double HADES_X = -.026805D * .65D / 16D;
    private static final double HADES_Y = (51.333335D - 32.425D) * .65D / 16D;
    private static final double HADES_Z = (9.16667D + .025D) * .65D / 16D;

    public record Frames(Matrix4f head, Matrix4f bodyAtRestingHead) {}

    /** Retain reflection handles only, never model, bone or player instances. */
    static final class Bones {
        final Method applyChain, unpivot, name, pivotX, pivotY, pivotZ;

        Bones(Class<?> bone, Class<?> transforms) throws ReflectiveOperationException {
            applyChain = transforms.getMethod("Oo0Oo0o00O00Oo0OOoOOoooo", PoseStack.class, List.class);
            unpivot = transforms.getMethod("OOOOo0O0oO0OOo0O0O0Oo0O0", PoseStack.class, bone);
            name = bone.getMethod("oOOo0Ooo0oOoo0O0OOOOo0oo");
            pivotX = bone.getMethod("o0OOO0o0o0OOo000oO00o00O");
            pivotY = bone.getMethod("O0OooOo0oOOoOoOoOooO000o");
            pivotZ = bone.getMethod("ooOO000o0O0OOOoO0Oo0o0Oo");
        }
    }

    static Frames capture(Matrix4f modelRoot, List<?> chain, Bones bones) throws ReflectiveOperationException {
        if (chain.isEmpty()) return null;
        int head = chain.size() - 1, body = -1;
        for (int i = 0; i < chain.size(); i++) {
            String name = (String) bones.name.invoke(chain.get(i));
            if (name.equals("AllHead")) { head = i; break; }
        }
        // The reference eye belongs to aaabody/AllBody, before the upper-body/head look animations.
        for (int i = 0; i < head; i++) {
            String name = (String) bones.name.invoke(chain.get(i));
            if (name.equals("aaabody") || name.equals("AllBody") || name.equals("Body") || name.equals("body")) body = i;
        }
        if (body < 0) body = head - 1;
        PoseStack headPose = new PoseStack();
        headPose.last().pose().set(modelRoot);
        boolean hidden = (boolean) bones.applyChain.invoke(null, headPose, chain);
        if (hidden || !usable(headPose.last().pose())) return null;

        PoseStack bodyPose = new PoseStack();
        bodyPose.last().pose().set(modelRoot);
        if (body >= 0) {
            bones.applyChain.invoke(null, bodyPose, chain.subList(0, body + 1));
            // YSM's attachment helper stops at the pivot. Return to model coordinates for a body child.
            bones.unpivot.invoke(null, bodyPose, chain.get(body));
        }
        if (!usable(bodyPose.last().pose())) return null;
        Object headBone = chain.get(head);
        Vector3f restingHead = new Vector3f(
                ((Number) bones.pivotX.invoke(headBone)).floatValue() / 16F,
                ((Number) bones.pivotY.invoke(headBone)).floatValue() / 16F,
                ((Number) bones.pivotZ.invoke(headBone)).floatValue() / 16F);
        bodyPose.last().pose().transformPosition(restingHead);
        Matrix4f bodyFrame = unitFrame(bodyPose.last().pose()).setTranslation(restingHead);
        return new Frames(unitFrame(headPose.last().pose()), bodyFrame);
    }

    private static boolean usable(Matrix4f matrix) {
        return matrix.isFinite() && Math.abs(new Matrix3f(matrix).determinant()) > 1.0E-8F;
    }

    private static Matrix4f unitFrame(Matrix4f source) {
        // Keep animated position/orientation, but size the item independently of the avatar's scale.
        Matrix4f result = new Matrix4f(source);
        Vector3f axis = new Vector3f();
        for (int i = 0; i < 3; i++) {
            source.getColumn(i, axis).normalize();
            result.setColumn(i, new Vector4f(axis, 0));
        }
        return result;
    }

    public static void apply(PoseStack pose, Frames frames, boolean hades) {
        if (!hades) pose.translate(0, -APOSTLE_LOWERING, 0);
        pose.last().pose().mul(hades ? frames.bodyAtRestingHead() : frames.head());
        if (hades) pose.translate(HADES_X, HADES_Y, HADES_Z);
        // YSM is model-up; the existing item mesh uses vanilla model-down coordinates.
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.last().normal().set(new Matrix3f(pose.last().pose()).invert().transpose());
    }

    private HaloYsmMount() {}
}
