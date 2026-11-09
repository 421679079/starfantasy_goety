package com.starfantasy.goety.halo;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/** Only the twelve exact apostle items are accepted; Hades halos cannot nest. */
public final class HadesHaloContents {
    public static final String KEY="HadesApostleHalos", MODID="starfantasy_goety";
    public static final String POTIONS_DISABLED="HadesDisabledPotions";
    public static int disabledPotions(ItemStack halo){return isHades(halo)&&halo.hasTag()?halo.getTag().getInt(POTIONS_DISABLED)&4095:0;}
    public static boolean potionsEnabled(ItemStack halo,Item item){
        ResourceLocation id=ForgeRegistries.ITEMS.getKey(item);
        int slot=id!=null&&MODID.equals(id.getNamespace())?IDS.indexOf(id.getPath()):-1;
        return slot>=0&&(disabledPotions(halo)&1<<slot)==0;
    }
    public static List<Item> potionSources(ItemStack halo){
        var sources=new java.util.ArrayList<Item>();sources.add(halo.getItem());
        for(Item item:items(halo))if(potionsEnabled(halo,item))sources.add(item);
        return sources;
    }
    public static final List<String> IDS=List.of("halo_of_the_risen","halo_of_the_abhorrent","halo_of_the_defiler",
            "halo_of_the_dark","halo_of_the_great_shadow","halo_of_the_witch_king","halo_of_the_pyre_lord",
            "halo_of_the_profane","halo_of_the_cruel","halo_of_the_terrible","halo_of_the_glorious","halo_of_the_atrocious");
    public static final int[] X={19,46,73,103,130,157};
    public static final int[][] Y={{44,34,28,28,34,44},{66,76,82,82,76,66}};
    public static boolean isHades(ItemStack stack){return !stack.isEmpty()&&new ResourceLocation(MODID,"halo_of_hades").equals(ForgeRegistries.ITEMS.getKey(stack.getItem()));}
    public static Item expected(int slot){return ForgeRegistries.ITEMS.getValue(new ResourceLocation(MODID,IDS.get(slot)));}
    public static boolean accepts(int slot,ItemStack stack){return slot>=0&&slot<12&&!stack.isEmpty()&&stack.getItem()==expected(slot)&&stack.getCount()==1;}
    public static NonNullList<ItemStack> read(ItemStack halo){
        var contents=NonNullList.withSize(12,ItemStack.EMPTY);
        if(!isHades(halo)||!halo.hasTag())return contents;
        ListTag list=halo.getTag().getList(KEY,Tag.TAG_COMPOUND);
        for(int n=0;n<Math.min(12,list.size());n++){
            CompoundTag entry=list.getCompound(n);int slot=entry.getInt("Slot");
            ItemStack stack=ItemStack.of(entry);
            if(accepts(slot,stack)&&contents.get(slot).isEmpty())contents.set(slot,stack);
        }
        return contents;
    }
    public static boolean contains(ItemStack halo,Item item){
        if(!isHades(halo)||!halo.hasTag())return false;
        ResourceLocation id=ForgeRegistries.ITEMS.getKey(item);
        if(id==null||!MODID.equals(id.getNamespace()))return false;
        int slot=IDS.indexOf(id.getPath());if(slot<0)return false;
        ListTag list=halo.getTag().getList(KEY,Tag.TAG_COMPOUND);
        for(int n=0;n<Math.min(12,list.size());n++){
            CompoundTag entry=list.getCompound(n);
            if(entry.getInt("Slot")==slot&&entry.getByte("Count")==1&&id.toString().equals(entry.getString("id")))return true;
        }
        return false;
    }
    /** Identity-only snapshot shared by the tooltip and equipped visual; no nested stack decoding. */
    public static int collectedMask(ItemStack halo){
        if(!isHades(halo)||!halo.hasTag())return 0;
        int mask=0;ListTag list=halo.getTag().getList(KEY,Tag.TAG_COMPOUND);
        for(int n=0;n<Math.min(12,list.size());n++){
            CompoundTag entry=list.getCompound(n);int slot=entry.getInt("Slot");
            if(slot>=0&&slot<12&&entry.getByte("Count")==1
                    &&(MODID+":"+IDS.get(slot)).equals(entry.getString("id")))mask|=1<<slot;
        }
        return mask;
    }
    /** Effect ticks need identity only, not reconstruction of capability-bearing stacks. */
    public static List<Item> items(ItemStack halo){
        return IDS.stream().map(id->ForgeRegistries.ITEMS.getValue(new ResourceLocation(MODID,id)))
                .filter(item->item!=null&&contains(halo,item)).toList();
    }
    public static void write(ItemStack halo,Container contents){
        if(!isHades(halo))return;
        ListTag list=new ListTag();
        for(int i=0;i<12;i++)if(accepts(i,contents.getItem(i))){
            CompoundTag tag=contents.getItem(i).save(new CompoundTag());tag.putInt("Slot",i);list.add(tag);
        }
        if(list.isEmpty())halo.removeTagKey(KEY);else halo.getOrCreateTag().put(KEY,list);
    }
    private HadesHaloContents(){}
}
