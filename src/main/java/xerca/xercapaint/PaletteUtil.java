package xerca.xercapaint;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class PaletteUtil {
    final public static Color emptinessColor = new Color(255, 236, 229);

    public static class Color {
        public static final Color WHITE = new Color(0xFFFFFFFF);

        public int r, g, b;

        public Color(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public Color(int rgb) {
            this.r = (rgb >> 16) & 0xFF;
            this.g = (rgb >> 8) & 0xFF;
            this.b = rgb & 0xFF;
        }

        public int rgbVal() {
            int val = r;
            val = (val << 8) + g;
            val = (val << 8) + b;
            val += 0xFF000000;
            return val;
        }

        public void setGLColor(){
            RenderSystem.setShaderColor(((float)r)/255.f, ((float)g)/255.f, ((float)b)/255.f, 1.0f);
        }

        static public Color mix(Color a, Color b, float ratio){
            if(ratio == 1.f) {
                return a;
            }
            else if(ratio == 0.f){
                return b;
            }
            Color res = new Color(
                    (int)(a.r*ratio) + (int)(b.r*(1-ratio)),
                    (int)(a.g*ratio) + (int)(b.g*(1-ratio)),
                    (int)(a.b*ratio) + (int)(b.b*(1-ratio))
            );
            int averageMaximum = (int)(Math.max(Math.max(a.r, a.g), a.b)*ratio) + (int)(Math.max(Math.max(b.r, b.g), b.b)*(1-ratio));

            int maximumOfAverage = Math.max(Math.max(res.r, res.g), res.b);
            int gainFactor = averageMaximum / maximumOfAverage;

            res.r *= gainFactor;
            res.g *= gainFactor;
            res.b *= gainFactor;
            return res;
        }
    }
    public static class CustomColor {
        private int totalRed = 0;
        private int totalGreen = 0;
        private int totalBlue = 0;
        private int totalMaximum = 0;

        private int numberOfColors = 0;

        private Color result;

        public CustomColor() {
            calculateResult();
        }

        public CustomColor(FriendlyByteBuf buf) {
            readFromBuffer(buf);
        }

        public CustomColor(int totalRed, int totalGreen, int totalBlue, int totalMaximum, int numberOfColors) {
            this.totalRed = totalRed;
            this.totalGreen = totalGreen;
            this.totalBlue = totalBlue;
            this.totalMaximum = totalMaximum;
            this.numberOfColors = numberOfColors;
            calculateResult();
        }

        public void setColor(int hexColor){
            this.totalRed = (hexColor >> 16) & 0xFF;
            this.totalGreen = (hexColor >> 8) & 0xFF;
            this.totalBlue = hexColor & 0xFF;
            this.totalMaximum = Math.max(Math.max(totalRed, totalGreen), totalBlue);
            this.numberOfColors = 1;
            calculateResult();
        }


        public void calculateResult() {
            if (numberOfColors == 0) {
                this.result = emptinessColor; // 为空时默认色
                return;
            }

            int averageRed = totalRed / numberOfColors;
            int averageGreen = totalGreen / numberOfColors;
            int averageBlue = totalBlue / numberOfColors;
            int averageMaximum = totalMaximum / numberOfColors;

            int maximumOfAverage = Math.max(Math.max(averageRed, averageGreen), averageBlue);

            // 防止除以零
            if (maximumOfAverage == 0) {
                this.result = new Color(0, 0, 0); // 或者用你自定义的默认颜色
                return;
            }

            int gainFactor = averageMaximum / maximumOfAverage;

            int resultRed = averageRed * gainFactor;
            int resultGreen = averageGreen * gainFactor;
            int resultBlue = averageBlue * gainFactor;

            this.result = new Color(resultRed, resultGreen, resultBlue);
        }

        public void mix(Color toBeMixed){
            totalRed += toBeMixed.r;
            totalGreen += toBeMixed.g;
            totalBlue += toBeMixed.b;
            totalMaximum += Math.max(Math.max(toBeMixed.r, toBeMixed.g), toBeMixed.b);
            numberOfColors += 1;
            calculateResult();
        }

        public void reset(){
            totalRed = 0;
            totalGreen = 0;
            totalBlue = 0;
            totalMaximum = 0;
            numberOfColors = 0;
            calculateResult();
        }

        public Color getColor() {
            return result;
        }

        public int getNumberOfColors() {
            return numberOfColors;
        }
        public void setNumberOfColors(int number){
            this.numberOfColors = number;
        }

        public void writeToBuffer(FriendlyByteBuf buf){
            buf.writeInt(totalRed);
            buf.writeInt(totalGreen);
            buf.writeInt(totalBlue);
            buf.writeInt(totalMaximum);
            buf.writeInt(numberOfColors);
        }

        public void readFromBuffer(FriendlyByteBuf buf){
            totalRed = buf.readInt();
            totalGreen = buf.readInt();
            totalBlue = buf.readInt();
            totalMaximum = buf.readInt();
            numberOfColors = buf.readInt();
        }
    }

    public static void writeCustomColorArrayToNBT(CompoundTag tag, CustomColor[] customColors){
        int[] totalReds = new int[12];
        int[] totalGreens = new int[12];
        int[] totalBlues = new int[12];
        int[] totalMaximums = new int[12];
        int[] numbersOfColors = new int[12];

        for(int i=0; i<customColors.length; i++){
            totalReds[i] = customColors[i].totalRed;
            totalGreens[i] = customColors[i].totalGreen;
            totalBlues[i] = customColors[i].totalBlue;
            totalMaximums[i] = customColors[i].totalMaximum;
            numbersOfColors[i] = customColors[i].numberOfColors;
        }
        tag.putIntArray("r", totalReds);
        tag.putIntArray("g", totalGreens);
        tag.putIntArray("b", totalBlues);
        tag.putIntArray("m", totalMaximums);
        tag.putIntArray("n", numbersOfColors);
    }

    public static void readCustomColorArrayFromNBT(CompoundTag tag, CustomColor[] customColors){
        int[] totalReds = tag.getIntArray("r");
        int[] totalGreens = tag.getIntArray("g");
        int[] totalBlues = tag.getIntArray("b");
        int[] totalMaximums = tag.getIntArray("m");
        int[] numbersOfColors = tag.getIntArray("n");

        for(int i=0; i<customColors.length; i++){
            customColors[i] = new CustomColor(totalReds[i], totalGreens[i], totalBlues[i], totalMaximums[i], numbersOfColors[i]);
        }
    }
}
