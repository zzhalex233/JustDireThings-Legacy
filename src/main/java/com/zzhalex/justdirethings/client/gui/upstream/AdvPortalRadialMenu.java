package com.zzhalex.justdirethings.client.gui.upstream;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.client.ClientPortalKeys;
import com.zzhalex.justdirethings.common.item.tool.ItemPortalGunV2;
import com.zzhalex.justdirethings.common.portal.PortalLinkData;
import com.zzhalex.justdirethings.common.util.DimensionDisplayHelper;
import com.zzhalex.justdirethings.network.JDTNetwork;
import com.zzhalex.justdirethings.network.message.MessagePortalGunFavorite;
import com.zzhalex.justdirethings.network.message.MessagePortalGunFavoriteChange;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AdvPortalRadialMenu extends GuiScreen {

    private static final int SEGMENTS = PortalLinkData.MAX_FAVORITES;
    private static final int RADIUS_MIN = 26;
    private static final int RADIUS_MAX = 120;
    private static final int BUTTON_ADD = 0;
    private static final int BUTTON_REMOVE = 1;
    private static final int BUTTON_EDIT = 2;
    private static final int BUTTON_STAY_OPEN = 3;
    private static final ResourceLocation ADD_TEXTURE = buttonTexture("add.png");
    private static final ResourceLocation REMOVE_TEXTURE = buttonTexture("remove.png");
    private static final ResourceLocation EDIT_TEXTURE = buttonTexture("matchnbttrue.png");
    private static final ResourceLocation STAY_OPEN_TEXTURE = buttonTexture("area.png");

    private int timeIn;
    private int slotHovered = -1;
    private int slotSelected;
    private ItemStack portalGun;
    private boolean staysOpen;

    public AdvPortalRadialMenu(ItemStack portalGun) {
        this.portalGun = portalGun == null ? ItemStack.EMPTY : portalGun;
        PortalLinkData data = getLinkData();
        slotSelected = data.getFavoriteIndex();
        staysOpen = data.isStayOpen();
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new IconButton(BUTTON_ADD, width / 2 - 150, height / 2 - 20, ADD_TEXTURE, "justdirethings.screen.add_favorite", true));
        buttonList.add(new IconButton(BUTTON_REMOVE, width / 2 + 140, height / 2 - 20, REMOVE_TEXTURE, "justdirethings.screen.remove_favorite", true));
        buttonList.add(new IconButton(BUTTON_EDIT, width / 2 - 150, height / 2 + 20, EDIT_TEXTURE, "justdirethings.screen.edit_favorite", true));
        buttonList.add(new IconButton(BUTTON_STAY_OPEN, width / 2 + 140, height / 2 + 20, STAY_OPEN_TEXTURE, "justdirethings.screen.stay_open", staysOpen));
    }

    @Override
    public void updateScreen() {
        portalGun = ItemPortalGunV2.findHeldPortalGun(mc.player);
        if (portalGun.isEmpty()) {
            mc.displayGuiScreen(null);
            return;
        }
        if (!staysOpen && !Keyboard.isKeyDown(ClientPortalKeys.TOGGLE_TOOL.getKeyCode())) {
            mc.displayGuiScreen(null);
        }
        timeIn++;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == BUTTON_ADD) {
            addFavorite();
        } else if (button.id == BUTTON_REMOVE) {
            removeFavorite();
        } else if (button.id == BUTTON_EDIT) {
            editFavorite();
        } else if (button.id == BUTTON_STAY_OPEN) {
            staysOpen = !staysOpen;
            saveFavorite();
            ((IconButton) button).setActive(staysOpen);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isInRange(mouseX, mouseY)) {
            saveFavorite();
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (staysOpen && (keyCode == Keyboard.KEY_ESCAPE || keyCode == ClientPortalKeys.TOGGLE_TOOL.getKeyCode())) {
            mc.displayGuiScreen(null);
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        renderRadial(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderTooltips(mouseX, mouseY);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void saveFavorite() {
        slotSelected = slotHovered;
        JDTNetwork.getChannel().sendToServer(new MessagePortalGunFavorite(slotSelected, staysOpen));
        playBeep();
    }

    public void addFavorite() {
        JDTNetwork.getChannel().sendToServer(new MessagePortalGunFavoriteChange(slotSelected, true, "UNNAMED", false, Vec3d.ZERO));
    }

    public void removeFavorite() {
        JDTNetwork.getChannel().sendToServer(new MessagePortalGunFavoriteChange(slotSelected, false, "NOTNEEDED", false, Vec3d.ZERO));
    }

    public void editFavorite() {
        Minecraft.getMinecraft().displayGuiScreen(new AdvPortalEditMenu(portalGun, slotSelected));
    }

    public PortalLinkData.PortalDestination getFavorite(int slot) {
        return getLinkData().getFavorite(slot);
    }

    public boolean isInRange(double mouseX, double mouseY) {
        int x = width / 2;
        int y = height / 2;
        double dist = new Vec3d(x, y, 0).distanceTo(new Vec3d(mouseX, mouseY, 0));
        return dist > RADIUS_MIN && dist < RADIUS_MAX;
    }

    private void renderRadial(int mouseX, int mouseY, float partialTicks) {
        float fract = Math.min(5.0F, timeIn + partialTicks) / 5.0F;
        int x = width / 2;
        int y = height / 2;
        boolean inRange = isInRange(mouseX, mouseY);
        float angle = mouseAngle(x, y, mouseX, mouseY);
        float totalDeg = 0.0F;
        float degPer = 360.0F / SEGMENTS;

        for (int seg = 0; seg < SEGMENTS; seg++) {
            PortalLinkData.PortalDestination favorite = getFavorite(seg);
            String favoriteName = favorite == null || favorite.isEmpty() ? "Empty" : favorite.getName();
            String dimension = favorite == null || favorite.isEmpty() ? "" : DimensionDisplayHelper.getDimensionName(favorite.getDimension());
            String coordinates = favorite == null || favorite.isEmpty() ? "" : String.format("(%d, %d, %d)", (int) favorite.getX(), (int) favorite.getY(), (int) favorite.getZ());
            boolean mouseInSector = isCursorInSlice(angle, totalDeg, degPer, inRange);
            float radius = Math.max(0.0F, Math.min((timeIn + partialTicks - seg / (float) SEGMENTS) * 25.0F, RADIUS_MAX)) * fract;
            float gray = 0.25F + (seg % 2 == 0 ? 0.1F : 0.0F);
            float red = gray;
            float green = gray;
            float blue = gray;
            float alpha = 0.4F;
            if (mouseInSector) {
                slotHovered = seg;
                red = green = blue = 1.0F;
            }
            if (seg == slotSelected) {
                red = green = 1.0F;
                alpha = 0.6F;
            }
            drawSlice(x, y, radius, radius / 2.3F, totalDeg, degPer, red, green, blue, alpha);
            totalDeg += degPer;
            drawFavoriteText(x, y, radius, totalDeg - degPer / 2.0F, favoriteName, dimension, coordinates);
        }
    }

    private void drawFavoriteText(int x, int y, float radius, float degrees, String name, String dimension, String coordinates) {
        if (radius <= 0.0F) {
            return;
        }
        float nameAngle = (float) Math.toRadians(degrees);
        float nameX = x + (float) (Math.cos(nameAngle) * (RADIUS_MAX / 1.4D));
        float nameY = y + (float) (Math.sin(nameAngle) * (RADIUS_MAX / 1.4D));
        GlStateManager.pushMatrix();
        GlStateManager.translate(nameX, nameY, 0.0F);
        GlStateManager.scale(0.85F, 0.85F, 0.85F);
        if (nameAngle > Math.PI / 2.0D && nameAngle < 3.0D * Math.PI / 2.0D) {
            GlStateManager.rotate((float) Math.toDegrees(nameAngle + Math.PI), 0.0F, 0.0F, 1.0F);
        } else {
            GlStateManager.rotate((float) Math.toDegrees(nameAngle), 0.0F, 0.0F, 1.0F);
        }
        fontRenderer.drawString(name, -fontRenderer.getStringWidth(name) / 2, -15, 0xFFFFFF);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(nameX, nameY, 0.0F);
        GlStateManager.scale(0.7F, 0.7F, 0.7F);
        if (nameAngle > Math.PI / 2.0D && nameAngle < 3.0D * Math.PI / 2.0D) {
            GlStateManager.rotate((float) Math.toDegrees(nameAngle + Math.PI), 0.0F, 0.0F, 1.0F);
        } else {
            GlStateManager.rotate((float) Math.toDegrees(nameAngle), 0.0F, 0.0F, 1.0F);
        }
        fontRenderer.drawString(dimension, -fontRenderer.getStringWidth(dimension) / 2, -5, 0xD3D3D3);
        fontRenderer.drawString(coordinates, -fontRenderer.getStringWidth(coordinates) / 2, 10, 0xD3D3D3);
        GlStateManager.popMatrix();
    }

    private static void drawSlice(int x, int y, float outerRadius, float innerRadius, float startDegrees, float degrees, float red, float green, float blue, float alpha) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(5, DefaultVertexFormats.POSITION_COLOR);
        for (float i = degrees; i >= 0.0F; i -= 2.0F) {
            float radians = (float) Math.toRadians(i + startDegrees);
            buffer.pos(x + Math.cos(radians) * innerRadius, y + Math.sin(radians) * innerRadius, 0.0D).color(red, green, blue, alpha).endVertex();
            buffer.pos(x + Math.cos(radians) * outerRadius, y + Math.sin(radians) * outerRadius, 0.0D).color(red, green, blue, alpha).endVertex();
        }
        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    private void renderTooltips(int mouseX, int mouseY) {
        for (GuiButton button : buttonList) {
            if (button instanceof IconButton && button.visible
                    && mouseX >= button.x && mouseY >= button.y
                    && mouseX < button.x + button.width && mouseY < button.y + button.height) {
                List<String> tooltip = new ArrayList<>();
                tooltip.add(I18n.format(((IconButton) button).tooltipKey));
                drawHoveringText(tooltip, mouseX, mouseY);
            }
        }
    }

    private PortalLinkData getLinkData() {
        if (portalGun != null && !portalGun.isEmpty() && portalGun.getItem() instanceof ItemPortalGunV2) {
            return ((ItemPortalGunV2) portalGun.getItem()).getLinkData(portalGun);
        }
        return new PortalLinkData();
    }

    private void playBeep() {
        if (mc.world != null && mc.player != null) {
            mc.world.playSound(mc.player, mc.player.posX, mc.player.posY, mc.player.posZ, SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, 0.5F, 1.0F);
        }
    }

    private static boolean isCursorInSlice(float angle, float totalDeg, float degPer, boolean inRange) {
        return inRange && angle > totalDeg && angle < totalDeg + degPer;
    }

    private static float mouseAngle(int x, int y, int mouseX, int mouseY) {
        Vector2f baseVec = new Vector2f(1.0F, 0.0F);
        Vector2f mouseVec = new Vector2f(mouseX - x, mouseY - y);
        float length = baseVec.length() * mouseVec.length();
        if (length <= 0.0F) {
            return 0.0F;
        }
        float dot = baseVec.dot(mouseVec) / length;
        float angle = (float) (Math.acos(Math.max(-1.0F, Math.min(1.0F, dot))) * 180.0F / Math.PI);
        return mouseY < y ? 360.0F - angle : angle;
    }

    private static ResourceLocation buttonTexture(String name) {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/buttons/" + name);
    }

    private static final class IconButton extends GuiButton {

        private final ResourceLocation texture;
        private final String tooltipKey;
        private boolean active;

        private IconButton(int id, int x, int y, ResourceLocation texture, String tooltipKey, boolean active) {
            super(id, x, y, 16, 16, "");
            this.texture = texture;
            this.tooltipKey = tooltipKey;
            this.active = active;
        }

        private void setActive(boolean active) {
            this.active = active;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (!visible) {
                return;
            }
            hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
            mc.getTextureManager().bindTexture(texture);
            GlStateManager.color(active ? 1.0F : 0.33F, active ? 1.0F : 0.33F, active ? 1.0F : 0.33F, 1.0F);
            drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private static final class Vector2f {
        private final float x;
        private final float y;

        private Vector2f(float x, float y) {
            this.x = x;
            this.y = y;
        }

        private float dot(Vector2f vector) {
            return x * vector.x + y * vector.y;
        }

        private float length() {
            return (float) Math.sqrt(x * x + y * y);
        }
    }
}
