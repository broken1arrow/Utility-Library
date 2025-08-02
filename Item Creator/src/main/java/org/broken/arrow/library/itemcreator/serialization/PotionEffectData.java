package org.broken.arrow.library.itemcreator.serialization;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectData {
    public String type;
    public int duration;
    public int amplifier;
    public boolean ambient;
    public boolean particles;
    public boolean icon;

    public static PotionEffectData fromEffect(PotionEffect effect) {
        PotionEffectData data = new PotionEffectData();
        data.type = effect.getType().getName();
        data.duration = effect.getDuration();
        data.amplifier = effect.getAmplifier();
        data.ambient = effect.isAmbient();
        data.particles = effect.hasParticles();
        data.icon = effect.hasIcon();
        return data;
    }

    public PotionEffect toEffect() {
        return new PotionEffect(PotionEffectType.getByName(type), duration, amplifier, ambient, particles, icon);
    }
}