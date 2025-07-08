package xerca.xercapaint.client.timaviciix_external;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import xerca.xercapaint.PaletteUtil;

import java.util.function.Consumer;

public class HSVSliderWidget extends AbstractWidget {

    private final int sliderWidth = 100;
    private final int sliderHeight = 10;
    private final int sliderSpacing = 20;

    private final PaletteUtil.CustomColor[] colorArray;
    private int currentIndex;

    private float hue = 0f;      // 0 - 360
    private float saturation = 1f; // 0 - 1
    private float value = 1f;      // 0 - 1

    private final SliderWidget hueSlider;
    private final SliderWidget saturationSlider;
    private final SliderWidget valueSlider;

    public final EditBox hueInput;
    public final EditBox saturationInput;
    public final EditBox valueInput;

    private final Consumer<Integer> onColorChanged;

    public HSVSliderWidget(int x, int y, PaletteUtil.CustomColor[] colorArray, int currentIndex, Consumer<Integer> onColorChanged) {
        super(x, y, 200, 100, Component.literal("HSV Slider Group"));
        this.colorArray = colorArray;
        this.onColorChanged = onColorChanged;

        this.currentIndex = currentIndex;

        hueSlider = new SliderWidget(x, y, sliderWidth, sliderHeight, 0, 360, (int) hue, this::onHueChanged);
        saturationSlider = new SliderWidget(x, y + sliderSpacing, sliderWidth, sliderHeight, 0, 100, (int) (saturation * 100), this::onSaturationChanged);
        valueSlider = new SliderWidget(x, y + sliderSpacing * 2, sliderWidth, sliderHeight, 0, 100, (int) (value * 100), this::onValueChanged);

        hueInput = createInputBox(x + sliderWidth + 10, y, (int) hue, this::onHueInput);
        saturationInput = createInputBox(x + sliderWidth + 10, y + sliderSpacing, (int) (saturation * 100), this::onSaturationInput);
        valueInput = createInputBox(x + sliderWidth + 10, y + sliderSpacing * 2, (int) (value * 100), this::onValueInput);

        loadColorFromArray();
    }

    private EditBox createInputBox(int x, int y, int initialValue, Consumer<Integer> onInput) {
        EditBox inputBox = new EditBox(Minecraft.getInstance().font, x, y, 40, 18, Component.literal(""));
        inputBox.setValue(String.valueOf(initialValue));
        inputBox.setMaxLength(4);
        inputBox.setFilter(s -> s.matches("[0-9]*"));
        inputBox.setResponder(s -> {
            try {
                int value = s.isEmpty() ? 0 : Integer.parseInt(s);
                onInput.accept(value);
            } catch (NumberFormatException ignored) {
                inputBox.setValue(String.valueOf(Mth.clamp(
                        s.isEmpty() ? 0 : Integer.parseInt(s.replaceAll("[^0-9]", "")),
                        0, 255)
                ));
            }
        });
        return inputBox;
    }

    public void setColorIndex(int index) {
        if (index < 0 || index >= colorArray.length) return;
        currentIndex = index;
        loadColorFromArray();
    }

    public void hideInputs() {
        hueInput.setEditable(false);
        hueInput.visible = false;

        saturationInput.setEditable(false);
        saturationInput.visible = false;

        valueInput.setEditable(false);
        valueInput.visible = false;
    }

    public void showInputs() {
        hueInput.setEditable(true);
        hueInput.visible = true;

        saturationInput.setEditable(true);
        saturationInput.visible = true;

        valueInput.setEditable(true);
        valueInput.visible = true;
    }

    private void loadColorFromArray() {
        int color = colorArray[currentIndex].getColor().rgbVal();
        float[] hsv = rgbToHsv(color);

        hue = hsv[0];
        saturation = hsv[1];
        value = hsv[2];

        hueSlider.setValue((int) hue);
        saturationSlider.setValue((int) (saturation * 100));
        valueSlider.setValue((int) (value * 100));

        hueInput.setValue(String.valueOf((int) hue));
        saturationInput.setValue(String.valueOf((int) (saturation * 100)));
        valueInput.setValue(String.valueOf((int) (value * 100)));

        notifyColorChanged();
    }

    private void onHueChanged(int value) {
        hue = Mth.clamp(value, 0, 360);
        if (!hueInput.getValue().equals(String.valueOf(value))) {
            hueInput.setValue(String.valueOf(value));
        }
        updateColorArray();
    }

