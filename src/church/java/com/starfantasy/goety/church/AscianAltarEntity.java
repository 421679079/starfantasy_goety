package com.starfantasy.goety.church;

import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.registries.ForgeRegistries;

/** Confirmation belongs to this block and player, never to a global position cache. */
public final class AscianAltarEntity extends BlockEntity {
    private static final ResourceLocation BOSS=new ResourceLocation(ChurchContent.MODID,"ascian_king");
    private final Map<UUID,Long> confirmations=new HashMap<>();
    private boolean consumed;
    public AscianAltarEntity(BlockPos pos,BlockState state){super(ChurchContent.ASCIAN_ALTAR_ENTITY.get(),pos,state);}
    public void use(Player player){
        if(!(level instanceof ServerLevel server)||player.isSpectator()||consumed||isRemoved())return;
        if(server.dimension()!=Level.NETHER||worldPosition.getY()<128){tell(player,"location");return;}
        if(server.getDifficulty()==Difficulty.PEACEFUL){tell(player,"peaceful");return;}
        var type=ForgeRegistries.ENTITY_TYPES.getValue(BOSS);
        if(type==null)return;
        // Include materializing bosses and corpses until they have actually left the world.
        if(!server.getEntitiesOfClass(Mob.class,new AABB(worldPosition).inflate(96),e->e.getType()==type&&!e.isRemoved()).isEmpty()){
            confirmations.clear();tell(player,"nearby");return;
        }
        long now=server.getGameTime();
        confirmations.entrySet().removeIf(e->now<e.getValue()||now-e.getValue()>=100);
        Long first=confirmations.get(player.getUUID());
        if(first==null){if(confirmations.size()>=64)confirmations.clear();confirmations.put(player.getUUID(),now);tell(player,"confirm");return;}
        if(now==first)return; // Offhand and duplicate packets cannot confirm on the initial tick.
        if(!(type.create(server) instanceof Mob boss))return;
        Vec3 home=Vec3.atBottomCenterOf(worldPosition);
        boss.moveTo(home.x,home.y,home.z,player.getYRot()+180,0);
        // The SRG boss owns the persisted/synced 60-tick materialization clock.
        CompoundTag tag=new CompoundTag();boss.saveWithoutId(tag);tag.putInt("AscianSummonTicks",60);boss.load(tag);
        boss.setPersistenceRequired();
        if(!player.isCreative())boss.setTarget(player);
        BlockState altarState=getBlockState();
        consumed=true;confirmations.clear();
        // Remove the altar before checking its former space or adding the boss to the world.
        if(!server.destroyBlock(worldPosition,false,player)){consumed=false;tell(player,"failed");return;}
        if(!server.noCollision(boss,boss.getBoundingBox())){restoreAltar(server,altarState);tell(player,"blocked");return;}
        if(!server.addFreshEntity(boss)){restoreAltar(server,altarState);tell(player,"failed");}
        // Halo remains in the player's hand. A restored altar starts a fresh confirmation.
    }
    private void restoreAltar(ServerLevel server,BlockState state){
        if(server.isEmptyBlock(worldPosition))server.setBlock(worldPosition,state,3);
    }
    private static void tell(Player player,String key){
        player.displayClientMessage(Component.translatable("message.starfantasy_goety.ascian_altar."+key).withStyle(ChatFormatting.RED),true);
    }
}
