package com.starfantasy.goety.magic.focus.client;

import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.client.particles.WindParticle;
import com.Polarice3.Goety.utils.ColorUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.starfantasy.goety.magic.focus.ChaosMeteorEntity;
import com.starfantasy.goety.magic.focus.ChaosMeteorEntity.TrapVisual;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Player meteor trap particles, timed from the server's trap creation tick. */
public final class ChaosMeteorTrapEffects {
    private static final ResourceLocation WIND_TEXTURE = new ResourceLocation("goety", "textures/particle/trail.png");
    private static final ParticleRenderType WIND_BATCH = new ParticleRenderType() {
        @Override public void begin(BufferBuilder buffer, TextureManager textures) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, WIND_TEXTURE);
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override public void end(Tesselator tesselator) {
            try {
                tesselator.end();
            } finally {
                RenderSystem.depthMask(true);
                RenderSystem.enableCull();
                RenderSystem.disableBlend();
            }
        }

        @Override public String toString() { return "starfantasy_goety:chaos_meteor_trap_wind"; }
    };

    public static void emit(ChaosMeteorEntity meteor, long now) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = (ClientLevel) meteor.level();
        var camera = minecraft.getCameraEntity();
        if (camera == null) return;
        ParticleStatus quality = minecraft.options.particles().get();
        for (TrapVisual trap : meteor.trapVisuals()) {
            long age = now - trap.born();
            if (age < 0 || age >= ChaosMeteorEntity.TRAP_TICKS) continue;
            Vec3 emission = risingCenter(trap, age - ChaosMeteorEntity.TRAP_HIT_TICK);
            if (camera.distanceToSqr(emission) > 32 * 32) continue;
            disk(level, ModParticleTypes.BURNING.get(), emission, trap.scale(), 8, 0);
            wind(minecraft, level, meteor, trap, quality, .5F + level.random.nextFloat() * .5F);
            wind(minecraft, level, meteor, trap, quality, 1.5F + level.random.nextFloat() * .5F);
            if (age == ChaosMeteorEntity.TRAP_HIT_TICK) {
                disk(level, ParticleTypes.FLAME, emission, trap.scale(), 128, .25);
            }
        }
    }

    private static Vec3 risingCenter(TrapVisual trap, long riseTicks) {
        long rise = Math.max(0, riseTicks);
        return trap.center().add(0, .125 * rise * (rise + 1), 0);
    }

    private static void disk(ClientLevel level, ParticleOptions type, Vec3 center, float scale, int count, double speed) {
        for (int i = 0; i < count; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double radius = Math.sqrt(level.random.nextDouble()) * 1.5 * scale;
            level.addParticle(type, center.x + Math.cos(angle) * radius, center.y,
                    center.z + Math.sin(angle) * radius, 0, speed, 0);
        }
    }

    private static void wind(Minecraft minecraft, ClientLevel level, ChaosMeteorEntity meteor, TrapVisual trap,
                             ParticleStatus quality, float width) {
        if (quality == ParticleStatus.MINIMAL || (quality == ParticleStatus.DECREASED && level.random.nextInt(3) == 0)) return;
        minecraft.particleEngine.add(new TrapWind(level, meteor, trap, width * trap.scale()));
    }

    private static final class TrapWind extends WindParticle {
        private final ChaosMeteorEntity meteor;
        private final TrapVisual trap;

        TrapWind(ClientLevel level, ChaosMeteorEntity meteor, TrapVisual trap, float width) {
            super(level, trap.center().x, trap.center().y, trap.center().z,
                    ColorUtil.GOLD.red(), ColorUtil.GOLD.green(), ColorUtil.GOLD.blue(), width, 0, 0, -1);
            this.meteor = meteor;
            this.trap = trap;
        }

        private boolean active() {
            return !meteor.isRemoved() && meteor.level().getGameTime() - trap.born() < ChaosMeteorEntity.TRAP_TICKS
                    && meteor.trapVisuals().contains(trap);
        }

        @Override public Vec3 getPosition() {
            if (trap == null) return super.getPosition();
            return risingCenter(trap, meteor.level().getGameTime() - trap.born() - (ChaosMeteorEntity.TRAP_HIT_TICK - 1));
        }

        @Override public void tick() {
            if (active()) super.tick();
            else remove();
        }

        @Override public ParticleRenderType getRenderType() { return WIND_BATCH; }

        @Override public void render(VertexConsumer buffer, Camera camera, float partial) {
            if (!active() || trailPointer < 0) return;
            Vec3 view = camera.getPosition();
            Vec3 from = new Vec3(Mth.lerp(partial, xo, x), Mth.lerp(partial, yo, y), Mth.lerp(partial, zo, z));
            Vec3 top = new Vec3(0, getTrailHeight() * .5, 0).zRot(getCameraRot(camera));
            Vec3 bottom = top.scale(-1);
            int samples = sampleSize();
            for (int i = 0; i < samples; i++) {
                Vec3 to = getTrailPosition(i * sampleStep(), partial);
                float u = (float) i / samples, next = (float) (i + 1) / samples;
                vertex(buffer, from, bottom, view, u, 1);
                vertex(buffer, to, bottom, view, next, 1);
                vertex(buffer, to, top, view, next, 0);
                vertex(buffer, from, top, view, u, 0);
                from = to;
            }
        }

        private void vertex(VertexConsumer buffer, Vec3 point, Vec3 offset, Vec3 view, float u, float v) {
            buffer.vertex(point.x + offset.x - view.x, point.y + offset.y - view.y, point.z + offset.z - view.z)
                    .uv(u, v).color(rCol, gCol, bCol, trailA).uv2(240).endVertex();
        }
    }

    private ChaosMeteorTrapEffects() {}
}
