package com.starfantasy.goety.halo;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Edits the actual bound stack immediately; never returns a second inventory copy on close. */
public final class HadesHaloMenu extends AbstractContainerMenu {
    public final int sourceSlot;
    private final Inventory inventory;
    private final ItemStack anchor;
    private boolean ready;
    private final SimpleContainer contents;
    private final DataSlot disabledPotions=DataSlot.standalone();
    public HadesHaloMenu(int id,Inventory inventory,FriendlyByteBuf data){this(id,inventory,data.readVarInt());}
    public HadesHaloMenu(int id,Inventory inventory,int sourceSlot){
        super(HadesHaloContent.MENU.get(),id);this.inventory=inventory;this.sourceSlot=sourceSlot;
        anchor=sourceSlot>=0&&sourceSlot<inventory.getContainerSize()?inventory.getItem(sourceSlot):ItemStack.EMPTY;
        disabledPotions.set(HadesHaloContents.disabledPotions(anchor));addDataSlot(disabledPotions);
        contents=new SimpleContainer(12){
            @Override public int getMaxStackSize(){return 1;}
            @Override public boolean canPlaceItem(int slot,ItemStack stack){return HadesHaloContents.accepts(slot,stack);}
            @Override public void setChanged(){
                super.setChanged();
                if(ready&&!inventory.player.level().isClientSide&&stillValid(inventory.player)){
                    HadesHaloContents.write(anchor,this);inventory.setChanged();
                }
            }
        };
        var saved=HadesHaloContents.read(anchor);
        for(int i=0;i<12;i++){
            final int expected=i;contents.setItem(i,saved.get(i));
            addSlot(new Slot(contents,i,HadesHaloContents.X[i%6]-8,HadesHaloContents.Y[i/6][i%6]-8){
                @Override public boolean mayPlace(ItemStack stack){return HadesHaloContents.accepts(expected,stack);}
                @Override public int getMaxStackSize(){return 1;}
            });
        }
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addPlayerSlot(col+(row+1)*9,8+col*18,115+row*18);
        for(int col=0;col<9;col++)addPlayerSlot(col,8+col*18,173);
        ready=true;
    }
    public boolean potionsEnabled(int slot){return slot>=0&&slot<12&&(disabledPotions.get()&1<<slot)==0;}
    @Override public boolean clickMenuButton(Player player,int slot){
        if(slot<0||slot>=12||!stillValid(player)||player.level().isClientSide)return false;
        int mask=disabledPotions.get()^1<<slot;
        disabledPotions.set(mask);
        if(mask==0)anchor.removeTagKey(HadesHaloContents.POTIONS_DISABLED);
        else anchor.getOrCreateTag().putInt(HadesHaloContents.POTIONS_DISABLED,mask);
        inventory.setChanged();
        if(!potionsEnabled(slot))com.starfantasy.goety.combat.HaloPotionEffects.removeOwned(
                HadesHaloContents.expected(slot),player,HadesHaloContents.potionSources(anchor));
        return true;
    }
    private void addPlayerSlot(int inventoryIndex,int x,int y){
        addSlot(new Slot(inventory,inventoryIndex,x,y){
            @Override public boolean mayPickup(Player player){return inventoryIndex!=sourceSlot&&super.mayPickup(player);}
            @Override public boolean mayPlace(ItemStack stack){return inventoryIndex!=sourceSlot&&super.mayPlace(stack);}
        });
    }
    @Override public boolean stillValid(Player player){
        return player.isAlive()&&(player.level().isClientSide||sourceSlot>=0&&sourceSlot<inventory.getContainerSize()
                &&inventory.getItem(sourceSlot)==anchor&&HadesHaloContents.isHades(anchor)&&anchor.getCount()==1);
    }
    @Override public void clicked(int slot,int button,ClickType type,Player player){
        if(!stillValid(player)||type==ClickType.SWAP&&button==sourceSlot
                ||slot>=12&&slot<slots.size()&&getSlot(slot).getContainerSlot()==sourceSlot)return;
        super.clicked(slot,button,type,player);
    }
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(index<0||index>=slots.size()||!stillValid(player))return ItemStack.EMPTY;
        Slot slot=getSlot(index);if(!slot.hasItem()||!slot.mayPickup(player))return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(),copy=stack.copy();
        if(index<12){if(!moveItemStackTo(stack,12,slots.size(),true))return ItemStack.EMPTY;}
        else{
            int target=HadesHaloContents.IDS.indexOf(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath());
            if(target<0||!HadesHaloContents.accepts(target,stack)||!moveItemStackTo(stack,target,target+1,false))return ItemStack.EMPTY;
        }
        if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();
        slot.onTake(player,stack);return copy;
    }
}
