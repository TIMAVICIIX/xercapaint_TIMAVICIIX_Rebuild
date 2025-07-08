package xerca.xercapaint.client.timaviciix_external;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import xerca.xercapaint.PaletteUtil;

import java.util.List;
import java.util.function.Consumer;

public class RGBSliderWidget extends AbstractWidget {

    private final int sliderWidth = 100;
    private final int sliderHeight = 10;
    private final int sliderSpacing = 20;

    private final PaletteUtil.CustomColor[] colorArray; // 12色数组
    private int currentIndex;

    private int red = 255, green = 255, blue = 255;

    private final SliderWidget redSlider;
    private final SliderWidget greenSlider;
    private final SliderWidget blueSlider;

    public final EditBox redInput;
    public final EditBox greenInput;
    public final EditBox blueInput;

    private final Consumer<Integer> onColorChanged;

    public RGBSliderWidget(int x, int y, PaletteUtil.CustomColor[] colorArray, int currentIndex, Consumer<Integer> onColorChanged) {
        super(x, y, 200, 100, Component.literal("RGB Slider Group"));
        this.colorArray = colorArray;
        this.onColorChanged = onColorChanged;

        this.currentIndex = currentIndex;
        redSlider = new SliderWidget(x, y, sliderWidth, sliderHeight, 0, 255, red, this::onRedChanged);
        greenSlider = new SliderWidget(x, y + sliderSpacing, sliderWidth, sliderHeight, 0, 255, green, this::onGreenChanged);
        blueSlider = new SliderWidget(x, y + sliderSpacing * 2, sliderWidth, sliderHeight, 0, 255, blue, this::onBlueChanged);

        redInput = createInputBox(x + sliderWidth + 10, y, red, this::onRedInput);
        greenInput = createInputBox(x + sliderWidth + 10, y + sliderSpacing, green, this::onGreenInput);
        blueInput = createInputBox(x + sliderWidth + 10, y + sliderSpacing * 2, blue, this::onBlueInput);

        loadColorFromArray();
    }

