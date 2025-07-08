package xerca.xercapaint.client.timaviciix_external;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SliderWidget extends AbstractWidget {

    private final int minValue;
    private final int maxValue;
    private int value;

    private boolean dragging = false;

    private final Consumer<Integer> onValueChanged;

    public SliderWidget(int x, int y, int width, int height, int minValue, int maxValue, int initialValue, Consumer<Integer> onValueChanged) {
        super(x, y, width, height, Component.literal("Slider"));
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.value = Mth.clamp(initialValue, minValue, maxValue);
        this.onValueChanged = onValueChanged;
    }

    public void setValue(int newValue) {
        this.value = Mth.clamp(newValue, minValue, maxValue);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // 绘制滑槽
        guiGraphics.fill(getX(), getY() + height / 2 - 1, getX() + width, getY() + height / 2 + 1, 0xFF555555);

        // 计算滑块位置（考虑滑块宽度）
        int knobX = getX() + (int) ((value - minValue) / (float) (maxValue - minValue) * (width - 6));
        knobX = Mth.clamp(knobX, getX(), getX() + width - 6);

        // 绘制滑块
        guiGraphics.fill(knobX, getY(), knobX + 6, getY() + height, 0xFFAAAAAA);

        // 绘制边框
        int borderColor = 0xFFAAAAAA;
        guiGraphics.hLine(getX(), getX() + width - 1, getY(), borderColor);
        guiGraphics.hLine(getX(), getX() + width - 1, getY() + height - 1, borderColor);
        guiGraphics.vLine(getX(), getY(), getY() + height - 1, borderColor);
        guiGraphics.vLine(getX() + width - 1, getY(), getY() + height - 1, borderColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            dragging = true;
            updateValue(mouseX);
            return true;
        }else{
            dragging = false;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!this.isMouseOver(mouseX, mouseY) || dragging) {
            dragging = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging) {
            updateValue(mouseX);
            return true;
        }
        return false;
    }

    private void updateValue(double mouseX) {
        // 计算相对位置（考虑滑块宽度）
        float percent = (float) (mouseX - getX() - 3) / (float) (width - 6);
        percent = Mth.clamp(percent, 0.0f, 1.0f);

        int newValue = Math.round(minValue + percent * (maxValue - minValue));
        newValue = Mth.clamp(newValue, minValue, maxValue);

        if (newValue != value) {
            value = newValue;
            if (onValueChanged != null) {
                onValueChanged.accept(value);
            }
        }
    }


    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return (mouseX > getX() && mouseY > getY()) && (mouseX < getX() + width && mouseY < getY() + height);
    }

    @Override
    protected void updateWidgetNarration(net.minecraft.client.gui.narration.@NotNull NarrationElementOutput narrationElementOutput) {
    }
}
