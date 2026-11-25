package com.barklylabs.chemlab.client;

import com.barklylabs.chemlab.config.ChemLabConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/** Client-only composition engine for declarative profile layers. Each type owns its drawing behavior. */
public final class ExperienceLayerRenderers {
    private static final Map<String, BiConsumer<Context, ExperienceVisualLayer>> RENDERERS = new HashMap<>();
    static {
        register("edge_tint", ExperienceLayerRenderers::edgeTint);
        register("bands", ExperienceLayerRenderers::bands);
        register("motif", ExperienceLayerRenderers::motif);
        register("orbit", ExperienceLayerRenderers::orbit);
        register("particles", ExperienceLayerRenderers::particles);
        register("motion_trail", ExperienceLayerRenderers::motionTrail);
        register("chromatic", ExperienceLayerRenderers::chromatic);
    }

    private ExperienceLayerRenderers() { }

    /** Built-in extension point for additional local renderer modules. */
    public static void register(String type, BiConsumer<Context, ExperienceVisualLayer> renderer) {
        if (type == null || !type.matches("[a-z0-9_]{1,40}")) throw new IllegalArgumentException("Invalid visual layer type: " + type);
        RENDERERS.put(type, renderer);
    }

    public static void render(GuiGraphics graphics, boolean menu, List<ClientExperienceState.Layer> profiles) {
        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        long gameTicks = mc.level == null ? mc.gui.getGuiTicks() : mc.level.getGameTime();
        float partial = mc.getFrameTime();
        Context context = new Context(graphics, menu, width, height, gameTicks + partial, profiles);
        profiles.stream().flatMap(source -> source.profile().layers().stream()
                        .map(layer -> new Ordered(source, layer)))
                .sorted(Comparator.comparingInt(ordered -> ordered.layer.order()))
                .limit(96)
                .forEach(ordered -> {
                    BiConsumer<Context, ExperienceVisualLayer> renderer = RENDERERS.get(ordered.layer.type());
                    if (renderer == null || !enabled(ordered.layer)) return;
                    context.source = ordered.source;
                    context.layerPower = Math.max(0f, Math.min(1f,
                            ordered.source.intensity() * ordered.layer.intensity()));
                    if (context.layerPower > 0.002f) renderer.accept(context, ordered.layer);
                });
    }

    private static boolean enabled(ExperienceVisualLayer layer) {
        if (!ChemLabConfig.CLIENT_VISUAL_EFFECTS.get()) return false;
        boolean moduleEnabled = switch (layer.type()) {
            case "edge_tint" -> ChemLabConfig.CLIENT_LAYER_EDGE_TINT.get();
            case "bands" -> ChemLabConfig.CLIENT_LAYER_BANDS.get();
            case "motif" -> ChemLabConfig.CLIENT_LAYER_MOTIFS.get();
            case "orbit" -> ChemLabConfig.CLIENT_LAYER_ORBITS.get();
            case "particles" -> ChemLabConfig.CLIENT_LAYER_PARTICLES.get();
            case "motion_trail" -> ChemLabConfig.CLIENT_VISUAL_TRAILS.get();
            case "chromatic" -> ChemLabConfig.CLIENT_CHROMATIC_FRINGE.get();
            default -> true;
        };
        if (!moduleEnabled) return false;
        return switch (layer.accessibility()) {
            case "hud" -> ChemLabConfig.CLIENT_HUD_FEEDBACK.get();
            case "trails" -> ChemLabConfig.CLIENT_VISUAL_TRAILS.get() && !ChemLabConfig.CLIENT_REDUCE_MOTION.get();
            case "chromatic" -> ChemLabConfig.CLIENT_CHROMATIC_FRINGE.get();
            case "motion" -> !ChemLabConfig.CLIENT_REDUCE_MOTION.get();
            default -> true;
        };
    }

