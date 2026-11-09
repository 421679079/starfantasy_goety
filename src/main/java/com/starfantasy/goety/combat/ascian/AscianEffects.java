package com.starfantasy.goety.combat.ascian;

import com.starfantasy.goety.config.AscianKingConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;

public final class AscianEffects {
    public record Effect(MobEffect type,int level,int ticks) {}
    public static List<Effect> parse(List<? extends String> list,boolean permanent) {
        List<Effect> result=new ArrayList<>();
        for(String value:list) try {
            String[] s=value.split(":");
            if(s.length!=(permanent?3:4)) continue;
            MobEffect effect=ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(s[0],s[1]));
            int rank=Integer.parseInt(s[2]), ticks=permanent?220:Integer.parseInt(s[3]);
            if(effect!=null && rank>0 && ticks>0) result.add(new Effect(effect,rank-1,ticks));
        } catch(RuntimeException ignored) { /* Invalid/missing optional effects do not break a fight. */ }
        return result;
    }
    public static void apply(LivingEntity target,List<? extends String> values,boolean permanent) {
        for(Effect e:parse(values,permanent)) {
            MobEffectInstance old=target.m_21124_(e.type);
            if(old==null || old.m_19564_()<e.level || old.m_19557_()<60)
                target.m_147207_(new MobEffectInstance(e.type,e.ticks,e.level,true,true),permanent?target:null);
        }
    }
    public static void hit(LivingEntity target,int phase) {
        for(Effect e:parse(AscianKingConfig.HIT_EFFECTS.get(Math.max(0,Math.min(5,phase-1))).get(),false))
            target.m_7292_(new MobEffectInstance(e.type,e.ticks,e.level,true,true));
    }
    public static Vec3 ground(Level level,double x,double y,double z) {
        for(int iy=Math.min(level.m_151558_()-1,(int)Math.floor(y));iy>level.m_141937_();iy--) {
            BlockPos p=BlockPos.m_274561_(x,iy,z);
            if(level.m_8055_(p.m_7495_()).m_60783_(level,p.m_7495_(),Direction.UP) && !level.m_8055_(p).m_280555_())
                return new Vec3(x,iy+.06,z);
        }
        return new Vec3(x,y+.06,z);
    }
    public static Vec3 ground(LivingEntity boss,LivingEntity target) {
        return ground(boss.m_9236_(),target.m_20185_(),Math.max(target.m_20186_(),boss.m_20186_())+8,target.m_20189_());
    }
    public static boolean sphere(AABB b,Vec3 p,double radius) {
        double x=Math.max(b.f_82288_-p.f_82479_,Math.max(0,p.f_82479_-b.f_82291_));
        double y=Math.max(b.f_82289_-p.f_82480_,Math.max(0,p.f_82480_-b.f_82292_));
        double z=Math.max(b.f_82290_-p.f_82481_,Math.max(0,p.f_82481_-b.f_82293_));
        return x*x+y*y+z*z<=radius*radius;
    }
    private AscianEffects() {}
}
