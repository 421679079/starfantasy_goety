package com.starfantasy.goety.magic.focus.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starfantasy.library.vfx.StarFantasyRibbonGeometry;
import org.joml.Matrix4f;

/** Shared star-ring drawing for the explosion finale and casting ribbons. */
final class FlowerRibbonMesh {
    private FlowerRibbonMesh() { }

    @FunctionalInterface interface Points {
        void point(int ribbon, float u, float width, float side, float[] out);
    }

    static void strip(VertexConsumer buffer, Matrix4f pose, float fade, float from, float to,
                      int segments, boolean openTip, Points points) {
        strip(buffer, pose, fade, from, to, segments, openTip, true, points);
    }

    static void castingStrip(VertexConsumer buffer, Matrix4f pose, float fade, float reach, int segments, Points points) {
        strip(buffer, pose, fade, 0, reach, segments, true, false, points);
    }

    private static void strip(VertexConsumer buffer, Matrix4f pose, float fade, float from, float to,
                              int segments, boolean openTip, boolean dispersion, Points points) {
        float[] point = new float[3];
        int sheets = dispersion ? StarFantasyRibbonGeometry.RIBBONS : 1;
        for (int ribbon = 0; ribbon < sheets; ribbon++) for (int layer = 0; layer < 3; layer++) {
            int color = StarFantasyRibbonGeometry.color(ribbon);
            float red = ((color >> 16) & 255) / 255F, green = ((color >> 8) & 255) / 255F, blue = (color & 255) / 255F;
            float width = layer == 0 ? .11F : layer == 1 ? .065F : .012F;
            float alpha = fade * (layer == 0 ? .24F : layer == 1 ? .58F : .9F);
            float white = layer == 2 ? .5F : .12F;
            for (int i = 0; i < segments; i++) for (int corner = 0; corner < 4; corner++) {
                float sample = (i + (corner == 1 || corner == 2 ? 1 : 0)) / (float) segments;
                float u = from + (to - from) * sample;
                float tip = openTip ? Math.min(1, (1 - sample) / .12F) : 1;
                points.point(ribbon, u, width * tip, corner < 2 ? -1 : 1, point);
                float r = red, g = green, b = blue;
                if (!dispersion) {
                    // A static longitudinal blue/pink gradient on ONE sheet, not color-separated copies.
                    float mix = .5F - .5F * (float) Math.cos(u * Math.PI * 2);
                    int pink = StarFantasyRibbonGeometry.color(1);
                    r += (((pink >> 16) & 255) / 255F - r) * mix;
                    g += (((pink >> 8) & 255) / 255F - g) * mix;
                    b += ((pink & 255) / 255F - b) * mix;
                }
                buffer.vertex(pose, point[0], point[1], point[2] + StarFantasyRibbonGeometry.layerDepth(u, layer))
                        .color(r + (1 - r) * white, g + (1 - g) * white, b + (1 - b) * white, alpha * tip).endVertex();
            }
        }
    }

    static void star(VertexConsumer out, Matrix4f pose, float x, float y, float length, float width,
                     float r, float g, float b, float alpha) {
        for (int axis = 0; axis < 2; axis++) {
            float dx = axis == 0 ? length : width, dy = axis == 0 ? width : length;
            out.vertex(pose, x - dx, y, 0).color(r, g, b, alpha).endVertex();
            out.vertex(pose, x, y - dy, 0).color(r, g, b, alpha).endVertex();
            out.vertex(pose, x + dx, y, 0).color(r, g, b, alpha).endVertex();
            out.vertex(pose, x, y + dy, 0).color(r, g, b, alpha).endVertex();
        }
    }
}
