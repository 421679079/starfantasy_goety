package com.starfantasy.goety.client;

import com.Polarice3.Goety.client.events.ClientEvents;
import com.Polarice3.Goety.client.gui.radial.DrawingContext;
import com.Polarice3.Goety.client.gui.radial.GenericRadialMenu;
import com.Polarice3.Goety.client.gui.radial.IRadialMenuHost;
import com.Polarice3.Goety.client.gui.radial.RadialMenuItem;
import com.mojang.blaze3d.systems.RenderSystem;
import com.starfantasy.goety.magic.FinalStaffSchool;
import com.starfantasy.goety.network.ServerboundStaffSchoolPacket;
import com.starfantasy.goety.network.StarFantasyGoetyNetwork;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/** Uses Goety's focus wheel, including its sectors, hover selection and animation. */
public final class FinalStaffSchoolScreen extends Screen implements IRadialMenuHost {
    private final int slot;
    private final ItemStack openedStack;
    private final FinalStaffSchool original;
    private GenericRadialMenu menu;
    private boolean finished;

    public FinalStaffSchoolScreen(int slot, ItemStack stack) {
        super(Component.translatable("key.starfantasy_goety.staff_school"));
        this.slot = slot;
        this.openedStack = stack.copy();
        this.original = FinalStaffSchool.get(stack);
    }

    @Override protected void init() {
        if (menu != null) return;
        menu = new GenericRadialMenu(minecraft,
                new ResourceLocation("goety", "textures/gui/focus_wheel.png"), this) {
            @Override public void onClickOutside() { finish(null); }
        };
        menu.setCentralItem(openedStack);
        for (FinalStaffSchool school : FinalStaffSchool.values()) menu.add(new SchoolItem(school));
        menu.tick();
    }

    private boolean validStack() {
        return minecraft != null && minecraft.player != null && minecraft.player.isAlive()
                && !minecraft.player.isSpectator() && !minecraft.player.isUsingItem()
                && (slot == 40 || minecraft.player.getInventory().selected == slot)
                && ItemStack.matches(openedStack, minecraft.player.getInventory().getItem(slot));
    }

    @Override public void tick() {
        if (minecraft == null || minecraft.level == null || !minecraft.isWindowActive()
                || (!finished && !validStack())) { onClose(); return; }
        menu.tick();
        if (menu.isClosed()) onClose();
    }

    @Override public boolean isPauseScreen() { return false; }
    @Override public Screen getScreen() { return this; }
    @Override public Font getFontRenderer() { return font; }
    @Override public void renderTooltip(GuiGraphics graphics, ItemStack stack, int x, int y) {
        graphics.renderTooltip(font, stack, x, y);
    }

    // IN_GAME becomes inactive while any Screen is open. Poll the physical binding
    // with a GUI-independent modifier context instead (also supports mouse rebinds).
    private boolean shortcutHeld() {
        return ClientEvents.isKeyDown0(FinalStaffSchoolKeys.SWITCH)
                && FinalStaffSchoolKeys.SWITCH.getKeyModifier().isActive(KeyConflictContext.UNIVERSAL);
    }

    private void finish(FinalStaffSchool selected) {
        if (finished) return;
        finished = true;
        if (selected != null && selected != original && validStack()) {
            StarFantasyGoetyNetwork.selectStaffSchool(
                    new ServerboundStaffSchoolPacket(slot, selected.id, original.id));
        }
        menu.close();
    }

    @Override public boolean mouseClicked(double x, double y, int button) {
        if (button == 1) { onClose(); return true; }
        return super.mouseClicked(x, y, button);
    }

    @Override public boolean mouseReleased(double x, double y, int button) {
        if (button == 0 && menu.isReady() && !finished) { menu.clickItem(); return true; }
        return super.mouseReleased(x, y, button);
    }

