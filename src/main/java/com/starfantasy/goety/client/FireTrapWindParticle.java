package com.starfantasy.goety.client;

import com.Polarice3.Goety.client.particles.WindParticle;
import com.Polarice3.Goety.utils.ColorUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** Reuses Goety's orbit/trail simulation, but submits all ribbons as one particle batch. */
final class FireTrapWindParticle extends WindParticle {
    private static final ResourceLocation TEXTURE = new ResourceLocation("goety", "textures/particle/trail.png");
    private static final ParticleRenderType BATCH = new ParticleRenderType() {
        @Override public void m_6505_(BufferBuilder buffer, TextureManager textures) {
            RenderSystem.enableDepthTest(); RenderSystem.depthMask(false);
            RenderSystem.disableCull(); RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::m_172829_);
            RenderSystem.setShaderTexture(0, TEXTURE);
            buffer.m_166779_(VertexFormat.Mode.QUADS, DefaultVertexFormat.f_85813_);
        }
        @Override public void m_6294_(Tesselator tessellator) {
            try { tessellator.m_85914_(); }
            finally { RenderSystem.depthMask(true); RenderSystem.enableCull(); RenderSystem.disableBlend(); }
        }
        @Override public String toString() { return "starfantasy_goety:fire_trap_wind"; }
    };
    private final FireTrapEffect.Trap trap;

    FireTrapWindParticle(ClientLevel level, FireTrapEffect.Trap trap, float width) {
        super(level, trap.position().f_82479_, trap.position().f_82480_, trap.position().f_82481_,
                ColorUtil.GOLD.red(), ColorUtil.GOLD.green(), ColorUtil.GOLD.blue(), width, 0, 0, -1);
        this.trap = trap;
    }
    FireTrapWindParticle(ClientLevel level, WindParticle source) {
        super(level, source.getPosition().f_82479_, source.getPosition().f_82480_, source.getPosition().f_82481_,
                ColorUtil.GOLD.red(), ColorUtil.GOLD.green(), ColorUtil.GOLD.blue(),
                source.width, source.height, source.m_107273_(), source.ownerId);
        this.trap = null;
        this.initYRot = source.initYRot;
        this.rotateAge = source.rotateAge;
        Vec3 position = getOrbitPosition();
        this.f_107209_ = this.f_107212_ = position.f_82479_;
        this.f_107210_ = this.f_107213_ = position.f_82480_;
        this.f_107211_ = this.f_107214_ = position.f_82481_;
    }
    @Override public Vec3 getPosition() { return trap == null ? super.getPosition() : trap.position(); }
    @Override public void m_5989_() {
        if (trap != null && !trap.active) { m_107274_(); return; }
        super.m_5989_();
    }
    @Override public ParticleRenderType m_7556_() { return BATCH; }
    @Override public void m_5744_(VertexConsumer buffer, Camera camera, float partial) {
        if ((trap != null && !trap.active) || trailPointer < 0) return;
        Vec3 cameraPos = camera.m_90583_();
        Vec3 from = new Vec3(f_107209_ + (f_107212_ - f_107209_) * partial,
                f_107210_ + (f_107213_ - f_107210_) * partial,
                f_107211_ + (f_107214_ - f_107211_) * partial);
        Vec3 top = new Vec3(0, getTrailHeight() * 0.5D, 0).m_82535_(getCameraRot(camera));
        Vec3 bottom = top.m_82490_(-1);
        for (int i = 0; i < sampleSize(); i++) {
            Vec3 to = getTrailPosition(i * sampleStep(), partial);
            float u = (float)i / sampleSize(), next = (float)(i + 1) / sampleSize();
            vertex(buffer, from, bottom, cameraPos, u, 1);
            vertex(buffer, to, bottom, cameraPos, next, 1);
            vertex(buffer, to, top, cameraPos, next, 0);
            vertex(buffer, from, top, cameraPos, u, 0);
            from = to;
        }
    }
    private void vertex(VertexConsumer b, Vec3 point, Vec3 offset, Vec3 camera, float u, float v) {
        b.m_5483_(point.f_82479_ + offset.f_82479_ - camera.f_82479_,
                point.f_82480_ + offset.f_82480_ - camera.f_82480_,
                point.f_82481_ + offset.f_82481_ - camera.f_82481_)
                .m_7421_(u, v).m_85950_(f_107227_, f_107228_, f_107229_, trailA).m_85969_(240).m_5752_();
    }
}
