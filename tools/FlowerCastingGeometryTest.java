import com.starfantasy.goety.magic.focus.client.FlowerCastingGeometry;
import com.starfantasy.library.vfx.StarFantasyRibbonGeometry;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Production-geometry regression and an optional flat material/contact-sheet preview. */
public final class FlowerCastingGeometryTest {
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        check(FlowerCastingGeometry.ARMS == 4, "four arms");
        check(FlowerCastingGeometry.reach(0) == 0, "starts at center");
        check(FlowerCastingGeometry.reach(.001F) > 0, "immediate extension");
        check(FlowerCastingGeometry.reach(1) == 1, "fully extended");
        check(FlowerCastingGeometry.reach(-1) == 0 && FlowerCastingGeometry.reach(2) == 1, "clamped growth");
        check(FlowerCastingGeometry.pulse(0) == 0 && FlowerCastingGeometry.pulse(24) == 1, "star breathes");
        check(FlowerCastingGeometry.pulse(48) == 0, "continuous pulse cycle");
        float[] p = new float[3], q = new float[3];
        float previous = -1;
        for (int frame = 0; frame <= 200; frame++) {
            float progress = frame / 200F;
            check(FlowerCastingGeometry.reach(progress) >= previous, "monotonic extension");
            previous = FlowerCastingGeometry.reach(progress);
            for (int arm = 0; arm < 4; arm++) for (int ribbon = 0; ribbon < 2; ribbon++) {
                FlowerCastingGeometry.point(arm, ribbon, 0, progress, frame, .11F, 1, p);
                check(p[0] == 0 && p[1] == 0 && p[2] == 0, "all layers pinned at star");
                for (int i = 0; i <= 64; i++) for (int side = -1; side <= 1; side += 2) {
                    float u = progress * i / 64F;
                    FlowerCastingGeometry.point(arm, ribbon, u, progress, frame, .11F, side, p);
                    for (float value : p) check(Float.isFinite(value), "finite vertices");
                    check(Math.abs(Math.abs(p[0]) - u) < .00001, "longitudinal slices cannot fold into each other");
                    FlowerCastingGeometry.point(arm, 1 - ribbon, u, progress, frame, .11F, side, q);
                    check(p[0] == q[0] && p[1] == q[1] && p[2] == q[2], "casting has no chromatic displacement");
                    check(Math.abs(p[0]) < 1.2 && Math.abs(p[1]) < .75 && Math.abs(p[2]) < .15, "culling bounds");
                }
                FlowerCastingGeometry.point(arm, ribbon, progress, progress, frame, 0, 0, p);
                if (progress > .05) {
                    check((arm < 2 ? p[0] > 0 : p[0] < 0), "left/right free ends");
                    check(((arm & 1) == 0 ? p[1] > 0 : p[1] < 0), "upper/lower free ends");
                }
            }
        }
        for (int arm = 0; arm < 4; arm++) {
            float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY;
            float change = 0;
            for (int age = 0; age < 40; age++) {
                // Average the two chromatic layers to measure actual centerline movement, not strip twist.
                FlowerCastingGeometry.point(arm, 0, .8F, 1, age, 0, 0, p);
                FlowerCastingGeometry.point(arm, 1, .8F, 1, age, 0, 0, q);
                float center = (p[1] + q[1]) * .5F * FlowerCastingGeometry.RADIUS;
                min = Math.min(min, center); max = Math.max(max, center);
                FlowerCastingGeometry.point(arm, 0, .8F, 1, age + 3, 0, 0, q);
                change = Math.max(change, Math.abs(q[1] - p[1]) * FlowerCastingGeometry.RADIUS);
            }
            check(max - min > 1.4, "whole ribbon sweeps over 1.4 blocks, not a tiny edge twist");
            check(change > .65, "clearly different pose within three ticks");
            FlowerCastingGeometry.point(arm, 0, 1, 1, 0, 0, 0, p);
            check(Math.abs(p[0]) * FlowerCastingGeometry.RADIUS > 7, "ultimate-scale open ends");
        }
        System.out.println("PASS: immediate extension, four free ends, finite bounds, strong fast centerline motion and star pulse");
        if (args.length > 0) preview(args[0]);
    }

    /** Debug projection only: real game depth, perspective and shader compositing need in-game verification. */
    private static void preview(String path) throws Exception {
        int w = 720, h = 340;
        BufferedImage image = new BufferedImage(w * 2, h * 3, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(9, 12, 28)); g.fillRect(0, 0, image.getWidth(), image.getHeight());
        float[] ages = {3, 9, 15, 18, 21, 24}, progresses = {.2F, .6F, 1, 1, 1, 1};
        float[] point = new float[3];
        for (int frame = 0; frame < 6; frame++) {
            float age = ages[frame], progress = progresses[frame];
            double cx = (frame % 2) * w + w / 2.0, cy = (frame / 2) * h + h / 2.0 + 12, scale = 290;
            g.setColor(new Color(160, 170, 200));
            g.drawString("progress=" + progress + "  tick=" + age, (frame % 2) * w + 18, (frame / 2) * h + 25);
            for (int arm = 0; arm < 4; arm++) for (int ribbon = 0; ribbon < 1; ribbon++) for (int layer = 0; layer < 3; layer++) {
                int color = StarFantasyRibbonGeometry.color(ribbon);
                float white = layer == 2 ? .5F : .12F;
                float red = ((color >> 16) & 255) / 255F, green = ((color >> 8) & 255) / 255F, blue = (color & 255) / 255F;
                float width = layer == 0 ? .11F : layer == 1 ? .065F : .012F;
                float alpha = layer == 0 ? .24F : layer == 1 ? .58F : .9F;
                for (int i = 0; i < FlowerCastingGeometry.SEGMENTS; i++) {
                    Path2D.Float quad = new Path2D.Float();
                    for (int corner = 0; corner < 4; corner++) {
                        float sample = (i + (corner == 1 || corner == 2 ? 1 : 0)) / (float) FlowerCastingGeometry.SEGMENTS;
                        float tip = Math.min(1, (1 - sample) / .12F);
                        FlowerCastingGeometry.point(arm, ribbon, sample * progress, progress, age, width * tip, corner < 2 ? -1 : 1, point);
                        double x = cx + point[0] * scale, y = cy - point[1] * scale;
                        if (corner == 0) quad.moveTo(x, y); else quad.lineTo(x, y);
                    }
                    quad.closePath();
                    float tip = Math.min(1, (1 - (i + .5F) / FlowerCastingGeometry.SEGMENTS) / .12F);
                    float u = (i + .5F) / FlowerCastingGeometry.SEGMENTS * progress;
                    float mix = .5F - .5F * (float) Math.cos(u * Math.PI * 2);
                    int pink = StarFantasyRibbonGeometry.color(1);
                    float r = red + (((pink >> 16) & 255) / 255F - red) * mix;
                    float greenMix = green + (((pink >> 8) & 255) / 255F - green) * mix;
                    float b = blue + ((pink & 255) / 255F - blue) * mix;
                    g.setColor(new Color(r + (1 - r) * white, greenMix + (1 - greenMix) * white, b + (1 - b) * white, alpha * tip));
                    g.fill(quad);
                }
            }
            float pulse = FlowerCastingGeometry.pulse(age);
            for (int layer = 0; layer < 2; layer++) {
                float size = layer == 0 ? .90F + .30F * pulse : .90F + .20F * pulse;
                float length = (layer == 0 ? .29F : .26F) * size;
                float width = (layer == 0 ? .025F : .01F) * size;
                float alpha = layer == 0 ? .18F + .14F * pulse : .78F + .22F * pulse;
                g.setColor(new Color(layer == 0 ? .78F : 1, layer == 0 ? .85F : .94F, 1, alpha));
                for (int axis = 0; axis < 2; axis++) {
                    double dx = (axis == 0 ? length : width) * scale, dy = (axis == 0 ? width : length) * scale;
                    Path2D.Double star = new Path2D.Double();
                    star.moveTo(cx - dx, cy); star.lineTo(cx, cy - dy); star.lineTo(cx + dx, cy); star.lineTo(cx, cy + dy); star.closePath(); g.fill(star);
                }
            }
        }
        g.dispose();
        ImageIO.write(image, "png", new File(path));
        System.out.println("Preview: " + path);
    }
}
