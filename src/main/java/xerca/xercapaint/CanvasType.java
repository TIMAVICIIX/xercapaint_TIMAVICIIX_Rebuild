package xerca.xercapaint;


public enum CanvasType {

    //TIMAVICIIX EDIT:Use attr id replace ordinal index!
    ILLEGAL(-1, 0, 0),
    SMALL(0, 16, 16),
    LARGE(1, 32, 32),
    LONG(2, 32, 16),
    TALL(3, 16, 32);


    public final int id;
    private final int width;
    private final int height;

    CanvasType(int id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static CanvasType fromByte(byte x) {
        for (CanvasType type : CanvasType.values()) {
            if (type.id == x) return type;
        }
        return ILLEGAL;
    }

    public static CanvasType fitCanvasType(int width, int height) {
        for (CanvasType type : CanvasType.values()) {
            if (type.height == height && type.width == width) return type;
        }
        return ILLEGAL;
    }
}