    @Override public boolean keyPressed(int key, int scan, int modifiers) {
        if (finished) return true;
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            if (menu.isReady()) menu.clickItem();
            return true;
        }
        if (key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_DOWN || key == GLFW.GLFW_KEY_TAB) {
            if (hasShiftDown()) menu.cyclePrevious(); else menu.cycleNext();
            scaleKeyboardCursor();
            return true;
        }
        if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_UP) {
            menu.cyclePrevious(); scaleKeyboardCursor(); return true;
        }
        return super.keyPressed(key, scan, modifiers);
    }

    private float wheelScale() {
        return Math.max(.5F, Math.min(1.5F, Math.min((width - 24) / 120.0F, (height - 52) / 120.0F)));
    }

    /** Shared by native drawing and hit testing, scoped to this wheel's host. */
    public void configureWheelRadii(GenericRadialMenu wheel, boolean hitTest) {
        float progress = hitTest ? 1 : wheel.radiusOut / 60.0F;
        // Final inner radius stays at the original 30px; outer radius is 90px.
        // Only exceptionally small windows need a smaller central dead zone.
        wheel.radiusIn = Math.min(30.0F / wheelScale(), 40.0F) * progress;
        wheel.radiusOut = 60 * progress;
        // A 70px final orbit leaves space for ten enlarged, separate buttons.
        wheel.itemRadius = (140.0F / 3.0F) * progress;
    }

    private void scaleKeyboardCursor() {
        // Goety's keyboard cycling warps the cursor to its unscaled item position.
        var window = minecraft.getWindow();
        double[] x = new double[1], y = new double[1];
        GLFW.glfwGetCursorPos(window.getWindow(), x, y);
        double cx = window.getScreenWidth() / 2.0, cy = window.getScreenHeight() / 2.0;
        GLFW.glfwSetCursorPos(window.getWindow(), cx + (x[0] - cx) * wheelScale(),
                cy + (y[0] - cy) * wheelScale());
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Native draw updates the sector under the latest cursor position before
        // release is committed, avoiding last-frame selection at a sector boundary.
        float scale = wheelScale();
        float cx = width / 2, cy = height / 2;
        graphics.flush();
        // Native sectors use raw vertices while icons use GuiGraphics. Transform
        // their shared model-view matrix so both enlarge by exactly the same amount.
        var modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.translate(cx, cy, 0);
        modelView.scale(scale, scale, 1);
        modelView.translate(-cx, -cy, 0);
        RenderSystem.applyModelViewMatrix();
        try {
            menu.draw(graphics, partialTick, Math.round(cx + (mouseX - cx) / scale),
                    Math.round(cy + (mouseY - cy) / scale));
            graphics.flush();
        } finally {
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (!finished && menu.isReady()) {
            text(graphics, Component.translatable("tooltip.starfantasy_goety.staff_school", original.label()),
                    cx, cy - 60 * scale - 12, width - 16, 9, 0xFFFFFF);
            int hintY = Math.round(cy + 60 * scale + 8);
            for (var line : font.split(Component.translatable("screen.starfantasy_goety.school_hint"), width - 24)) {
                graphics.drawCenteredString(font, line, Math.round(cx), hintY, 0xC4BACD);
                hintY += font.lineHeight;
            }
            if (!shortcutHeld()) menu.clickItem();
        }
    }

    private final class SchoolItem extends RadialMenuItem {
        private final FinalStaffSchool school;
        SchoolItem(FinalStaffSchool school) {
            super(menu);
            this.school = school;
            setVisible(true);
        }
        @Override public boolean onClick() { finish(school); return true; }
        @Override public void draw(DrawingContext context) {
            GuiGraphics graphics = context.guiGraphics();
            int x = Math.round(context.x()), y = Math.round(context.y());
            if (school == original) disc(graphics, x, y, 13, 0xFFEDE2C8);
            disc(graphics, x, y, 12, 0xFF000000 | school.color);
            disc(graphics, x, y, 11, 0xFF000000 | tint(school.color, .18F));
            if (isHovered()) disc(graphics, x, y, 10, 0xFF000000 | tint(school.color, .32F));
            Component label = school.label();
            String name = label.getString();
            if (name.length() >= 8 && font.width(label) > 26 && name.matches("[A-Za-z]+")) {
                // Long English names stay readable instead of shrinking to one
                // extremely thin line; both lines remain inside the medallion.
                int middle = name.length() / 2;
                text(graphics, Component.literal(name.substring(0, middle)), x, y - 3.3F, 18, 5, 0xFFF4E9);
                text(graphics, Component.literal(name.substring(middle)), x, y + 3.3F, 18, 5, 0xFFF4E9);
            } else {
                text(graphics, label, x, y, 20, 8, 0xFFF4E9);
            }
        }
        @Override public void drawTooltips(DrawingContext context) {
            context.guiGraphics().renderTooltip(font, school.label(), (int) context.x(), (int) context.y());
        }
    }

    private void text(GuiGraphics graphics, Component label, float x, float y, float maxWidth, float maxHeight, int color) {
        float size = Math.min(1, Math.min(maxWidth / Math.max(1, font.width(label)), maxHeight / font.lineHeight));
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(size, size, 1);
        graphics.drawString(font, label, -font.width(label)/2, -font.lineHeight/2, color, true);
        graphics.pose().popPose();
    }

    private static int tint(int rgb, float amount) {
        return ((int) (((rgb >> 16) & 255)*amount) << 16)
                | ((int) (((rgb >> 8) & 255)*amount) << 8) | (int) ((rgb & 255)*amount);
    }

    private static void disc(GuiGraphics graphics, int x, int y, int radius, int color) {
        for (int row = -radius; row < radius; row++) {
            int half = (int) Math.floor(Math.sqrt(radius * radius - (row + .5) * (row + .5)));
            graphics.fill(x - half, y + row, x + half, y + row + 1, color);
        }
    }
}