    private static void edgeTint(Context c, ExperienceVisualLayer layer) {
        int maxAlpha = c.menu ? 40 : 94;
        float modulation = ChemLabConfig.CLIENT_PREVENT_FLASHING.get() ? 0.92f + 0.08f * (float)Math.sin(c.time * 0.012) : 0.72f + 0.28f * (float)Math.sin(c.time * 0.07);
        int alpha = alpha(maxAlpha, c, layer, modulation);
        int rgb = layer.color();
        int[] bands = {Math.round(24 * layer.scale()), Math.round(13 * layer.scale()), Math.round(5 * layer.scale())};
        for (int i = 0; i < bands.length; i++) {
            int a = Math.max(0, alpha * (i + 1) / 3);
            int color = argb(a, rgb);
            int band = Math.max(1, Math.min(64, bands[i]));
            c.g.fill(0, 0, c.w, band, color);
            c.g.fill(0, c.h - band, c.w, c.h, color);
            if (!c.menu) {
                c.g.fill(0, 0, band, c.h, color);
                c.g.fill(c.w - band, 0, c.w, c.h, color);
            }
        }
    }

    private static void bands(Context c, ExperienceVisualLayer layer) {
        if (c.menu) return;
        float motion = ChemLabConfig.CLIENT_REDUCE_MOTION.get() ? 0f : (float)Math.sin(c.time * 0.018 * layer.speed());
        int displacement = Math.round(motion * 10 * layer.scale());
        int alpha = alpha(54, c, layer, 1f);
        int color = argb(alpha, layer.color());
        for (int i = 0; i < 4; i++) {
            int y = c.h / 2 + displacement + (i - 2) * Math.max(4, Math.round(11 * layer.scale()));
            int inset = Math.max(8, c.w / 5 + Math.abs(i - 2) * 7);
            c.g.fill(inset, y, c.w - inset, y + 1 + i % 2, color);
        }
    }

    private static void motif(Context c, ExperienceVisualLayer layer) {
        if (c.menu) return;
        ExperienceProfile p = c.source.profile();
        int alpha = alpha(112, c, layer, 1f);
        if (alpha < 2 || p.hud() <= 0) return;
        int color = argb(alpha, layer.color());
        int x = 9, y = 22, s = Math.max(1, Math.round(layer.scale()));
        switch (p.pattern()) {
            case "echo", "afterimage" -> { for (int i=0;i<4;i++) c.g.fill(x+i*4*s,y+i*2*s,x+(i*4+2)*s,y+(i*2+1)*s,color); }
            case "mirror", "facets", "prism" -> { c.g.fill(c.w-24,y,c.w-19,y+5,color); c.g.fill(c.w-19,y+5,c.w-14,y+10,color); c.g.fill(c.w-24,y+10,c.w-19,y+15,color); }
            case "leaf", "moth", "bloom" -> { c.g.fill(x+3,y,x+5,y+6,color); c.g.fill(x,y+3,x+4,y+6,color); c.g.fill(x+4,y+6,x+8,y+10,color); }
            case "cloud", "tide" -> { for (int i=0;i<4;i++) c.g.fill(c.w-30+i*4,y+(i%2)*3,c.w-27+i*4,y+2+(i%2)*3,color); }
            case "chime", "clock", "archive" -> { c.g.fill(x,y,x+2,y+12,color); c.g.fill(x+9,y,x+11,y+12,color); c.g.fill(x,y,x+11,y+2,color); c.g.fill(x,y+10,x+11,y+12,color); }
            case "spark", "sun", "sunburst", "ember", "warmth" -> { c.g.fill(c.w-19,y+3,c.w-16,y+11,color); c.g.fill(c.w-23,y+6,c.w-12,y+8,color); c.g.fill(c.w-18,y,c.w-17,y+3,color); }
            case "moon", "lantern", "orbit" -> { c.g.fill(x,y+2,x+8,y+12,color); c.g.fill(x+4,y,x+9,y+9,argb(alpha/2, layer.color())); }
            default -> c.g.fill(x,y,x+5,y+3,color);
        }
    }

