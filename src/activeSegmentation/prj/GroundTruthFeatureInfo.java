package activeSegmentation.prj;

public class GroundTruthFeatureInfo {

    private String key;
    private String label;
    private int color;
    private int pixelValue;
    private int classIndex;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getPixelValue() {
        return pixelValue;
    }

    public void setPixelValue(int pixelValue) {
        this.pixelValue = pixelValue;
    }

    public int getClassIndex() {
        return classIndex;
    }

    public void setClassIndex(int classIndex) {
        this.classIndex = classIndex;
    }
}
