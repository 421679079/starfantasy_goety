package com.starfantasy.goety.client.apostle;

import com.starfantasy.goety.config.ApostleConfig;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "starfantasy_goety", value = Dist.CLIENT)
public final class ApostleSoundEvents {
    private static final ResourceLocation AMBIENT = ApostleResources.id("original_apostle_ambient");
    private static final ResourceLocation HURT = ApostleResources.id("original_apostle_hurt");
    private static final ResourceLocation DEATH = ApostleResources.id("original_apostle_death");

    private ApostleSoundEvents() {}

    @SubscribeEvent
    public static void chooseAppearanceSound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound == null) return;
        ResourceLocation id = sound.getLocation();
        boolean hurt = HURT.equals(id);
        boolean ambient = AMBIENT.equals(id);
        if (!hurt && !ambient && !DEATH.equals(id)) return;

        boolean moe = ApostleConfig.ENABLE_APOSTLE_MOE.get();
        // Marker events reference the original Goety events in sounds.json.
        // Preserve the exact original instance and its sound parameters when disabled.
        if (!moe) return;
        // Doki silenced ambient and true-death voices; pre-death effects stay native.
        if (moe && !hurt) {
            event.setSound(null);
            return;
        }
        event.setSound(new MoeHurtSound(sound));
    }

    /** PlaySoundEvent fires before resolve(), so volume must only be read lazily. */
    private static final class MoeHurtSound implements SoundInstance {
        private final SoundInstance original;
        private SimpleSoundInstance resolved;

        private MoeHurtSound(SoundInstance original) { this.original = original; }
        @Override public ResourceLocation getLocation() { return ApostleResources.id("apostle_moe_hurt"); }
        @Override public WeighedSoundEvents resolve(SoundManager manager) {
            if (original.resolve(manager) == null) return null;
            resolved = new SimpleSoundInstance(getLocation(), getSource(),
                    original.getVolume(), 1.0F, RandomSource.create(),
                    isLooping(), getDelay(), getAttenuation(), getX(), getY(), getZ(), isRelative());
            return resolved.resolve(manager);
        }
        @Override public Sound getSound() { return resolved.getSound(); }
        @Override public SoundSource getSource() { return original.getSource(); }
        @Override public boolean isLooping() { return original.isLooping(); }
        @Override public boolean isRelative() { return original.isRelative(); }
        @Override public int getDelay() { return original.getDelay(); }
        @Override public float getVolume() { return resolved.getVolume(); }
        @Override public float getPitch() { return 1.0F; }
        @Override public double getX() { return original.getX(); }
        @Override public double getY() { return original.getY(); }
        @Override public double getZ() { return original.getZ(); }
        @Override public Attenuation getAttenuation() { return original.getAttenuation(); }
        @Override public boolean canStartSilent() { return original.canStartSilent(); }
        @Override public boolean canPlaySound() { return original.canPlaySound(); }
    }
}
