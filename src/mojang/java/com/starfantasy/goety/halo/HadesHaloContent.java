package com.starfantasy.goety.halo;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class HadesHaloContent {
    private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(ForgeRegistries.MENU_TYPES,HadesHaloContents.MODID);
    public static final RegistryObject<MenuType<HadesHaloMenu>> MENU=MENUS.register("hades_halo",()->IForgeMenuType.create(HadesHaloMenu::new));
    public static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation(HadesHaloContents.MODID,"halo_menu"),()->"1","1"::equals,"1"::equals);
    public static void init(IEventBus bus){
        MENUS.register(bus);
        CHANNEL.messageBuilder(OpenRequest.class,0,NetworkDirection.PLAY_TO_SERVER)
                .encoder((m,b)->{b.writeVarInt(m.container());b.writeVarInt(m.inventorySlot());})
                .decoder(b->new OpenRequest(b.readVarInt(),b.readVarInt()))
                .consumerMainThread((m,ctx)->{
                    ServerPlayer player=ctx.get().getSender();
                    if(player!=null&&player.containerMenu.containerId==m.container()
                            &&m.inventorySlot()>=0&&m.inventorySlot()<player.getInventory().getContainerSize()
                            &&!(player.containerMenu instanceof HadesHaloMenu)){
                        // Creative and Curios screens can number their menu slots differently.
                        boolean allowed=player.containerMenu.slots.stream().anyMatch(slot->slot.container==player.getInventory()
                                &&slot.getContainerSlot()==m.inventorySlot()&&slot.mayPickup(player));
                        if(allowed)open(player,m.inventorySlot());
                    }
                    ctx.get().setPacketHandled(true);
                }).add();
    }
    public static void openHand(Player player,InteractionHand hand){
        if(player instanceof ServerPlayer server)open(server,hand==InteractionHand.MAIN_HAND?player.getInventory().selected:40);
    }
    public static void open(ServerPlayer player,int inventorySlot){
        if(!player.isAlive()||player.isSpectator()||inventorySlot<0||inventorySlot>=player.getInventory().getContainerSize()
                ||!player.containerMenu.getCarried().isEmpty())return;
        var stack=player.getInventory().getItem(inventorySlot);
        if(!HadesHaloContents.isHades(stack)||stack.getCount()!=1)return;
        NetworkHooks.openScreen(player,new SimpleMenuProvider((id,inv,p)->new HadesHaloMenu(id,inv,inventorySlot),
                Component.translatable("item.starfantasy_goety.halo_of_hades")),buf->buf.writeVarInt(inventorySlot));
    }
    public record OpenRequest(int container,int inventorySlot){}
    private HadesHaloContent(){}
}
