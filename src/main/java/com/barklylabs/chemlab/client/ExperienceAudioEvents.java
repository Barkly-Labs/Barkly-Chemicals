package com.barklylabs.chemlab.client;

import com.barklylabs.chemlab.BarklyChemLab;
import com.barklylabs.chemlab.config.ChemLabConfig;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BarklyChemLab.MOD_ID, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class ExperienceAudioEvents {
    private static long lastEchoTick = -100;
    @SubscribeEvent public static void presentSound(PlaySoundEvent event) {
        if (!ChemLabConfig.CLIENT_AUDIO_EFFECTS.get() || !ClientExperienceState.active()) return;
        SoundInstance original = event.getSound();
        if (original == null || original instanceof FilteredSound filtered && filtered.echoCopy) return;
        SoundSource source = original.getSource();
        if (source == SoundSource.MUSIC || source == SoundSource.RECORDS) return;
        float muffle = ClientExperienceState.combined("muffle", p -> p.muffle());
        float pan = ClientExperienceState.combined("pan", p -> Math.abs(p.pan()));
        float pitch = ClientExperienceState.combined("pitch", p -> Math.abs(p.pitch()));
        float echo = ClientExperienceState.combined("echo", p -> p.echo());
        if (muffle + pan + pitch + echo < 0.01f) return;
        event.setSound(new FilteredSound(original, muffle, pan, pitch));
        Minecraft mc = Minecraft.getInstance();
        long now = mc.level == null ? 0 : mc.level.getGameTime();
        if (echo > 0.12f && now - lastEchoTick > 24) {
            lastEchoTick = now;
            FilteredSound duplicate = new FilteredSound(original, Math.max(muffle, echo * 0.6f), pan, pitch, Math.max(3, Math.round(echo * 8)), true);
            mc.execute(() -> mc.getSoundManager().play(duplicate));
        }
    }

    private record FilteredSound(SoundInstance base, float muffle, float pan, float pitchShift, int extraDelay, boolean echoCopy) implements SoundInstance {
        FilteredSound(SoundInstance base, float muffle, float pan, float pitchShift) { this(base, muffle, pan, pitchShift, 0, false); }
        @Override public net.minecraft.resources.ResourceLocation getLocation() { return base.getLocation(); }
        @Override public WeighedSoundEvents resolve(SoundManager manager) { return base.resolve(manager); }
        @Override public Sound getSound() { return base.getSound(); }
        @Override public SoundSource getSource() { return base.getSource(); }
        @Override public boolean isLooping() { return base.isLooping(); }
        @Override public boolean isRelative() { return base.isRelative(); }
        @Override public int getDelay() { return base.getDelay() + extraDelay; }
        @Override public float getVolume() { return base.getVolume() * (echoCopy ? 0.24f : 1.0f - Math.min(0.45f, muffle * 0.35f)); }
        @Override public float getPitch() { return base.getPitch() * (1.0f - Math.min(0.08f, pitchShift * 0.04f)); }
        @Override public double getX() {
            float yaw = Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.getYRot();
            return base.getX() + Math.sin(Math.toRadians(yaw)) * Math.min(1.25f, pan * 1.25f);
        }
        @Override public double getY() { return base.getY(); }
        @Override public double getZ() {
            float yaw = Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.getYRot();
            return base.getZ() - Math.cos(Math.toRadians(yaw)) * Math.min(1.25f, pan * 1.25f);
        }
        @Override public Attenuation getAttenuation() { return base.getAttenuation(); }
        @Override public boolean canStartSilent() { return base.canStartSilent(); }
        @Override public boolean canPlaySound() { return base.canPlaySound(); }
    }
    private ExperienceAudioEvents() { }
}