    private static void orbit(Context c, ExperienceVisualLayer layer) {
        if (c.menu) return;
        float angle = ChemLabConfig.CLIENT_REDUCE_MOTION.get() ? 0f : c.time * 0.012f * layer.speed();
        int cx = c.w / 2, cy = c.h / 2;
        int radius = Math.max(12, Math.min(70, Math.round(36 * layer.scale())));
        int alpha = alpha(76, c, layer, 1f);
        for (int i = 0; i < 6; i++) {
            double a = angle + i * Math.PI / 3.0;
            int x = cx + (int)(Math.cos(a) * radius), y = cy + (int)(Math.sin(a) * radius * 0.6);
            c.g.fill(x-2, y-2, x+3, y+3, argb(alpha * (6-i) / 6, layer.color()));
        }
    }

    private static void particles(Context c, ExperienceVisualLayer layer) {
        if (c.menu) return;
        int count = 7;
        int alpha = alpha(58, c, layer, 1f);
        for (int i=0; i<count; i++) {
            double seed = i * 19.13 + c.source.profile().id().hashCode() * 0.001;
            float drift = ChemLabConfig.CLIENT_REDUCE_MOTION.get() ? 0f : (float)(c.time * 0.012 * layer.speed());
            int x = (int)Math.floorMod((long)(Math.sin(seed) * c.w + c.w + drift * 17), Math.max(1,c.w));
            int y = (int)Math.floorMod((long)(Math.cos(seed * 1.7) * c.h + c.h - drift * 11), Math.max(1,c.h));
            int size = i % 3 == 0 ? 3 : 2;
            c.g.fill(x, y, x+size, y+size, argb(alpha * (i%2 == 0 ? 1 : 2) / 2, layer.color()));
        }
    }

    private static void motionTrail(Context c, ExperienceVisualLayer layer) {
        if (c.menu || ChemLabConfig.CLIENT_REDUCE_MOTION.get() || !ChemLabConfig.CLIENT_VISUAL_TRAILS.get()) return;
        float trail = ClientExperienceState.combined("trail", p -> p.trail());
        if (trail <= 0.02f) return;
        int dx = (int)(Math.sin(c.time * 0.035 * layer.speed()) * 5 * layer.scale());
        int a = alpha(44, c, layer, trail);
        c.g.fill(c.w/2-27+dx,c.h/2-1,c.w/2-18+dx,c.h/2+1,argb(a,layer.color()));
        c.g.fill(c.w/2+18-dx,c.h/2-1,c.w/2+27-dx,c.h/2+1,argb(a,layer.color()));
    }

    private static void chromatic(Context c, ExperienceVisualLayer layer) {
        if (c.menu || !ChemLabConfig.CLIENT_CHROMATIC_FRINGE.get()) return;
        float fringe = ClientExperienceState.combined("fringe", p -> p.fringe());
        int a = alpha(72, c, layer, fringe);
        c.g.fill(c.w/2-32,c.h/2-1,c.w/2-25,c.h/2+1,argb(a,0x55DDF0));
        c.g.fill(c.w/2+25,c.h/2-1,c.w/2+32,c.h/2+1,argb(a,0xEF78D8));
    }

    private static int alpha(int maximum, Context c, ExperienceVisualLayer layer, float multiplier) {
        float flash = ChemLabConfig.CLIENT_PREVENT_FLASHING.get() ? 0.72f : 1f;
        return Math.max(0, Math.min(maximum, Math.round(maximum * c.layerPower * layer.opacity() * Math.max(0f, Math.min(1f, multiplier)) * flash)));
    }
    private static int argb(int alpha, int rgb) { return (Math.max(0,Math.min(255,alpha)) << 24) | (rgb & 0xFFFFFF); }

    private record Ordered(ClientExperienceState.Layer source, ExperienceVisualLayer layer) { }
    private static final class Context {
        final GuiGraphics g; final boolean menu; final int w,h; final float time; final List<ClientExperienceState.Layer> sources;
        ClientExperienceState.Layer source; float layerPower;
        Context(GuiGraphics g, boolean menu, int w, int h, float time, List<ClientExperienceState.Layer> sources) {
            this.g=g; this.menu=menu; this.w=w; this.h=h; this.time=time; this.sources=sources;
        }
    }
}
