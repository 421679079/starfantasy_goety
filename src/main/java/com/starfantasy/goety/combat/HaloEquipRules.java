package com.starfantasy.goety.combat;

import com.starfantasy.goety.item.HaloCurioItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

/** One halo or Ascian mask at a time, including cosmetic slots; target-slot replacement is allowed. */
public final class HaloEquipRules {
    public static boolean allows(SlotContext target){
        var inventory=CuriosApi.getCuriosInventory(target.entity()).resolve();if(inventory.isEmpty())return true;
        for(var entry:inventory.get().getCurios().entrySet())for(boolean cosmetic:new boolean[]{false,true}){
            var stacks=cosmetic?entry.getValue().getCosmeticStacks():entry.getValue().getStacks();
            for(int i=0;i<stacks.getSlots();i++){
                if(entry.getKey().equals(target.identifier())&&i==target.index()&&cosmetic==target.cosmetic())continue;
                var item=stacks.getStackInSlot(i).m_41720_();
                if(item instanceof HaloCurioItem||item instanceof com.starfantasy.goety.item.AscianMaskItem)return false;
            }
        }
        return true;
    }
    private HaloEquipRules(){}
}
