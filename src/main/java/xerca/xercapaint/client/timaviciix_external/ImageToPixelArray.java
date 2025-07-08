package xerca.xercapaint.client.timaviciix_external;

import xerca.xercapaint.CanvasType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageToPixelArray {

    public static int argbToInt(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int[] convertImageToPixelArray(String imagePath, CanvasType canvasType) {
        /*if (canvasType == null) {
            System.out.println("错误：未知画布类型 \"" + canvasType.name() + "\"，请使用 SMALL, LONG, TALL, LARGE。");
            return null;
        }*/
        try {
            BufferedImage img = ImageIO.read(new File(imagePath));
            int width = img.getWidth();
            int height = img.getHeight();

            if (width != canvasType.getWidth() || height != canvasType.getHeight()) {
                System.out.println("错误：图片尺寸必须为 " + canvasType.getWidth() + "x" + canvasType.getHeight() +
                        "，当前图片尺寸为 " + width + "x" + height);
                return null;
            }

            int[] pixels = new int[width * height];
            int index = 0;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int argb = img.getRGB(x, y);
                    int a = (argb >> 24) & 0xff;
                    int r = (argb >> 16) & 0xff;
                    int g = (argb >> 8) & 0xff;
                    int b = argb & 0xff;

                    pixels[index++] = argbToInt(a, r, g, b);
                }
            }

            return pixels;

        } catch (IOException e) {
            System.out.println("读取图片失败：" + e.getMessage());
            return null;
        }
    }

    /*public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("用法: java ImageToPixelArray <图片路径> <画布类型>");
            System.out.println("示例: java ImageToPixelArray mypic.png LONG");
            return;
        }

        String imagePath = args[0];
        String canvasType = args[1];

        int[] pixels = convertImageToPixelArray(imagePath, canvasType);
        if (pixels != null) {
            System.out.println("像素整形数组：");
            System.out.print("[");
            for (int i = 0; i < pixels.length; i++) {
                System.out.print(pixels[i]);
                if (i != pixels.length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println("]");
        }
    }*/
}
