package activeSegmentation.gui;

import ij.ImagePlus;

import java.awt.*;
import java.util.Vector;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.*;
import java.awt.*;
import java.awt.image.*;
import ij.util.*;
import ij.measure.*;
import java.util.Vector;
import java.awt.event.*;


public class LUTLabelUI implements PlugIn, ActionListener {

    Vector colors;
    private ImagePlus imp;
    Button openButton, saveButton, resizeButton, invertButton;
    ColorPanel colorPanel;
    FeaturePanelGroundTruth featurePanelGroundTruth;
    String key;


    public LUTLabelUI(FeaturePanelGroundTruth featurePanelGroundTruth, String key) {
        this.featurePanelGroundTruth = featurePanelGroundTruth;
        this.key = key;
    }

    public void run(String args) {
        ImagePlus imp = featurePanelGroundTruth.getOverlayImage();
        if (imp==null) {
            IJ.showMessage("LUT Editor", "No images are open");
            return;
        }
        if (imp.getBitDepth()==24) {
            IJ.showMessage("LUT Editor", "RGB images do not use LUTs");
            return;
        }
        colorPanel = new ColorPanel(imp, this.featurePanelGroundTruth, key);
        if (colorPanel.getMapSize()!=256) {
            IJ.showMessage("LUT Editor", "LUT must have 256 entries");
            return;
        }
        int red=0, green=0, blue=0;
        GenericDialog gd = new GenericDialog("LUT Editor");
        Panel buttonPanel = new Panel(new GridLayout(4, 1, 0, 5));
        openButton = new Button("Open...");
        openButton.addActionListener(this);
        buttonPanel.add(openButton);
        saveButton = new Button("Save...");
        saveButton.addActionListener(this);
        buttonPanel.add(saveButton);
        resizeButton = new Button("Set...");
        resizeButton.addActionListener(this);
        buttonPanel.add(resizeButton);
        invertButton = new Button("Invert...");
        invertButton.addActionListener(this);
        buttonPanel.add(invertButton);
        Panel panel = new Panel();
        panel.add(colorPanel);
        panel.add(buttonPanel);
        gd.addPanel(panel, GridBagConstraints.CENTER, new Insets(10, 0, 0, 0));
        gd.showDialog();
        if (gd.wasCanceled()){
            colorPanel.cancelLUT();
            return;
        } else{
            colorPanel.applyLUT();
        }
        featurePanelGroundTruth.saveGroundTruthFeatureMetadata();
    }

    void save() {
        IJ.run("LUT...");
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source==openButton)
            colorPanel.open();
        else if (source==saveButton)
            save();
        else if (source==resizeButton)
            colorPanel.resize();
        else if (source==invertButton)
            colorPanel.invert();
    }
}