    private void onSaturationChanged(int value) {
        saturation = Mth.clamp(value / 100f, 0f, 1f);
        if (!saturationInput.getValue().equals(String.valueOf(value))) {
            saturationInput.setValue(String.valueOf(value));
        }
        updateColorArray();
    }

    private void onValueChanged(int value) {
        this.value = Mth.clamp(value / 100f, 0f, 1f);
        if (!valueInput.getValue().equals(String.valueOf(value))) {
            valueInput.setValue(String.valueOf(value));
        }
        updateColorArray();
    }

    private void onHueInput(int value) {
        hue = Mth.clamp(value, 0, 360);
        hueSlider.setValue((int) hue);
        updateColorArray();
    }

    private void onSaturationInput(int value) {
        saturation = Mth.clamp(value / 100f, 0f, 1f);
        saturationSlider.setValue(value);
        updateColorArray();
    }

    private void onValueInput(int value) {
        this.value = Mth.clamp(value / 100f, 0f, 1f);
        valueSlider.setValue(value);
        updateColorArray();
    }

    private void updateColorArray() {
        int rgb = hsvToRgb(hue, saturation, value);
        colorArray[currentIndex].setColor(rgb);
        notifyColorChanged();
    }

    private void notifyColorChanged() {
        if (onColorChanged != null) {
            onColorChanged.accept(colorArray[currentIndex].getColor().rgbVal());
        }
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        hueSlider.render(guiGraphics, mouseX, mouseY, partialTicks);
        saturationSlider.render(guiGraphics, mouseX, mouseY, partialTicks);
        valueSlider.render(guiGraphics, mouseX, mouseY, partialTicks);

        hueInput.render(guiGraphics, mouseX, mouseY, partialTicks);
        saturationInput.render(guiGraphics, mouseX, mouseY, partialTicks);
        valueInput.render(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.drawString(Minecraft.getInstance().font, "H:", getX() - 10, getY() + 3, 0xFFFFFF);
        guiGraphics.drawString(Minecraft.getInstance().font, "S:", getX() - 10, getY() + sliderSpacing + 3, 0xFFFFFF);
        guiGraphics.drawString(Minecraft.getInstance().font, "V:", getX() - 10, getY() + sliderSpacing * 2 + 3, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return hueSlider.mouseClicked(mouseX, mouseY, button)
                || saturationSlider.mouseClicked(mouseX, mouseY, button)
                || valueSlider.mouseClicked(mouseX, mouseY, button)
                || hueInput.mouseClicked(mouseX, mouseY, button)
                || saturationInput.mouseClicked(mouseX, mouseY, button)
                || valueInput.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return hueSlider.mouseReleased(mouseX, mouseY, button)
                || saturationSlider.mouseReleased(mouseX, mouseY, button)
                || valueSlider.mouseReleased(mouseX, mouseY, button)
                || hueInput.mouseReleased(mouseX, mouseY, button)
                || saturationInput.mouseReleased(mouseX, mouseY, button)
                || valueInput.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return hueSlider.mouseDragged(mouseX, mouseY, button, dragX, dragY)
                || saturationSlider.mouseDragged(mouseX, mouseY, button, dragX, dragY)
                || valueSlider.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return hueInput.keyPressed(keyCode, scanCode, modifiers)
                || saturationInput.keyPressed(keyCode, scanCode, modifiers)
                || valueInput.keyPressed(keyCode, scanCode, modifiers)
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return hueInput.charTyped(codePoint, modifiers)
                || saturationInput.charTyped(codePoint, modifiers)
                || valueInput.charTyped(codePoint, modifiers)
                || super.charTyped(codePoint, modifiers);
    }

    public void tick() {
        hueInput.tick();
        saturationInput.tick();
        valueInput.tick();
    }

    @Override
    protected void updateWidgetNarration(@NotNull net.minecraft.client.gui.narration.NarrationElementOutput narrationElementOutput) {
    }

    public int getCurrentColor() {
        return colorArray[currentIndex].getColor().rgbVal();
    }

    // ============ HSV 与 RGB 转换工具 ============

    private float[] rgbToHsv(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        float[] hsv = new float[3];
        java.awt.Color.RGBtoHSB(r, g, b, hsv);
        hsv[0] *= 360f; // 颜色范围调整到 0 - 360
        return hsv;
    }

    private int hsvToRgb(float h, float s, float v) {
        int rgb = java.awt.Color.HSBtoRGB(h / 360f, s, v);
        return rgb & 0xFFFFFF;
    }
}

