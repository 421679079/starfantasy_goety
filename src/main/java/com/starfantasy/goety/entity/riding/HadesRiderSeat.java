package com.starfantasy.goety.entity.riding;

import com.google.gson.*;
import com.starfantasy.goety.entity.HadesServantEntity;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** The two animated ancestors of the head seat, evaluated identically on client and server. */
public final class HadesRiderSeat {
    private static final String ROOT = "/assets/starfantasy_goety/";
    private static final JsonObject ANIMATIONS = read(ROOT + "animations/entity/hades.animation.json").getAsJsonObject("animations");
    private static final Map<String, JsonObject> BONES = bones();
    private static final Map<String, Track> TRACKS = new HashMap<>();
    static {
        for (var animation : ANIMATIONS.entrySet()) {
            JsonObject bones = animation.getValue().getAsJsonObject().getAsJsonObject("bones");
            for (String name : List.of("body", "h_head")) {
                JsonObject bone = bones.getAsJsonObject(name);
                for (String channel : List.of("position", "rotation", "scale")) {
                    TRACKS.put(animation.getKey() + "/" + name + "/" + channel,
                            new Track(bone == null ? null : bone.get(channel), channel.equals("scale") ? 1 : 0));
                }
            }
        }
    }

    private HadesRiderSeat() { }

    public record BonePose(Vector3f position, Vector3f rotation, Vector3f scale) { }
    public record Pose(BonePose body, BonePose head) { }

    public static Pose pose(HadesServantEntity entity, float tickOffset) {
        return poseAt(entity, entity.attackAge() + tickOffset, entity.m_9236_().m_46467_() + tickOffset);
    }

    public static Pose poseAt(HadesServantEntity entity, double attackTicks, double idleTicks) {
        String name;
        double ticks = Math.max(0, attackTicks);
        if (entity.attackType() == HadesServantEntity.ROUNDHOUSE) name = "roundhouse";
        else if (entity.attackType() == HadesServantEntity.CLAW_COMBO) {
            name = entity.attackAge() < 30 ? "claw1" : "claw2";
            if (entity.attackAge() >= 30) ticks = Math.max(0, ticks - 30);
        } else if (entity.attackType() == HadesServantEntity.DIVE_RAY) {
            name = entity.attackAge() < 60 ? "shoot" : "overhead_swipe";
            if (entity.attackAge() >= 60) ticks = Math.max(0, ticks - 60);
        } else if (entity.attackType() == HadesServantEntity.INFERNAL_JUDGMENT) {
            name = "smash";
            ticks = Math.min(ticks, 80);
        } else {
            name = entity.isRiderMoving() ? "walk" : "idle";
            ticks = (idleTicks % 80 + 80) % 80;
        }
        return new Pose(bone(name, "body", ticks / 20), bone(name, "h_head", ticks / 20));
    }

    private static BonePose bone(String animation, String name, double time) {
        String key = animation + "/" + name + "/";
        Vector3f rotation = TRACKS.get(key + "rotation").at(time);
        // GeckoLib converts Bedrock animation X/Y rotation signs, retaining Z.
        rotation.mul(-(float)Math.PI / 180, -(float)Math.PI / 180, (float)Math.PI / 180);
        return new BonePose(TRACKS.get(key + "position").at(time), rotation, TRACKS.get(key + "scale").at(time));
    }

    public static Vec3 position(HadesServantEntity entity, float tickOffset) {
        return position(pose(entity, tickOffset), entity.m_20182_(), entity.f_20883_);
    }

    public static Vec3 position(Pose pose, Vec3 origin, float bodyYaw) {
        Vector3f seat = vector(BONES.get("rider_seat").get("pivot"));
        seat.mul(-1F / 16, 1F / 16, 1F / 16);
        return headPosition(pose, origin, bodyYaw, seat);
    }

    public static Vec3 chainPosition(Pose pose, Vec3 origin, float bodyYaw) {
        Vector3f core = vector(BONES.get("hades_apostle_fusion_core").get("pivot"));
        core.mul(-1F / 16, 1F / 16, 1F / 16);
        return transformedPosition(bodyTransform(pose, bodyYaw), origin, core);
    }

    private static Vec3 headPosition(Pose pose, Vec3 origin, float bodyYaw, Vector3f point) {
        Matrix4f matrix = bodyTransform(pose, bodyYaw);
        apply(matrix, "h_head", pose.head());
        return transformedPosition(matrix, origin, point);
    }

