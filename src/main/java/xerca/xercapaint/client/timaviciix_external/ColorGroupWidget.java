package xerca.xercapaint.client.timaviciix_external;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import xerca.xercapaint.Mod;
import xerca.xercapaint.PaletteUtil;

import java.util.function.Consumer;

public class ColorGroupWidget extends AbstractWidget {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Mod.modId, "textures/gui/advance_palette.png");

    private final int boxSize;
    private final int spacing;
    private final int columns;
    private final Consumer<Integer> onSelect;
    private final PaletteUtil.CustomColor[] customColors;
    private int selectedIndex = 0;

    public ColorGroupWidget(int x, int y, int boxSize, int spacing, int columns, Consumer<Integer> onSelect, PaletteUtil.CustomColor[] customColors) {
        super(x, y,
                columns * boxSize + (columns - 1) * spacing,  // 总宽度
                2 * boxSize + spacing,  // 总高度（2行）
                Component.empty());
        this.boxSize = boxSize;
        this.spacing = spacing;
        this.columns = columns;
        this.onSelect = onSelect;
        this.customColors = customColors;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack poseStack = guiGraphics.pose();

        // 计算带内边距的背景区域
        int padding = 2; // 5像素内边距
        int bgX = getX() - padding;
        int bgY = getY() - padding;
        int bgWidth = width + 2 * padding;  // 左右各加5px
        int bgHeight = height + 2 * padding; // 上下各加5px

        // 渲染背景纹理（带内边距的自适应）
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        guiGraphics.blit(BACKGROUND_TEXTURE,
                bgX, bgY,
                0, 0,
                bgWidth, bgHeight,
                bgWidth, bgHeight); // 拉伸纹理填充整个区域

        // 渲染所有色块
        for (int i = 0; i < customColors.length; i++) {
            int row = i / columns;
            int col = i % columns;
            int xPos = getX() + col * (boxSize + spacing);
            int yPos = getY() + row * (boxSize + spacing);

            // 绘制色块
            if (customColors[i].getNumberOfColors() == 0){
                guiGraphics.fill(xPos, yPos, xPos + boxSize, yPos + boxSize, PaletteUtil.emptinessColor.rgbVal());
            }else{
                guiGraphics.fill(xPos, yPos, xPos + boxSize, yPos + boxSize, customColors[i].getColor().rgbVal());
            }


            // 绘制选中边框
            if (i == selectedIndex) {
                drawSelectionBorder(guiGraphics, xPos, yPos);
            }
        }

        // ========== 绘制原版按钮边框 ==========
        int borderColor = 0xFFAAAAAA; // 原版按钮颜色
        drawBorder(guiGraphics, bgX, bgY, bgWidth, bgHeight, borderColor);
    }

    private void drawBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.hLine(x, x + width - 1, y, color); // 上边
        guiGraphics.hLine(x, x + width - 1, y + height - 1, color); // 下边
        guiGraphics.vLine(x, y, y + height - 1, color); // 左边
        guiGraphics.vLine(x + width - 1, y, y + height - 1, color); // 右边
    }

    private void drawSelectionBorder(GuiGraphics guiGraphics, int x, int y) {
        int borderColor = 0xFFFFFFFF; // 白色边框

        // 上边框
        guiGraphics.fill(x - 1, y - 1, x + boxSize + 1, y, borderColor);
        // 下边框
        guiGraphics.fill(x - 1, y + boxSize, x + boxSize + 1, y + boxSize + 1, borderColor);
        // 左边框
        guiGraphics.fill(x - 1, y - 1, x, y + boxSize + 1, borderColor);
        // 右边框
        guiGraphics.fill(x + boxSize, y - 1, x + boxSize + 1, y + boxSize + 1, borderColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !active) return false;

        for (int i = 0; i < customColors.length; i++) {
            int row = i / columns;
            int col = i % columns;
            int xPos = getX() + col * (boxSize + spacing);
            int yPos = getY() + row * (boxSize + spacing);

            // 检查点击是否在色块范围内
            if (mouseX >= xPos && mouseX < xPos + boxSize &&
                    mouseY >= yPos && mouseY < yPos + boxSize) {

                selectedIndex = i;
                onSelect.accept(selectedIndex);
                customColors[selectedIndex].setNumberOfColors(1);
                playDownSound(Minecraft.getInstance().getSoundManager());
                return true;
            }
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
