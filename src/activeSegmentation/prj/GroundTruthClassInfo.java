package activeSegmentation.prj;

import java.awt.*;

public class GroundTruthClassInfo {

    private String key;
    private String label;
    private Color color;
    private int pixelValue;
    private int classIndex;

    public GroundTruthClassInfo(String key, String label, Color color, int pixelValue, int classIndex) {
        this.key = key;
        this.label = label;
        this.color = color;
        this.pixelValue = pixelValue;
        this.classIndex = classIndex;
    }

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

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
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