    private static Matrix4f bodyTransform(Pose pose, float bodyYaw) {
        Matrix4f matrix = new Matrix4f().rotateY((float)Math.toRadians(180 - bodyYaw)).translate(0, 0.01F, 0);
        apply(matrix, "body", pose.body());
        return matrix;
    }

    private static Vec3 transformedPosition(Matrix4f matrix, Vec3 origin, Vector3f point) {
        matrix.transformPosition(point);
        return origin.m_82520_(point.x, point.y, point.z);
    }

    private static void apply(Matrix4f matrix, String name, BonePose pose) {
        Vector3f pivot = vector(BONES.get(name).get("pivot")).mul(-1F / 16, 1F / 16, 1F / 16);
        Vector3f p = pose.position(), r = pose.rotation(), s = pose.scale();
        matrix.translate(-p.x / 16, p.y / 16, p.z / 16).translate(pivot)
                .rotateZ(r.z).rotateY(r.y).rotateX(r.x).scale(s).translate(-pivot.x, -pivot.y, -pivot.z);
    }

    private static Map<String, JsonObject> bones() {
        Map<String, JsonObject> result = new HashMap<>();
        JsonArray bones = read(ROOT + "geo/entity/hades.geo.json").getAsJsonArray("minecraft:geometry")
                .get(0).getAsJsonObject().getAsJsonArray("bones");
        for (JsonElement element : bones) {
            JsonObject bone = element.getAsJsonObject();
            result.put(bone.get("name").getAsString(), bone);
        }
        return result;
    }

    private static JsonObject read(String path) {
        try (var stream = Objects.requireNonNull(HadesRiderSeat.class.getResourceAsStream(path), path);
             var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception exception) { throw new IllegalStateException("Cannot read Hades seat animation: " + path, exception); }
    }

    private static Vector3f vector(JsonElement value) {
        if (value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            return vector(object.has("vector") ? object.get("vector") : object.get("post"));
        }
        if (value.isJsonPrimitive()) return new Vector3f(value.getAsFloat());
        JsonArray a = value.getAsJsonArray();
        return new Vector3f(a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat());
    }

    private record Key(double time, Vector3f value, boolean smooth) { }
    private static final class Track {
        private final List<Key> keys = new ArrayList<>();
        Track(JsonElement channel, float fallback) {
            if (channel == null || !channel.isJsonObject()) {
                keys.add(new Key(0, channel == null ? new Vector3f(fallback) : vector(channel), false));
            } else {
                for (var entry : channel.getAsJsonObject().entrySet()) {
                    JsonElement value = entry.getValue();
                    boolean smooth = value.isJsonObject() && value.getAsJsonObject().has("lerp_mode")
                            && value.getAsJsonObject().get("lerp_mode").getAsString().equals("catmullrom");
                    keys.add(new Key(Double.parseDouble(entry.getKey()), vector(value), smooth));
                }
                keys.sort(Comparator.comparingDouble(Key::time));
            }
        }
        Vector3f at(double time) {
            if (time <= keys.get(0).time()) return new Vector3f(keys.get(0).value());
            for (int i = 1; i < keys.size(); i++) {
                Key a = keys.get(i - 1), b = keys.get(i);
                if (time > b.time()) continue;
                float t = (float)((time - a.time()) / (b.time() - a.time()));
                if (!a.smooth() && !b.smooth()) return new Vector3f(a.value()).lerp(b.value(), t);
                Vector3f p0 = keys.get(Math.max(0, i - 2)).value(), p3 = keys.get(Math.min(keys.size() - 1, i + 1)).value();
                Vector3f result = new Vector3f();
                for (int axis = 0; axis < 3; axis++) {
                    float v0 = p0.get(axis), v1 = a.value().get(axis), v2 = b.value().get(axis), v3 = p3.get(axis);
                    result.setComponent(axis, 0.5F * (2 * v1 + (-v0 + v2) * t
                            + (2*v0 - 5*v1 + 4*v2 - v3)*t*t + (-v0 + 3*v1 - 3*v2 + v3)*t*t*t));
                }
                return result;
            }
            return new Vector3f(keys.get(keys.size() - 1).value());
        }
    }
}
