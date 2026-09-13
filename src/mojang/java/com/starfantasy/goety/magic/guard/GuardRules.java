package com.starfantasy.goety.magic.guard;

/** Pure timing and geometry rules, shared by the channel and its regression checks. */
public final class GuardRules {
    public static final int PETALS = 7;
    private GuardRules() {}
    public static int duration(int base, int duratio) {
        return base + Math.min(4, Math.max(0, duratio) / 2);
    }
    public static boolean inFront(double lookX, double lookZ, double sourceX, double sourceZ) {
        return lookX * sourceX + lookZ * sourceZ > 1.0E-7D;
    }
    public static double petalAngle(int index) {
        return Math.PI / 2.0D + index * Math.PI * 2.0D / PETALS;
    }
    public static final class UsePress {
        private boolean spent;
        public void started() { spent = true; }
        public boolean blocks(boolean guardStaff) { return spent && guardStaff; }
        public void release() { spent = false; }
    }
    public static final class Cast {
        public final long start;
        public final int duration;
        private boolean successful;
        private boolean finished;
        public Cast(long start, int duration) { this.start = start; this.duration = duration; }
        public boolean active(long now) { return !finished && now >= start && now - start < duration; }
        public boolean succeed() {
            if (finished) return false;
            boolean first = !successful;
            successful = true;
            return first;
        }
        /** True only for the first unsuccessful finish, which must charge cooldown. */
        public boolean finish() {
            if (finished) return false;
            finished = true;
            return !successful;
        }
    }
}