    private EditBox createInputBox(int x, int y, int initialValue, Consumer<Integer> onInput) {
        EditBox inputBox = new EditBox(Minecraft.getInstance().font, x, y, 40, 18, Component.literal(""));
        inputBox.setValue(String.valueOf(initialValue));
        inputBox.setMaxLength(3);
        inputBox.setFilter(s -> s.matches("[0-9]*"));
        inputBox.setResponder(s -> {
            try {
                int value = s.isEmpty() ? 0 : Integer.parseInt(s);
                value = Mth.clamp(value, 0, 255);
                onInput.accept(value);
            } catch (NumberFormatException ignored) {
                // 恢复有效值
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

    public void hideInputs(){
        redInput.setEditable(false);
        redInput.visible = false;

        greenInput.setEditable(false);
        greenInput.visible = false;

        blueInput.setEditable(false);
        blueInput.visible = false;
    }

    public void showInputs(){
        redInput.setEditable(true);
        redInput.visible = true;

        greenInput.setEditable(true);
        greenInput.visible = true;

        blueInput.setEditable(true);
        blueInput.visible = true;
    }

    private void loadColorFromArray() {
        int color = colorArray[currentIndex].getColor().rgbVal();
        red = (color >> 16) & 0xFF;
        green = (color >> 8) & 0xFF;
        blue = color & 0xFF;

        redSlider.setValue(red);
        greenSlider.setValue(green);
        blueSlider.setValue(blue);

        redInput.setValue(String.valueOf(red));
        greenInput.setValue(String.valueOf(green));
        blueInput.setValue(String.valueOf(blue));

        notifyColorChanged();
    }

    private void onRedChanged(int value) {
        red = value;
        // 仅在值变化时更新输入框，避免循环更新
        if (!redInput.getValue().equals(String.valueOf(value))) {
            redInput.setValue(String.valueOf(value));
        }
        updateColorArray();
    }

    private void onGreenChanged(int value) {
        green = value;
        if (!greenInput.getValue().equals(String.valueOf(value))) {
            greenInput.setValue(String.valueOf(value));
        }
        updateColorArray();
    }

    private void onBlueChanged(int value) {
        blue = value;
        if (!blueInput.getValue().equals(String.valueOf(value))) {
            blueInput.setValue(String.valueOf(value));
        }
        updateColorArray();
    }

    private void onRedInput(int value) {
        red = value;
        redSlider.setValue(value);
        updateColorArray();
    }

    private void onGreenInput(int value) {
        green = value;
        greenSlider.setValue(value);
        updateColorArray();
    }

    private void onBlueInput(int value) {
        blue = value;
        blueSlider.setValue(value);
        updateColorArray();
    }

    private void updateColorArray() {
        colorArray[currentIndex].setColor((red << 16) | (green << 8) | blue);
        notifyColorChanged();
    }

    private void notifyColorChanged() {
        if (onColorChanged != null) {
            onColorChanged.accept(colorArray[currentIndex].getColor().rgbVal());
        }
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // 渲染滑块
        redSlider.render(guiGraphics, mouseX, mouseY, partialTicks);
        greenSlider.render(guiGraphics, mouseX, mouseY, partialTicks);
        blueSlider.render(guiGraphics, mouseX, mouseY, partialTicks);

        // 渲染输入框
        redInput.render(guiGraphics, mouseX, mouseY, partialTicks);
        greenInput.render(guiGraphics, mouseX, mouseY, partialTicks);
        blueInput.render(guiGraphics, mouseX, mouseY, partialTicks);

        // 渲染标签
        guiGraphics.drawString(Minecraft.getInstance().font, "R:", getX() - 10, getY() + 3, 0xFFFFFF);
        guiGraphics.drawString(Minecraft.getInstance().font, "G:", getX() - 10, getY() + sliderSpacing + 3, 0xFFFFFF);
        guiGraphics.drawString(Minecraft.getInstance().font, "B:", getX() - 10, getY() + sliderSpacing * 2 + 3, 0xFFFFFF);
    }

    private void drawBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.hLine(x, x + width - 1, y, color);
        guiGraphics.hLine(x, x + width - 1, y + height - 1, color);
        guiGraphics.vLine(x, y, y + height - 1, color);
        guiGraphics.vLine(x + width - 1, y, y + height - 1, color);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return redSlider.mouseClicked(mouseX, mouseY, button)
                || greenSlider.mouseClicked(mouseX, mouseY, button)
                || blueSlider.mouseClicked(mouseX, mouseY, button)
                || redInput.mouseClicked(mouseX, mouseY, button)
                || greenInput.mouseClicked(mouseX, mouseY, button)
                || blueInput.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return redSlider.mouseReleased(mouseX, mouseY, button)
                || greenSlider.mouseReleased(mouseX, mouseY, button)
                || blueSlider.mouseReleased(mouseX, mouseY, button)
                || redInput.mouseReleased(mouseX, mouseY, button)
                || greenInput.mouseReleased(mouseX, mouseY, button)
                || blueInput.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return redSlider.mouseDragged(mouseX, mouseY, button, dragX, dragY)
                || greenSlider.mouseDragged(mouseX, mouseY, button, dragX, dragY)
                || blueSlider.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return redInput.keyPressed(keyCode, scanCode, modifiers)
                || greenInput.keyPressed(keyCode, scanCode, modifiers)
                || blueInput.keyPressed(keyCode, scanCode, modifiers)
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return redInput.charTyped(codePoint, modifiers)
                || greenInput.charTyped(codePoint, modifiers)
                || blueInput.charTyped(codePoint, modifiers)
                || super.charTyped(codePoint, modifiers);
    }

    // 添加组件tick方法
    public void tick() {
        redInput.tick();
        greenInput.tick();
        blueInput.tick();
    }

    @Override
    protected void updateWidgetNarration(@NotNull net.minecraft.client.gui.narration.NarrationElementOutput narrationElementOutput) {
    }

    // 实时暴露方法
    public int getCurrentColor() {
        return colorArray[currentIndex].getColor().rgbVal();
    }
}
