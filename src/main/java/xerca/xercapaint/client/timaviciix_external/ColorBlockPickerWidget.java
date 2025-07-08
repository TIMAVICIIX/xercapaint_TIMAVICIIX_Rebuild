package xerca.xercapaint.client.timaviciix_external;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ColorBlockPickerWidget extends AbstractWidget {
    private static final int COLOR_AREA_SIZE = 100;
    private static final int HUE_SLIDER_HEIGHT = 15;
    private static final int PADDING = 5;

    private float hue = 0.0f;
    private float saturation = 1.0f;
    private float brightness = 1.0f;
    private int selectedColor;

    private boolean draggingColorArea = false;
    private boolean draggingHueSlider = false;

    private final Consumer<Integer> onColorChanged;

    private DynamicTexture colorAreaTexture;
    private ResourceLocation colorAreaTextureLocation;
    private boolean colorAreaDirty = true;

    private DynamicTexture hueSliderTexture;
    private ResourceLocation hueSliderTextureLocation;
    private boolean hueSliderDirty = true;

    public ColorBlockPickerWidget(int x, int y, Consumer<Integer> onColorChanged) {
        super(x, y, COLOR_AREA_SIZE, COLOR_AREA_SIZE + HUE_SLIDER_HEIGHT + PADDING, Component.literal("Color Picker"));
        this.onColorChanged = onColorChanged;
        updateSelectedColor();
        initTextures();
    }

    private void initTextures() {
        // 使用唯一ID注册纹理，避免冲突
        String uniqueId = String.valueOf(System.identityHashCode(this));
        colorAreaTexture = new DynamicTexture(COLOR_AREA_SIZE, COLOR_AREA_SIZE, true);
        colorAreaTextureLocation = Minecraft.getInstance().getTextureManager().register(
                "color_area_" + uniqueId, colorAreaTexture);

        hueSliderTexture = new DynamicTexture(COLOR_AREA_SIZE, HUE_SLIDER_HEIGHT, true);
        hueSliderTextureLocation = Minecraft.getInstance().getTextureManager().register(
                "hue_slider_" + uniqueId, hueSliderTexture);

        colorAreaDirty = true;
        hueSliderDirty = true;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack poseStack = guiGraphics.pose();

        if (colorAreaDirty) {
            updateColorAreaTexture();
            colorAreaDirty = false;
        }
        if (hueSliderDirty && hueSliderTextureLocation != null) {
            updateHueSliderTexture();
            hueSliderDirty = false;
        }

        RenderSystem.setShaderTexture(0, colorAreaTextureLocation);
        guiGraphics.blit(colorAreaTextureLocation, getX(), getY(), 0, 0, COLOR_AREA_SIZE, COLOR_AREA_SIZE, COLOR_AREA_SIZE, COLOR_AREA_SIZE);

        RenderSystem.setShaderTexture(0, hueSliderTextureLocation);
        guiGraphics.blit(
                hueSliderTextureLocation,
                getX(), // 目标 X
                getY() + COLOR_AREA_SIZE + PADDING, // 目标 Y
                COLOR_AREA_SIZE, // 宽度（正）
                HUE_SLIDER_HEIGHT, // 高度
                COLOR_AREA_SIZE, 0, // 从纹理右边开始读 U，V = 0
                -COLOR_AREA_SIZE, HUE_SLIDER_HEIGHT, // U 宽度取负，V 宽度正常
                COLOR_AREA_SIZE, HUE_SLIDER_HEIGHT // 纹理实际尺寸
        );


        renderSelectionMarkers(guiGraphics);

        // ====== 绘制边框 Start ======
        int borderColor = 0xFFAAAAAA; // 原版按钮颜色

        // 色域块边框
        drawBorder(guiGraphics, getX(), getY(), COLOR_AREA_SIZE, COLOR_AREA_SIZE, borderColor);

        // 色相条边框
        int hueSliderY = getY() + COLOR_AREA_SIZE + PADDING;
        drawBorder(guiGraphics, getX(), hueSliderY, COLOR_AREA_SIZE, HUE_SLIDER_HEIGHT, borderColor);
        // ====== 绘制边框 End ======
    }

    private void drawBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        // 上边
        guiGraphics.hLine(x - 1, x + width, y - 1, color);
        // 下边
        guiGraphics.hLine(x - 1, x + width, y + height, color);
        // 左边
        guiGraphics.vLine(x - 1, y - 1, y + height, color);
        // 右边
        guiGraphics.vLine(x + width, y - 1, y + height, color);
    }

    private void updateColorAreaTexture() {
        NativeImage image = colorAreaTexture.getPixels();
        if (image == null) return;

        for (int y = 0; y < COLOR_AREA_SIZE; y++) {
            for (int x = 0; x < COLOR_AREA_SIZE; x++) {
                float s = (float) x / (COLOR_AREA_SIZE - 1);
                float v = 1.0f - (float) y / (COLOR_AREA_SIZE - 1);

                // 修复边界值
                if (x == COLOR_AREA_SIZE - 1) s = 1.0f;
                if (y == 0) v = 1.0f;
                if (y == COLOR_AREA_SIZE - 1) v = 0.0f;

                int color = hsvToRgb(mirrorHue(hue), s, v);
                image.setPixelRGBA(x, y, 0xFF000000 | color);
            }
        }
        colorAreaTexture.upload();
    }

    private float mirrorHue(float hue) {
        return (360.0f - (hue % 360.0f)) % 360.0f;
    }


    private void updateHueSliderTexture() {
        NativeImage image = hueSliderTexture.getPixels();
        if (image == null) return;

        for (int x = 0; x < COLOR_AREA_SIZE; x++) {
            float h = ((float) x / (COLOR_AREA_SIZE - 1)) * 360.0f;

            int color = hsvToRgb(h % 360.0f, 1.0f, 1.0f);
            for (int y = 0; y < HUE_SLIDER_HEIGHT; y++) {
                image.setPixelRGBA(x, y, 0xFF000000 | color);
            }
        }
        int pureRed = hsvToRgb(0.0f, 1.0f, 1.0f);
        for (int y = 0; y < HUE_SLIDER_HEIGHT; y++) {
            image.setPixelRGBA(COLOR_AREA_SIZE - 1, y, 0xFF000000 | pureRed);
        }

        hueSliderTexture.upload();
    }


    private void renderSelectionMarkers(GuiGraphics guiGraphics) {
        int x = getX();
        int y = getY();

        // 计算色域选择区标记位置
        int colorAreaX = x + Math.round(saturation * (COLOR_AREA_SIZE - 1));
        int colorAreaY = y + Math.round((1 - brightness) * (COLOR_AREA_SIZE - 1));

        // 确保标记在区域内
        colorAreaX = Math.max(x, Math.min(x + COLOR_AREA_SIZE - 1, colorAreaX));
        colorAreaY = Math.max(y, Math.min(y + COLOR_AREA_SIZE - 1, colorAreaY));

        // 绘制十字标记
        int white = 0xFFFFFFFF;
        int black = 0xFF000000;

        // 黑色边框确保在亮色背景上可见
        guiGraphics.hLine(colorAreaX - 6, colorAreaX + 6, colorAreaY, black);
        guiGraphics.vLine(colorAreaX, colorAreaY - 6, colorAreaY + 6, black);

        // 白色十字
        guiGraphics.hLine(colorAreaX - 5, colorAreaX + 5, colorAreaY, white);
        guiGraphics.vLine(colorAreaX, colorAreaY - 5, colorAreaY + 5, white);

        // 计算色相滑块标记位置
        int hueX = x + Math.round((hue / 360.0f) * (COLOR_AREA_SIZE - 1));
        int hueY = y + COLOR_AREA_SIZE + PADDING;

        // 确保标记在滑块上
        hueX = Math.max(x, Math.min(x + COLOR_AREA_SIZE - 1, hueX));

        // 绘制三角形标记（上下都有）
        guiGraphics.fill(hueX - 3, hueY - 3, hueX + 3, hueY, white); // 上三角形
        guiGraphics.fill(hueX - 3, hueY + HUE_SLIDER_HEIGHT, hueX + 3, hueY + HUE_SLIDER_HEIGHT + 3, white); // 下三角形
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible) return false;

        int colorAreaY1 = getY() + COLOR_AREA_SIZE;
        int hueSliderY1 = colorAreaY1 + PADDING + HUE_SLIDER_HEIGHT;

        if (mouseX >= getX() && mouseX < getX() + COLOR_AREA_SIZE && mouseY >= getY() && mouseY < colorAreaY1) {
            draggingColorArea = true;
            updateColorFromMouse(mouseX, mouseY);
            return true;
        }

        if (mouseX >= getX() && mouseX < getX() + COLOR_AREA_SIZE && mouseY >= colorAreaY1 + PADDING && mouseY < hueSliderY1) {
            draggingHueSlider = true;
            updateHueFromMouse(mouseX);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingColorArea) {
            updateColorFromMouse(mouseX, mouseY);
            return true;
        }

        if (draggingHueSlider) {
            updateHueFromMouse(mouseX);
            return true;
        }

        return false;
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingColorArea = false;
        draggingHueSlider = false;
        return true;
    }

    private void updateColorFromMouse(double mouseX, double mouseY) {
        saturation = (float) Math.max(0, Math.min(1, (mouseX - getX() + 0.5f) / COLOR_AREA_SIZE));
        brightness = (float) Math.max(0, Math.min(1, 1 - (mouseY - getY() + 0.5f) / COLOR_AREA_SIZE));
        updateSelectedColor();
    }

    private void updateHueFromMouse(double mouseX) {
        // 计算鼠标在滑块上的相对位置
        double relativeX = (mouseX - getX());
        double position = relativeX / (COLOR_AREA_SIZE - 1);

        // 规范化位置值
        position = Math.max(0, Math.min(1, position));

        // 计算新色相值
        float newHue = (float) (position * 360.0);

        // 修复360度边界问题
        if (position >= 0.99f) {
            newHue = 360.0f;
        }

        if (Math.abs(newHue - hue) > 0.1f) {
            hue = newHue;
            colorAreaDirty = true;
            updateSelectedColor();
        }
    }

    private void updateSelectedColor() {
        selectedColor = shiftHue(hsvToRgb(hue, saturation, brightness), 240.0f);
        if (onColorChanged != null) {
            onColorChanged.accept(selectedColor);
        }
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    // 提供外部颜色导入，自动更新色域和滑块
    public void importColor(int rgbColor) {
        // 提取 RGB
        int red = (rgbColor >> 16) & 0xFF;
        int green = (rgbColor >> 8) & 0xFF;
        int blue = rgbColor & 0xFF;

        // 转换为 HSV
        float[] hsv = rgbToHsv(red, green, blue);

        // 更新内部状态
        this.hue = (hsv[0] + 120.0f) % 360.0f;
        this.saturation = hsv[1];
        this.brightness = hsv[2];

        // 刷新色域纹理
        this.colorAreaDirty = true;
    }

    /**
     * 色相整体偏移
     *
     * @param originalColor 原始颜色（Hex）
     * @param offsetDegrees 偏移角度（0~360），例如 120 会将红色偏移到绿色
     * @return 偏移后的颜色（Hex）
     */
    public static int shiftHue(int originalColor, float offsetDegrees) {
        // 解析RGB
        int r = (originalColor >> 16) & 0xFF;
        int g = (originalColor >> 8) & 0xFF;
        int b = originalColor & 0xFF;

        // RGB 转 HSV
        float[] hsv = rgbToHsv(r, g, b);

        // 偏移 Hue
        hsv[0] = (hsv[0] + offsetDegrees) % 360.0f;

        // HSV 转 RGB
        return hsvToRgb(hsv[0], hsv[1], hsv[2]);
    }


    private static int hsvToRgb(float h, float s, float v) {
        if (s <= 0.0f) {
            int gray = (int) (v * 255);
            return (gray << 16) | (gray << 8) | gray;
        }

        // 规范化色相值
        float hh = (h % 360.0f);
        if (hh < 0) hh += 360.0f;

        hh /= 60.0f;
        int i = (int) hh;
        float ff = hh - i;
        float p = v * (1.0f - s);
        float q = v * (1.0f - (s * ff));
        float t = v * (1.0f - (s * (1.0f - ff)));

        float r, g, b;
        switch (i) {
            case 0:
                r = v;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = v;
                b = p;
                break;
            case 2:
                r = p;
                g = v;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = v;
                break;
            case 4:
                r = t;
                g = p;
                b = v;
                break;
            case 5:
            default:
                r = v;
                g = p;
                b = q;
                break;
        }

        int red = (int) (r * 255);
        int green = (int) (g * 255);
        int blue = (int) (b * 255);

        return (red << 16) | (green << 8) | blue;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
        colorAreaDirty = true;
        hueSliderDirty = true;
    }

    private static float[] rgbToHsv(int r, int g, int b) {
        float red = r / 255.0f;
        float green = g / 255.0f;
        float blue = b / 255.0f;

        float max = Math.max(red, Math.max(green, blue));
        float min = Math.min(red, Math.min(green, blue));
        float delta = max - min;

        float h = 0.0f;
        if (delta != 0) {
            if (max == red) {
                h = 60 * (((green - blue) / delta) % 6);
            } else if (max == green) {
                h = 60 * (((blue - red) / delta) + 2);
            } else {
                h = 60 * (((red - green) / delta) + 4);
            }
        }
        if (h < 0) h += 360.0f;

        float s = (max == 0) ? 0 : delta / max;

        return new float[]{h, s, max};
    }

    public static float rgbToHue(int rgb) {
        // 提取 RGB 分量
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        // 转换为 0 ~ 1 的浮点数
        float rf = r / 255.0f;
        float gf = g / 255.0f;
        float bf = b / 255.0f;

        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;

        float hue;

        if (delta == 0) {
            hue = 0; // 灰色，无色相
        } else if (max == rf) {
            hue = 60 * (((gf - bf) / delta) % 6);
        } else if (max == gf) {
            hue = 60 * (((bf - rf) / delta) + 2);
        } else {
            hue = 60 * (((rf - gf) / delta) + 4);
        }

        if (hue < 0) {
            hue += 360;
        }

        return hue;
    }


}

