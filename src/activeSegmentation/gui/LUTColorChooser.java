package activeSegmentation.gui;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import ij.Prefs;
import ij.gui.GenericDialog;
import ij.util.Tools;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.util.Vector;

public class LUTColorChooser implements TextListener, AdjustmentListener {
    Vector colors;
    Vector sliders;
    ColorViewer panel;
    Color initialColor;
    int red;
    int green;
    int blue;
    boolean useHSB;
    String title;
    Frame frame;
    double scale;
    FeaturePanelGroundTruth featurePanelGroundTruth;
    String key;

    public LUTColorChooser(String title, Color initialColor, boolean useHSB, FeaturePanelGroundTruth featurePanelGroundTruth, String key) {
        this(title, initialColor, useHSB, (Frame)null, featurePanelGroundTruth, key);
    }

    public LUTColorChooser(String title, Color initialColor, boolean useHSB, Frame frame, FeaturePanelGroundTruth featurePanelGroundTruth, String key) {
        this.scale = Prefs.getGuiScale();
        this.title = title;
        if (initialColor == null) {
            initialColor = Color.black;
        }

        this.initialColor = initialColor;
        this.red = initialColor.getRed();
        this.green = initialColor.getGreen();
        this.blue = initialColor.getBlue();
        this.useHSB = useHSB;
        this.frame = frame;
        this.featurePanelGroundTruth = featurePanelGroundTruth;
        this.key = key;
    }

    public Color getColor() {
        GenericDialog gd = this.frame != null ? new GenericDialog(this.title, this.frame) : new GenericDialog(this.title);
        gd.addSlider("Red:", 0.0D, 255.0D, (double)this.red);
        gd.addSlider("Green:", 0.0D, 255.0D, (double)this.green);
        gd.addSlider("Blue:", 0.0D, 255.0D, (double)this.blue);
        gd.addStringField("Class label:",featurePanelGroundTruth.getGroundInfoClassList().get(key).getLabel());
        this.panel = new ColorViewer(this.initialColor, this.scale);
        gd.addPanel(this.panel, 10, new Insets(10, 0, 0, 0));
        this.colors = gd.getNumericFields();

        int red;
        for(red = 0; red < this.colors.size(); ++red) {
            ((TextField)this.colors.elementAt(red)).addTextListener(this);
        }

        this.sliders = gd.getSliders();

        for(red = 0; red < this.sliders.size(); ++red) {
            ((Scrollbar)this.sliders.elementAt(red)).addAdjustmentListener(this);
        }

        gd.showDialog();
        if (gd.wasCanceled()) {
            return null;
        } else {
            red = (int)gd.getNextNumber();
            int green = (int)gd.getNextNumber();
            int blue = (int)gd.getNextNumber();
            featurePanelGroundTruth.getGroundInfoClassList().get(key).setLabel(gd.getNextString());
            featurePanelGroundTruth.saveGroundTruthFeatureMetadata();
            return new Color(red, green, blue);
        }
    }

    public void textValueChanged(TextEvent e) {
        int red = (int)Tools.parseDouble(((TextField)this.colors.elementAt(0)).getText());
        int green = (int)Tools.parseDouble(((TextField)this.colors.elementAt(1)).getText());
        int blue = (int)Tools.parseDouble(((TextField)this.colors.elementAt(2)).getText());
        if (red < 0) {
            red = 0;
        }

        if (red > 255) {
            red = 255;
        }

        if (green < 0) {
            green = 0;
        }

        if (green > 255) {
            green = 255;
        }

        if (blue < 0) {
            blue = 0;
        }

        if (blue > 255) {
            blue = 255;
        }

        this.panel.setColor(new Color(red, green, blue));
        this.panel.repaint();
    }

    public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
        Object source = e.getSource();

        for(int i = 0; i < this.sliders.size(); ++i) {
            if (source == this.sliders.elementAt(i)) {
                Scrollbar sb = (Scrollbar)source;
                TextField var5 = (TextField)this.colors.elementAt(i);
            }
        }

    }
}
