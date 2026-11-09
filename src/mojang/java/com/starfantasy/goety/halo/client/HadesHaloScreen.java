package com.starfantasy.goety.halo.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.starfantasy.goety.halo.HadesHaloContents;
import com.starfantasy.goety.halo.HadesHaloMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class HadesHaloScreen extends AbstractContainerScreen<HadesHaloMenu> {
    private static final ResourceLocation CONTAINER=new ResourceLocation("minecraft","textures/gui/container/generic_54.png");
    private static final ResourceLocation EYE=new ResourceLocation("starfantasy_goety","textures/entity/hades/hades.png");
    private static final ResourceLocation TOGGLES=new ResourceLocation("curios","textures/gui/inventory.png");
    private static final int HEADER_HEIGHT = 17;
    private static final int INVENTORY_TOP = 100;
    private static final int INVENTORY_TEXTURE_TOP = 125;
    private static final int INVENTORY_HEIGHT = 97;
    private static final int BORDER_WIDTH = 7;
    private static final int BACKGROUND_TILE_HEIGHT = 14;
    private final java.util.List<PotionButton> potionButtons=new java.util.ArrayList<>();
    public HadesHaloScreen(HadesHaloMenu menu,Inventory inventory,Component title){
        super(menu,inventory,title);imageWidth=176;imageHeight=INVENTORY_TOP+INVENTORY_HEIGHT;titleLabelY=8;inventoryLabelX=8;inventoryLabelY=INVENTORY_TOP;
    }
    @Override protected void init(){
        super.init();titleLabelX=(imageWidth-font.width(title))/2;potionButtons.clear();
        for(int i=0;i<12;i++){
            var slot=menu.getSlot(i);var button=new PotionButton(leftPos+slot.x+11,topPos+slot.y-3,i);
            button.refreshVisibility();
            potionButtons.add(addRenderableWidget(button));
        }
    }
    private final class PotionButton extends AbstractButton {
        private final int slot;
        PotionButton(int x,int y,int slot){super(x,y,8,8,Component.translatable("tooltip.starfantasy_goety.halo.potions"));this.slot=slot;}
        void refreshVisibility(){visible=active=menu.getSlot(slot).hasItem();}
        @Override public void onPress(){if(menu.getSlot(slot).hasItem()&&minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,slot);}
        @Override protected void renderWidget(GuiGraphics g,int mouseX,int mouseY,float partial){}
        void renderOverlay(GuiGraphics g){
            if(!visible)return;
            RenderSystem.disableDepthTest();
            g.pose().pushPose();g.pose().translate(0,0,300);
            g.blit(TOGGLES,getX(),getY(),menu.potionsEnabled(slot)?75:83,0,8,8,256,256);g.pose().popPose();
            RenderSystem.enableDepthTest();
        }
        @Override protected void updateWidgetNarration(NarrationElementOutput output){defaultButtonNarrationText(output);}
    }
    private void part(GuiGraphics g,int sx,int sy,int x,int y,int width,int height){
        g.blit(CONTAINER,leftPos+x,topPos+y,sx,sy,width,height,256,256);
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY){
        int rightBorder = imageWidth - BORDER_WIDTH;
        int bodyHeight = INVENTORY_TOP - HEADER_HEIGHT;
        part(g,0,0,0,0,imageWidth,HEADER_HEIGHT);
        part(g,0,HEADER_HEIGHT,0,HEADER_HEIGHT,BORDER_WIDTH,bodyHeight);
        part(g,rightBorder,HEADER_HEIGHT,rightBorder,HEADER_HEIGHT,BORDER_WIDTH,bodyHeight);
        // Repeat the slot-free strip between the chest and inventory grids at native scale.
        for (int y = HEADER_HEIGHT; y < INVENTORY_TOP; y += BACKGROUND_TILE_HEIGHT) {
            part(g,BORDER_WIDTH,INVENTORY_TEXTURE_TOP,BORDER_WIDTH,y,imageWidth-2*BORDER_WIDTH,
                    Math.min(BACKGROUND_TILE_HEIGHT,INVENTORY_TOP-y));
        }
        for(int i=0;i<12;i++)part(g,7,17,HadesHaloContents.X[i%6]-9,HadesHaloContents.Y[i/6][i%6]-9,18,18);
        part(g,0,INVENTORY_TEXTURE_TOP,0,INVENTORY_TOP,imageWidth,INVENTORY_HEIGHT);
        RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        g.blit(EYE,leftPos+53,topPos+41,71,29,704,256,47,19,1024,1024);
        try{
            RenderSystem.setShaderColor(.65F,.65F,.65F,.32F);
            for(int i=0;i<12;i++)if(!menu.getSlot(i).hasItem()){
                var icon=new ResourceLocation(HadesHaloContents.MODID,"textures/item/"+HadesHaloContents.IDS.get(i)+".png");
                g.blit(icon,leftPos+menu.getSlot(i).x,topPos+menu.getSlot(i).y,16,16,0,0,64,64,64,64);
            }
        }finally{RenderSystem.setShaderColor(1,1,1,1);RenderSystem.disableBlend();}
    }
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        for(var button:potionButtons)button.refreshVisibility();
        renderBackground(g);super.render(g,mouseX,mouseY,partial);
        for(var button:potionButtons)button.renderOverlay(g);
        for(var button:potionButtons)if(button.visible&&button.isMouseOver(mouseX,mouseY)){
            g.renderTooltip(font,Component.translatable("tooltip.starfantasy_goety.halo.potions"),mouseX,mouseY);return;
        }
        var slot=getSlotUnderMouse();
        if(slot!=null&&slot.index<12&&menu.getCarried().isEmpty()){
            var preview=slot.hasItem()?slot.getItem().copy():HadesHaloContents.expected(slot.index).getDefaultInstance();
            if(!slot.hasItem())preview.setHoverName(Component.translatable("tooltip.starfantasy_goety.halo.uncollected").append(preview.getHoverName()));
            // Vanilla and Curios both honor HideFlags.MODIFIERS. Modify the tooltip copy only.
            var tag=preview.getOrCreateTag();tag.putInt("HideFlags",tag.getInt("HideFlags")|2);
            g.renderTooltip(font,preview,mouseX,mouseY);
        }else renderTooltip(g,mouseX,mouseY);
    }
}
