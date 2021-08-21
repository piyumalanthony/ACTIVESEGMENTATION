package activeSegmentation.gui;


import activeSegmentation.feature.GroundTruthManager;
import activeSegmentation.prj.*;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.*;
import ij.plugin.LUT_Editor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.LUT;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import activeSegmentation.ASCommon;

import activeSegmentation.LearningType;
import activeSegmentation.ProjectType;
import activeSegmentation.feature.FeatureManager;
import activeSegmentation.util.GuiUtil;
import org.w3c.dom.ls.LSInput;

import static  activeSegmentation.ProjectType.*;

public class FeaturePanelGroundTruth extends ImageWindow implements ASCommon {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private FeatureManager featureManager;
    private GroundTruthManager groundTruthManager;
    private FeatureManager testFeatureManager;
    ProjectManager projectManager;
    private boolean isShowColorOverlay = false;
    private Random rand = new Random();
    private Map<String, GroundTruthClassInfo> groundInfoClassList = new HashMap<>();
    private Map<Integer, GroundTruthClassInfo> groundTruthClassInfoIndexedMap = new HashMap<>();
    private Map<Integer, GroundTruthClassInfo> groundTruthClassInfoPixelIndexedMap = new HashMap<>();
    private List<String> groundTruthImages = new ArrayList<>();
    private List<String> testingImages = new ArrayList<>();
    private ImagePlus overlayImage;
    private Map<String, String> imageToGroundTruthMap = new HashMap<>();
    private int customNumOfTrainingInstances = 0;
//    private LUT_Editor lut_editor;
    /**
     * opacity (in %) of the result overlay image
     */
    int overlayOpacity = 33;
    /**
     * alpha composite for the result overlay image
     */
    Composite overlayAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overlayOpacity / 100f);
    private ImageOverlay resultOverlay;
    LUT overlayLUT;
    /**
     * flag to display the overlay image
     */
    private boolean showColorOverlay = false;
    ImagePlus classifiedImage;
    // Create overlay LUT
    byte[] red = new byte[256];
    byte[] green = new byte[256];
    byte[] blue = new byte[256];

    private Map<String, JList<String>> exampleList;
    private Map<String, JList<String>> allexampleList;

    /**
     * array of ROI list overlays to paint the transparent ROIs of each class
     */
    private Map<String, RoiListOverlay> roiOverlayList;

    /**
     * Used only during classification setting
     */
    private Map<String, Integer> predictionResultClassification;

    final Composite transparency050 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f);

    /*
     *  the files must be in the resources/feature folder
     */
    private static final Icon uploadIcon = new ImageIcon(FeaturePanelNew.class.getResource("upload.png"));
    private static final Icon downloadIcon = new ImageIcon(FeaturePanelNew.class.getResource("download.png"));


    /**
     * This {@link ActionEvent} is fired when the 'next' button is pressed.
     */
    private ActionEvent NEXT_BUTTON_PRESSED = new ActionEvent(this, 0, "Next");
    /**
     * This {@link ActionEvent} is fired when the 'previous' button is pressed.
     */
    private ActionEvent PREVIOUS_BUTTON_PRESSED = new ActionEvent(this, 1, "Previous");
    //    private ActionEvent ADDCLASS_BUTTON_PRESSED = new ActionEvent( this, 2, "AddClass" );
    private ActionEvent SAVECLASS_BUTTON_PRESSED = new ActionEvent(this, 3, "SaveLabel");
    //    private ActionEvent DELETE_BUTTON_PRESSED = new ActionEvent( this, 4, "DeleteClass" );
    private ActionEvent COMPUTE_BUTTON_PRESSED = new ActionEvent(this, 5, "TRAIN");
    private ActionEvent SAVE_BUTTON_PRESSED = new ActionEvent(this, 6, "SAVEDATA");
    private ActionEvent TOGGLE_BUTTON_PRESSED = new ActionEvent(this, 7, "TOGGLE");
    //    private ActionEvent DOWNLOAD_BUTTON_PRESSED = new ActionEvent( this, 8, "DOWNLOAD" );
    private ActionEvent MASKS_BUTTON_PRESSED = new ActionEvent(this, 8, "MASKS");
    private ActionEvent LOAD_GROUND_TRUTH_BUTTON_PRESSED = new ActionEvent(this, 9, "LOAD_GROUND_TRUTH");
    private ActionEvent LOAD_TEST_DATA_BUTTON_PRESSED = new ActionEvent(this, 10, "LOAD_TEST_DATA");
    private ActionEvent LUT_BUTTON_PRESSED = new ActionEvent(this, 11, "LUT");
    private ActionEvent OVERALY_BUTTON_PRESSED = new ActionEvent(this, 12, "OVERLAY");
    private ActionEvent TRAINING_INSTANCE_BUTTON_PRESSED = new ActionEvent(this, 12, "TRAINING_INSTANCE");


    private ImagePlus displayImage;
    /**
     * Used only in classification setting, in segmentation we get from feature manager
     */
    //private ImagePlus tempClassifiedImage;
    private JPanel imagePanel, classPanel, roiPanel;
    private JTextField imageNum;
    private JTextField numOfTrainingInstances;
    private JLabel total,totalForTrainingInstances;
    private List<JCheckBox> jCheckBoxList;
    private Map<String, JTextArea> jTextList;
    private JComboBox<LearningType> learningType;
    private JFrame frame;

    /*
     * constructor
     */
    public FeaturePanelGroundTruth(FeatureManager featureManager, ProjectManager projectManager, GroundTruthManager groundTruthManager) {
        super(featureManager.getCurrentImage());
        this.featureManager = featureManager;
        this.projectManager = projectManager;
        this.groundTruthManager = groundTruthManager;
        this.displayImage = featureManager.getCurrentImage();
        this.jCheckBoxList = new ArrayList<JCheckBox>();
        this.jTextList = new HashMap<String, JTextArea>();
        this.exampleList = new HashMap<String, JList<String>>();
        this.allexampleList = new HashMap<String, JList<String>>();
        roiOverlayList = new HashMap<String, RoiListOverlay>();
        //tempClassifiedImage = new ImagePlus();
        this.setVisible(false);
        showPanel();
        setGroundTruthFeatureMetadata();
        addGroundTruthClassPanel(groundInfoClassList);
        validateFrame();
    }


    public void showPanel() {
        frame = new JFrame("Ground Truth Loader");

        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JList<String> frameList = GuiUtil.model();
        frameList.setForeground(Color.BLACK);


        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setFont(panelFONT);
        panel.setBackground(Color.GRAY);

        imagePanel = new JPanel();
        roiPanel = new JPanel();
        classPanel = new JPanel();

        /*
         * image panel
         */
        imagePanel.setLayout(new BorderLayout());

        ic = new SimpleCanvas(featureManager.getCurrentImage());
        ic.setMinimumSize(new Dimension(IMAGE_CANVAS_DIMENSION, IMAGE_CANVAS_DIMENSION));
        loadImage(displayImage);
        setOverlay();
        imagePanel.setBackground(Color.GRAY);
        imagePanel.add(ic, BorderLayout.CENTER);
        imagePanel.setBounds(10, 10, IMAGE_CANVAS_DIMENSION, IMAGE_CANVAS_DIMENSION);
        panel.add(imagePanel);

        /*
         * class panel
         */

//        classPanel.setBounds(605,20,350,100);
//        classPanel.setPreferredSize(new Dimension(350, 100));
//        classPanel.setBorder(BorderFactory.createTitledBorder("Classes"));
//
//        JScrollPane classScrolPanel = new JScrollPane(classPanel);
//        classScrolPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//        classScrolPanel.setBounds(605,20,350,80);
//        addClassPanel();
//        panel.add(classScrolPanel);


        /*
         * features
         */
        JPanel features = new JPanel();
        features.setBounds(605, 20, 350, 135);
        features.setBorder(BorderFactory.createTitledBorder("Learning"));

        addButton(new JButton(), "PREVIOUS", null, 610, 30, 200, 20, features, PREVIOUS_BUTTON_PRESSED, null);

        imageNum = new JTextField();
        imageNum.setColumns(5);
        imageNum.setBounds(630, 30, 10, 20);
        JLabel dasedLine = new JLabel("/");
        dasedLine.setFont(new Font("Arial", Font.PLAIN, 15));
        dasedLine.setForeground(Color.BLACK);
        dasedLine.setBounds(670, 30, 10, 20);
        total = new JLabel("Total");
        total.setFont(new Font("Arial", Font.PLAIN, 15));
        total.setForeground(Color.BLACK);
        total.setBounds(690, 30, 80, 30);
        imageNum.setText(Integer.toString(featureManager.getCurrentSlice()));
        total.setText(Integer.toString(featureManager.getTotalSlice()));
        features.add(imageNum);
        features.add(dasedLine);
        features.add(total);

        addButton(new JButton(), "Next", null, 800, 30, 80, 20, features, NEXT_BUTTON_PRESSED, null);

        JPanel trainingInstancePanel = new JPanel();

        JLabel  trainingInstancePanelLabel = new JLabel("Set number of training instances:");
        trainingInstancePanelLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        trainingInstancePanelLabel.setForeground(Color.BLACK);
        trainingInstancePanelLabel.setBounds(590, 80, 200, 20);
        numOfTrainingInstances = new JTextField();
        numOfTrainingInstances.setColumns(5);
        numOfTrainingInstances.setBounds(630, 80, 10, 20);
        JLabel dasedLineForTrainingInstances = new JLabel("/");
        dasedLineForTrainingInstances.setFont(new Font("Arial", Font.PLAIN, 15));
        dasedLineForTrainingInstances.setForeground(Color.BLACK);
        dasedLineForTrainingInstances.setBounds(670, 80, 10, 20);
        totalForTrainingInstances = new JLabel("Total");
        totalForTrainingInstances.setFont(new Font("Arial", Font.PLAIN, 15));
        totalForTrainingInstances.setForeground(Color.BLACK);
        totalForTrainingInstances.setBounds(690, 80, 80, 30);
        numOfTrainingInstances.setText(String.valueOf(projectManager.getMetaInfo().getNumOfTrainingInstances()));
        totalForTrainingInstances.setText(Integer.toString(featureManager.getTotalSlice()));
        trainingInstancePanel.add(trainingInstancePanelLabel);
        trainingInstancePanel.add(numOfTrainingInstances);
        trainingInstancePanel.add(dasedLineForTrainingInstances);
        trainingInstancePanel.add(totalForTrainingInstances);
        addButton(new JButton(), "Save", null, 800, 80, 80, 20, trainingInstancePanel, TRAINING_INSTANCE_BUTTON_PRESSED, null);
        features.add(trainingInstancePanel);
//        features.add(numOfTrainingInstances);
//        features.add(dasedLineForTrainingInstances);
//        features.add(totalForTrainingInstances);
        /*
         * compute panel
         */

        JPanel computePanel = new JPanel();

        addButton(new JButton(), "Train", null, 550, 100, 350, 100, computePanel, COMPUTE_BUTTON_PRESSED, null);
        addButton(new JButton(), "Save", null, 550, 100, 350, 100, computePanel, SAVE_BUTTON_PRESSED, null);
        addButton(new JButton(), "Overlay", null, 550, 100, 350, 100, computePanel, TOGGLE_BUTTON_PRESSED, null);
        addButton(new JButton(), "Masks", null, 550, 100, 350, 100, computePanel, MASKS_BUTTON_PRESSED, null);

        features.add(computePanel);
        frame.add(features);



        /*
         *  Data panel
         */

        JPanel dataJPanel = new JPanel();
        learningType = new JComboBox<LearningType>(LearningType.values());
        learningType.setVisible(true);
        learningType.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (featureManager.getProjectType() == ProjectType.CLASSIF) {
                    if (showColorOverlay) {
                        updateGui();
                        updateResultOverlay(null);
                    } else
                        updateGui();
                } else
                    updateGui();


                // here we need to add for classification
            }
        });

        dataJPanel.setBounds(720, 160, 100, 40);
        learningType.setSelectedIndex(0);
        learningType.setFont(panelFONT);
        learningType.setBackground(Color.GRAY);
        learningType.setForeground(Color.WHITE);
        dataJPanel.add(learningType);
        dataJPanel.setBackground(Color.GRAY);

        panel.add(dataJPanel);


        /*
        Ground Truth panel
         */
        JPanel importLabels = new JPanel();
        importLabels.setBounds(605, 200, 350, 80);
        importLabels.setBorder(BorderFactory.createTitledBorder("Import Ground truth"));
        JFileChooser fc = new JFileChooser();
        fc.setLayout(new BorderLayout());
        fc.setSize(800, 500);


        addButton(new JButton(), "Load Ground Truth", null, 800, 130, 40, 20, importLabels, LOAD_GROUND_TRUTH_BUTTON_PRESSED, null);
        addButton(new JButton(), "Load Test Data", null, 800, 130, 40, 20, importLabels, LOAD_TEST_DATA_BUTTON_PRESSED, null);
        addButton(new JButton(), "Show Overaly", null, 800, 130, 40, 20, importLabels, OVERALY_BUTTON_PRESSED, null);

        fc.setVisible(true);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);


        File labels = new File("label");
        if (!labels.exists()) {
            labels.mkdirs();
        }
        panel.add(importLabels);


        /*
         * ROI panel
         */
        roiPanel.setBorder(BorderFactory.createTitledBorder("class labels"));
        //roiPanel.setPreferredSize(new Dimension(350, 400));
        JScrollPane scrollPane = new JScrollPane(roiPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBounds(605, 290, 350, 250);
        panel.add(scrollPane);


        frame.add(panel);



        /*
         *  frame code
         */
        frame.pack();
        frame.setSize(largeframeWidth, largeframeHight);
        //frame.setSize(getMaximumSize());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        updateGui();

    }

    private void addClassPanel() {
//        classPanel.removeAll();
        roiPanel.removeAll();
//        jCheckBoxList.clear();
//        jTextList.clear();
        int classes = groundTruthClassInfoIndexedMap.size();
//        IJ.log(Integer.toString(classes));
//        if(classes%3==0){
//            int tempSize=classes/3;
//            classPanel.setPreferredSize(new Dimension(340, 80+30*tempSize));
//        }
        roiPanel.setPreferredSize(new Dimension(350, 175 * classes));
//        addButton(new JButton(), "ADD CLASS",null , 630, 20, 130, 20,classPanel,ADDCLASS_BUTTON_PRESSED,null );
//        addButton(new JButton(), "SAVE CLASS",null , 630, 20, 130, 20,classPanel,SAVECLASS_BUTTON_PRESSED,null );
//        addButton(new JButton(), "DELETE CLASS",null , 630, 20, 130, 20,classPanel,DELETE_BUTTON_PRESSED,null );
        for (String key : groundInfoClassList.keySet()) {
            String label = groundInfoClassList.get(key).getLabel();
            Color color = groundInfoClassList.get(key).getColor();
//            addClasses(key,label,color);
            addSidePanel(color, key, label);
        }
    }

    /**
     * Draw the painted traces on the display image
     */

    private void addSidePanel(Color color, String key, String label) {
        JPanel panel = new JPanel();
        JList<String> current = GuiUtil.model();

        current.setForeground(color);
        exampleList.put(key, current);
        JList<String> all = GuiUtil.model();
        all.setForeground(color);
        allexampleList.put(key, all);
        RoiListOverlay roiOverlay = new RoiListOverlay();
        roiOverlay.setComposite(transparency050);
        ((OverlayedImageCanvas) ic).addOverlay(roiOverlay);
        roiOverlayList.put(key, roiOverlay);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setName(key);
//
        ActionEvent lutAction = new ActionEvent(buttonPanel, 2, "OpenLUT");
//
        JCheckBox checkBox = new JCheckBox();
        checkBox.setName(key);
        buttonPanel.add(checkBox);
        JLabel textArea = new JLabel();
        textArea.setName(key);
        textArea.setText(label);
        buttonPanel.add(textArea);

        JButton button = new JButton();
        button.setBackground(color);
        button.setName(key);
//        ActionEvent colorAction = new ActionEvent(button, color.getRGB(), "ColorButton");
//        addAction(button, colorAction);
        buttonPanel.add(button);

//        JButton addButton= new JButton();
//        addButton.setName(key);
        JButton LUTButton = new JButton();
        LUTButton.setName(key);
//        JButton download= new JButton();
//        download.setName(key);
//        addButton(addButton, label, null, 605,280,350,250, buttonPanel, addbuttonAction, null);
        addButton(LUTButton, "open LUT", null, 605, 280, 350, 250, buttonPanel, lutAction, null);
//        addButton(download, null, downloadIcon, 605,280,350,250, buttonPanel, downloadAction, null);
        roiPanel.add(buttonPanel);
//        panel.add(GuiUtil.addScrollPanel(exampleList.get(key),null));
//        panel.add(GuiUtil.addScrollPanel(allexampleList.get(key),null));
//        roiPanel.add(panel );
//        exampleList.get(key).addMouseListener(mouseListener);
//        allexampleList.get(key).addMouseListener(mouseListener);
    }

    private void addClasses(String key, String label, Color color) {
        JCheckBox checkBox = new JCheckBox();
        checkBox.setName(key);
        jCheckBoxList.add(checkBox);
        JTextArea textArea = new JTextArea();
        textArea.setName(key);
        textArea.setText(label);
        jTextList.put(key, textArea);
        classPanel.add(checkBox);
        classPanel.add(textArea);
        JButton button = new JButton();
        button.setBackground(color);
        button.setName(key);
        ActionEvent colorAction = new ActionEvent(button, color.getRGB(), "ColorButton");
        addAction(button, colorAction);
        classPanel.add(button);
    }

    private void addAction(JButton button, final ActionEvent action) {
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                doAction(action);
            }
        });

    }

    private void loadImage(ImagePlus image) {
        this.displayImage = image;
        setImage(this.displayImage);
        updateImage(this.displayImage);
    }

    public void validateFrame() {
        frame.invalidate();
        frame.revalidate();
        frame.repaint();
    }

    public void doAction(final ActionEvent event) {
        if(event== OVERALY_BUTTON_PRESSED){
            isShowColorOverlay = !isShowColorOverlay;
            if (isShowColorOverlay) {
                List<ImagePlus> groundTruthImages = loadSavedImages(this.featureManager.getProjectManager().getProjectDir().get(ASCommon.TRAININGLABELSDIR));
                updateResultOverlayForGroundTruth(groundTruthImages.get(featureManager.getCurrentSlice() - 1));
            } else {
                resultOverlay.setImage(null);
                displayImage.updateAndDraw();
            }
        } // end if
//        if(event==DELETE_BUTTON_PRESSED){
//
//            System.out.println(featureManager.getNumOfClasses());
//            System.out.println(jCheckBoxList.size());
//            int totalDel=0;
//
//            for (JCheckBox checkBox : jCheckBoxList)
//                if (checkBox.isSelected())
//                    totalDel++;
//
//            if(featureManager.getNumOfClasses()-totalDel<2)
//                JOptionPane.showMessageDialog(null, "There should be minimum two classes");
//            else {
//                for (JCheckBox checkBox : jCheckBoxList)
//                    if (checkBox.isSelected())
//                        featureManager.deleteClass(checkBox.getName());
//                addClassPanel();
//                validateFrame();
//                updateGui();
//            }

//        } // end if
        if (event == TRAINING_INSTANCE_BUTTON_PRESSED) {
            this.customNumOfTrainingInstances = Integer.parseInt(numOfTrainingInstances.getText());
            saveGroundTruthFeatureMetadata();
            IJ.log(String.format("Number of training instances %d",customNumOfTrainingInstances));
        }


        if (event == SAVE_BUTTON_PRESSED) {
            saveGroundTruthFeatureMetadata();
            JOptionPane.showMessageDialog(null, "Successfully saved groundTruth metainfo!");
        } //end if

        if (event == SAVECLASS_BUTTON_PRESSED) {
            for (JCheckBox checkBox : jCheckBoxList) {
                //System.out.println(checkBox.getText());
                String key = checkBox.getName();
                featureManager.setClassLabel(key, jTextList.get(key).getText());

            }
            addClassPanel();
            validateFrame();
            updateGui();
        } // end if

        if (event == PREVIOUS_BUTTON_PRESSED) {
            ImagePlus image = featureManager.getPreviousImage();
//            image.getAllStatistics();
            imageNum.setText(Integer.toString(featureManager.getCurrentSlice()));
            loadImage(image);
            List<ImagePlus> groundTruthImages = loadSavedImages(this.featureManager.getProjectManager().getProjectDir().get(ASCommon.TRAININGLABELSDIR));

            if (showColorOverlay) {
                if (featureManager.getProjectType() == ProjectType.CLASSIF)
                    classifiedImage = null;
                else
                    classifiedImage = featureManager.getClassifiedImage();
                updateResultOverlay(classifiedImage);
            }

            // force limit size of image window
            if (ic.getWidth() > IMAGE_CANVAS_DIMENSION) {
                int x_centre = ic.getWidth() / 2 + ic.getX();
                int y_centre = ic.getHeight() / 2 + ic.getY();
                ic.zoomIn(x_centre, y_centre);
            }
            updateResultOverlayForGroundTruth(groundTruthImages.get(featureManager.getCurrentSlice()-1));
        } // end if

        if (event == NEXT_BUTTON_PRESSED) {
            ImagePlus image = featureManager.getNextImage();
            imageNum.setText(Integer.toString(featureManager.getCurrentSlice()));
            loadImage(image);
            List<ImagePlus> groundTruthImages = loadSavedImages(this.featureManager.getProjectManager().getProjectDir().get(ASCommon.TRAININGLABELSDIR));
            if (showColorOverlay) {
                if (featureManager.getProjectType() == ProjectType.CLASSIF)
                    classifiedImage = null;
                else
                    classifiedImage = featureManager.getClassifiedImage();
                updateResultOverlay(classifiedImage);
            }

            // force limit size of image window
            if (ic.getWidth() > IMAGE_CANVAS_DIMENSION) {
                int x_centre = ic.getWidth() / 2 + ic.getX();
                int y_centre = ic.getHeight() / 2 + ic.getY();
                ic.zoomIn(x_centre, y_centre);
            }
            //imagePanel.add(ic);
            updateGui();
            updateResultOverlayForGroundTruth(groundTruthImages.get(featureManager.getCurrentSlice()-1));
        } // end if

        if (event == COMPUTE_BUTTON_PRESSED) {
            if(customNumOfTrainingInstances==0 || customNumOfTrainingInstances > groundTruthImages.size()) {
                JOptionPane.showMessageDialog(null, "Set the number of training instanced to a valid value!");
            } else{
                if (featureManager.getProjectType() == ProjectType.CLASSIF) {
                    // it means new round of training, so set result setting to false
                    showColorOverlay = false;
                    // removing previous markings and reset things
                    predictionResultClassification = null;
                    displayImage.setOverlay(null);

                    // compute new predictions
                    featureManager.computeForGroundTruth(groundInfoClassList, imageToGroundTruthMap, groundTruthClassInfoPixelIndexedMap, customNumOfTrainingInstances);
                    predictionResultClassification = featureManager.getClassificationResultMap();

                    // we do not need to get any image in classification setting, only predictions are needed
                    classifiedImage = null;
                }

                //segmentation setting
                else {
                    classifiedImage = featureManager.computeForGroundTruth(groundInfoClassList, imageToGroundTruthMap, groundTruthClassInfoPixelIndexedMap, customNumOfTrainingInstances);
                }

            IJ.log("compute");

            toggleOverlay();}
        } //end if

        if (event == TOGGLE_BUTTON_PRESSED) {
            toggleOverlay();
        } // end if

//        if(event==DOWNLOAD_BUTTON_PRESSED){
//
//            ImagePlus image=featureManager.stackedClassifiedImage();
//            image.show();
//            //FileSaver saver= new FileSaver(image);
//            //saver.saveAsTiff();
//        } //end if

        if (event == MASKS_BUTTON_PRESSED) {
            System.out.println("masks ");
            if (classifiedImage == null) {
                classifiedImage = featureManager.compute();
            }
            classifiedImage.show();

        } //end if

        if (event == LOAD_GROUND_TRUTH_BUTTON_PRESSED) {
            System.out.println("Loading ground truth.... ");
            JFileChooser fileChooser = new JFileChooser();

            // For Directory
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fileChooser.setAcceptAllFileFilterUsed(false);
            int rVal = fileChooser.showOpenDialog(null);
            if (rVal == JFileChooser.APPROVE_OPTION) {
                String imageDir = fileChooser.getSelectedFile().toString();
                loadImageStack(imageDir, this.featureManager.getProjectManager().getProjectDir().get(ASCommon.TRAININGLABELSDIR));
                List<ImagePlus> images = loadSavedImages(this.featureManager.getProjectManager().getProjectDir().get(ASCommon.TRAININGLABELSDIR));
                String message = String.format("Successfully loaded the ground truth images!\n Num of Slices loaded: %d \n Bit depth: %d \n Images height: %d \n Images Width: %d ", images.size(), images.get(0).getBitDepth(), images.get(0).getHeight(), images.get(0).getWidth());
                JOptionPane.showMessageDialog(null, message);
                groundInfoClassList.clear();
                groundTruthClassInfoIndexedMap.clear();
                groundTruthClassInfoPixelIndexedMap.clear();


                System.out.println(images.toString());
                long[] hist = calculateGroundTruthClasses(images);
                Map<String, Integer> histData = getGroundTruthClassDetails(hist);
                int classIndex = 0;
                for (String label : histData.keySet()) {
                    GroundTruthClassInfo groundTruthClassInfo = new GroundTruthClassInfo(label, label, getColor(), histData.get(label), classIndex);
                    groundInfoClassList.put(label, groundTruthClassInfo);
                    groundTruthClassInfoIndexedMap.put(classIndex, groundTruthClassInfo);
                    groundTruthClassInfoPixelIndexedMap.put(histData.get(label), groundTruthClassInfo);
                    classIndex++;

                }
                this.groundTruthManager.setPixelClasses(histData);
                this.groundTruthManager.setNumClasses(histData.size());
                updateResultOverlayForGroundTruth(images.get(featureManager.getCurrentSlice()-1));

//                addClassPanel();
                addGroundTruthClassPanel(groundInfoClassList);
                validateFrame();
                for (int i = 0; i < groundTruthImages.size(); i++) {
                    imageToGroundTruthMap.put(this.featureManager.getImages().get(i), groundTruthImages.get(i));
                }



            }
            updateGui();
            saveGroundTruthFeatureMetadata();
            IJ.log("metadata updated!");

        } //end if


        if (event == LOAD_TEST_DATA_BUTTON_PRESSED) {
            System.out.println("Loading testing images.... ");
            JFileChooser fileChooser = new JFileChooser();

            // For Directory
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fileChooser.setAcceptAllFileFilterUsed(false);
            int rVal = fileChooser.showOpenDialog(null);
            if (rVal == JFileChooser.APPROVE_OPTION) {
                String imageDir = fileChooser.getSelectedFile().toString();
                loadImageStack(imageDir, this.featureManager.getProjectManager().getProjectDir().get(ASCommon.TESTIMAGESDIR));
                JOptionPane.showMessageDialog(null, "Successfully loaded the testing images!");


//                System.out.println(image.toString());
//                long[] hist = calculateGroundTruthClasses(image);
//                Map<Integer, Long> histData = getGroundTruthClassDetails(hist);
            }


        } //end if

        if (event == LUT_BUTTON_PRESSED) {

            LUT_Editor lut_editor1 = new LUT_Editor();
            lut_editor1.run("");

        }

        if (event.getActionCommand() == "ColorButton") {
            String key = ((Component) event.getSource()).getName();
//            Color c;
//            c = JColorChooser.showDialog(new JFrame(),
//                    "CLASS COLOR", groundInfoClassList.get(key).getColor());
//
//            ((Component) event.getSource()).setBackground(c);
//            groundInfoClassList.get(key).setColor(c);
//            updateGui();
//            updateResultOverlay(new ImagePlus(groundTruthImages.get(featureManager.getCurrentSlice()-1)));
            displayClassInfo(groundInfoClassList.get(key));
        }// end if

        if (event.getActionCommand() == "OpenLUT") {
            String key = ((Component) event.getSource()).getName();
            LUTLabelUI lut_editor1 = new LUTLabelUI(this, key);
            lut_editor1.run("");
        }//end if


    }

    public void displayClassInfo(GroundTruthClassInfo groundTruthClassInfo){

        GenericDialog gd =  new GenericDialog("Class details of "+ groundTruthClassInfo.getLabel());
        gd.setSize(50,50);
        gd.addStringField("Title: ", groundTruthClassInfo.getLabel());
        gd.addStringField("Key: ", groundTruthClassInfo.getKey());


        gd.showDialog();


    }


    /**
     * Toggle between overlay and original image with markings
     */
    private void toggleOverlay() {
        if (featureManager.getProjectType() == ProjectType.SEGM) {
            showColorOverlay = !showColorOverlay;
            if(null==classifiedImage){
                JOptionPane.showMessageDialog(null, "No classified image found!");
            }
            if (showColorOverlay && (null != classifiedImage)) {
                updateResultOverlay(classifiedImage);

            }else {
                resultOverlay.setImage(null);
                displayImage.updateAndDraw();
            }
        }

        // classification setting, no classified image
        else {
            showColorOverlay = !showColorOverlay;
            // user wants to see results
            if (showColorOverlay) {
                updateResultOverlay(classifiedImage);
            }

            // user wants to see original rois, no results
            else {

                // remove result overlay
                displayImage.setOverlay(null);
                displayImage.updateAndDraw();

                //just show examples drawn by user
                updateGui();
            }
        }
    }

    public void updateResultOverlay(ImagePlus classifiedImage) {
        if (featureManager.getProjectType() == ProjectType.SEGM) {
            ImageProcessor overlay = classifiedImage.getProcessor().duplicate();
            overlay = overlay.convertToByte(false);
            setLut(this.groundInfoClassList);
            overlay.setColorModel(overlayLUT);
            resultOverlay.setImage(overlay);
            displayImage.updateAndDraw();
        }

        if (featureManager.getProjectType() == ProjectType.CLASSIF) {
            // remove previous overlay
            displayImage.setOverlay(null);
            displayImage.updateAndDraw();

            //get current slice
            int currentSlice = featureManager.getCurrentSlice();
            Font font = new Font("Arial", Font.PLAIN, 38);
            Overlay overlay = new Overlay();
            ArrayList<Roi> rois;
            for (String classKey : featureManager.getClassKeys()) {
                //returns rois of current image slice of given class, current slice is updated internally
                rois = (ArrayList<Roi>) featureManager.getExamples(classKey, learningType.getSelectedItem().toString(), featureManager.getCurrentSlice());
                if (rois != null) {
                    for (Roi roi : rois) {
                        int pred = predictionResultClassification.get(roi.getName());
                        TextRoi textroi = new TextRoi(roi.getBounds().x, roi.getBounds().y,
                                roi.getFloatWidth(), roi.getFloatHeight(), Integer.toString(pred), font);
                        textroi.setFillColor(roi.getFillColor());
                        //textroi.setNonScalable(true);
                        textroi.setPosition(currentSlice);
                        overlay.add(textroi);
                    }
                }
            }
            // add result overlay
            displayImage.setOverlay(overlay);
            displayImage.updateAndDraw();
        }
    }

    public void setLut(Map<String,GroundTruthClassInfo> classInfos) {
        int i = 0;
        for (GroundTruthClassInfo classInfo : classInfos.values()) {
            red[classInfo.getPixelValue()] = (byte) classInfo.getColor().getRed();
            green[classInfo.getPixelValue()] = (byte) classInfo.getColor().getGreen();
            blue[classInfo.getPixelValue()] = (byte) classInfo.getColor().getBlue();
            i++;
        }
//        blue[255] = -1;
        overlayLUT = new LUT(red, green, blue);
    }

    private void updateGui() {
        try {
//            drawExamples();
//            updateExampleLists();
            //updateallExampleLists();
            ic.setMinimumSize(new Dimension(IMAGE_CANVAS_DIMENSION, IMAGE_CANVAS_DIMENSION));
            ic.repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawExamples() {
        for (String key : featureManager.getClassKeys()) {
            ArrayList<Roi> rois = (ArrayList<Roi>) featureManager.
                    getExamples(key, learningType.getSelectedItem().toString(), featureManager.getCurrentSlice());
            roiOverlayList.get(key).setColor(featureManager.getClassColor(key));
            roiOverlayList.get(key).setRoi(rois);
            //System.out.println("roi draw"+ key);
        }
        getImagePlus().updateAndDraw();
    }

    private void updateExampleLists() {
        LearningType type = (LearningType) learningType.getSelectedItem();
        for (String key : featureManager.getClassKeys()) {
            exampleList.get(key).removeAll();
            Vector<String> listModel = new Vector<String>();

            for (int j = 0; j < featureManager.getRoiListSize(key, learningType.getSelectedItem().toString(), featureManager.getCurrentSlice()); j++) {
                listModel.addElement(key + " " + j + " " +
                        featureManager.getCurrentSlice() + " " + type.getLearningType());
            }
            exampleList.get(key).setListData(listModel);
            exampleList.get(key).setForeground(featureManager.getClassColor(key));
        }
    }

    private MouseListener mouseListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent mouseEvent) {
            JList<?> theList = (JList<?>) mouseEvent.getSource();
            if (mouseEvent.getClickCount() == 1) {
                int index = theList.getSelectedIndex();

                if (index >= 0) {
                    String item = theList.getSelectedValue().toString();
                    String[] arr = item.split(" ");
                    //System.out.println("Class Id"+ arr[0].trim());
                    //int sliceNum=Integer.parseInt(arr[2].trim());
                    showSelected(arr[0].trim(), index);

                }
            }

            if (mouseEvent.getClickCount() == 2) {
                int index = theList.getSelectedIndex();
                String type = learningType.getSelectedItem().toString();
                if (index >= 0) {
                    String item = theList.getSelectedValue().toString();
                    //System.out.println("ITEM : "+ item);
                    String[] arr = item.split(" ");
                    //int classId= featureManager.getclassKey(arr[0].trim())-1;
                    featureManager.deleteExample(arr[0], Integer.parseInt(arr[1].trim()), type);
                    updateGui();
                }
            }
        }
    };


    /**
     * Select a list and deselect the others
     *
     * @param classKey item event (originated by a list)
     * @param index    list index
     */
    private void showSelected(String classKey, int index) {
        updateGui();


        displayImage.setColor(Color.YELLOW);
        String type = learningType.getSelectedItem().toString();
        //System.out.println(classKey+"--"+index+"---"+type);
        final Roi newRoi = featureManager.getRoi(classKey, index, type);
        //System.out.println(newRoi);
        newRoi.setImage(displayImage);
        displayImage.setRoi(newRoi);
        displayImage.updateAndDraw();
    }

    private JButton addButton(final JButton button, final String label, final Icon icon, final int x,
                              final int y, final int width, final int height,
                              JComponent panel, final ActionEvent action, final Color color) {
        panel.add(button);
        button.setText(label);
        button.setIcon(icon);
        button.setFont(panelFONT);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBackground(new Color(192, 192, 192));
        button.setForeground(Color.WHITE);
        if (color != null) {
            button.setBackground(color);
        }
        button.setBounds(x, y, width, height);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                //System.out.println(e.toString());
                doAction(action);
            }
        });

        return button;
    }

    private void setOverlay() {
        resultOverlay = new ImageOverlay();
        resultOverlay.setComposite(overlayAlpha);
        ((OverlayedImageCanvas) ic).addOverlay(resultOverlay);
    }

    private void downloadRois(String key) {
        String type = learningType.getSelectedItem().toString();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setAcceptAllFileFilterUsed(false);
        int rVal = fileChooser.showOpenDialog(null);
        if (rVal == JFileChooser.APPROVE_OPTION) {
            String name = fileChooser.getSelectedFile().toString();
            if (!name.endsWith(".zip")) {
                name = name + ".zip";
            }

            featureManager.saveExamples(name, key, type, featureManager.getCurrentSlice());
        }
    }


    /*
    Image loading and statistics calculation
     */
    private void createImages(String image, ImagePlus currentImage, String dir) {
        String format = image.substring(image.lastIndexOf("."));
        String folder = image.substring(0, image.lastIndexOf("."));
        if (currentImage.getStackSize() > 0) {
            createStackImage(currentImage, format, folder, dir);
        } else {
            String imgDir = dir + folder;
//            groundTruthImages.add(imgDir);
//            createDirectory(imgDir);
            IJ.saveAs(currentImage, format, imgDir);

        }

    }

    private void createStackImage(ImagePlus image, String format, String folder, String directory) {
        IJ.log("createStack");
        IJ.log(format);
        for (int i = 1; i <= image.getStackSize(); i++) {
            ImageProcessor processor = image.getStack().getProcessor(i);
            String title = folder + i;
            IJ.log(folder);
            IJ.log(title);
//            groundTruthImages.add(directory + title);
//            createDirectory(directory);
            IJ.saveAs(new ImagePlus(title, processor), format, directory + title);
        }
        IJ.log("createStackdone");
    }

    private boolean createDirectory(String project) {
        File file = new File(project);
        if (!file.exists()) {
            file.mkdirs();
        }
        return true;
    }

    private HashMap<Integer, long[]> generateClassStatistics(ImagePlus image) {
        HashMap<Integer, long[]> statistics = new HashMap<>();
        for (int i = 1; i <= image.getStackSize(); i++) {
            ImageProcessor processor = image.getStack().getProcessor(i);
            ImageStatistics imgStat = processor.getStatistics();
            statistics.put(i, imgStat.getHistogram());
        }
        IJ.log("Histogram calculated");
        return statistics;

    }

    private long[] calculateGroundTruthClasses(List<ImagePlus> images) {
        long[] histCommon = new long[256];
        for (ImagePlus image: images) {
            ImageProcessor processor = image.getProcessor();
            long[] hist = processor.getStats().getHistogram();
            for (int j = 0; j < 256; j++) {
                histCommon[j] += hist[j];
            }
        }
        return histCommon;
    }

    private Map<String, Integer> getGroundTruthClassDetails(long[] hist) {
        Map<String, Integer> classInfo = new HashMap<>();
        Integer labelIndex = 1;
        for (int i = 0; i < 256; i++) {
            if (hist[i] != 0) {
                classInfo.put("label"+ labelIndex.toString(), i);
                labelIndex++;
            }
        }
        return classInfo;
    }


    private void loadImageStack(String imageDir, String toDir) {
        if (imageDir.endsWith(".tif") || imageDir.endsWith(".tiff")) {
            ImagePlus imagePlus = IJ.openImage(imageDir);
            createImages(imagePlus.getTitle(), imagePlus, toDir);
        } else {
            List<String> images = loadImages(imageDir);
            for (String image : images) {
                ImagePlus currentImage = IJ.openImage(imageDir + "/" + image);
                createImages(currentImage.getTitle(), currentImage, toDir);

            }
        }

    }

    private List<String> loadImages(String dir) {
        List<String> imageList = new ArrayList<>();
        File folder = new File(dir);
        File[] images = sortImages(folder.listFiles());

        for (File file : images) {
            if (file.isFile()) {
                imageList.add(file.getName());
            }
        }
        return imageList;
    }

    private File[] sortImages(File[] images) {
        final Pattern p = Pattern.compile("\\d+");
        Arrays.sort(images, new Comparator<File>() {

            @Override
            public int compare(File o1, File o2) {
                Matcher m = p.matcher(o1.getName());
                Integer number1 = null;
                if (!m.find()) {
                    return o1.getName().compareTo(o2.getName());
                } else {
                    Integer number2 = null;
                    number1 = Integer.parseInt(m.group());
                    m = p.matcher(o2.getName());
                    if (!m.find()) {
                        return o1.getName().compareTo(o2.getName());
                    } else {
                        number2 = Integer.parseInt(m.group());
                        int comparison = number1.compareTo(number2);
                        if (comparison != 0) {
                            return comparison;
                        } else {
                            return o1.getName().compareTo(o2.getName());
                        }

                    }
                }
            }
        });
        return images;
    }

    private List<ImagePlus> loadSavedImages(String dir) {
        List<String> images = loadImages(dir);
        List<ImagePlus> imageStack = new ArrayList<>();
        groundTruthImages.clear();
        for (String image : images) {
            imageStack.add(new ImagePlus(dir+"/"+image));
            groundTruthImages.add(dir + "/" + image);
        }
        return imageStack;

    }

    private void setGroundTruthDirs(String dir) {
        List<String> images = loadImages(dir);
        for (String image : images) {
            groundTruthImages.add(dir + "/" + image);
        }
    }

    public void updateResultOverlayForGroundTruth(ImagePlus groundTruthImage)
    {
        if(featureManager.getProjectType()==ProjectType.SEGM) {
            overlayImage = groundTruthImage;
            ImageProcessor overlay = groundTruthImage.getProcessor().duplicate();
            overlay = overlay.convertToByte(false);
            setLut(this.groundInfoClassList);
            overlay.setColorModel(overlayLUT);
            resultOverlay.setImage(overlay);
            displayImage.updateAndDraw();
        }
    }


    //todo: make color unique
    private Color getColor() {
        float r = rand.nextFloat();
        float g = rand.nextFloat();
        float b = rand.nextFloat();
        Color randomColor = new Color(r, g, b);
        return randomColor;
    }

    public void addGroundTruthClassPanel(Map<String,GroundTruthClassInfo> groundTruthClasses) {
//        classPanel.removeAll();
        roiPanel.removeAll();
//        jCheckBoxList.clear();
//        jTextList.clear();
        int classes = groundTruthManager.getNumOfClasses();
//        IJ.log(Integer.toString(classes));
//        if(classes%3==0){
//            int tempSize=classes/3;
//            classPanel.setPreferredSize(new Dimension(340, 80+30*tempSize));
//        }
        roiPanel.setPreferredSize(new Dimension(350, 175 * classes));
//        addButton(new JButton(), "ADD CLASS",null , 630, 20, 130, 20,classPanel,ADDCLASS_BUTTON_PRESSED,null );
//        addButton(new JButton(), "SAVE CLASS",null , 630, 20, 130, 20,classPanel,SAVECLASS_BUTTON_PRESSED,null );
//        addButton(new JButton(), "DELETE CLASS",null , 630, 20, 130, 20,classPanel,DELETE_BUTTON_PRESSED,null );
        for (GroundTruthClassInfo classInfo : groundTruthClasses.values()) {
//            String label = featureManager.getClassLabel(key);
//            Color color = featureManager.getClassColor(key);
////            addClasses(key,label,color);
            addSidePanel(classInfo.getColor(), classInfo.getKey(), classInfo.getLabel());
        }
    }

    public Map<String, GroundTruthClassInfo> getGroundInfoClassList() {
        return groundInfoClassList;
    }

    public void setGroundInfoClassList(Map<String, GroundTruthClassInfo> groundInfoClassList) {
        this.groundInfoClassList = groundInfoClassList;
    }

    public List<String> getGroundTruthImages() {
        return groundTruthImages;
    }

    public void setGroundTruthImages(List<String> groundTruthImages) {
        this.groundTruthImages = groundTruthImages;
    }

    public List<String> getTestingImages() {
        return testingImages;
    }

    public void setTestingImages(List<String> testingImages) {
        this.testingImages = testingImages;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Map<Integer, GroundTruthClassInfo> getGroundTruthClassInfoIndexedMap() {
        return groundTruthClassInfoIndexedMap;
    }

    public void setGroundTruthClassInfoIndexedMap(Map<Integer, GroundTruthClassInfo> groundTruthClassInfoIndexedMap) {
        this.groundTruthClassInfoIndexedMap = groundTruthClassInfoIndexedMap;
    }

    public ImagePlus getOverlayImage() {
        return overlayImage;
    }

    public void setOverlayImage(ImagePlus overlayImage) {
        this.overlayImage = overlayImage;
    }

    public Map<Integer, GroundTruthClassInfo> getGroundTruthClassInfoPixelIndexedMap() {
        return groundTruthClassInfoPixelIndexedMap;
    }

    public void setGroundTruthClassInfoPixelIndexedMap(Map<Integer, GroundTruthClassInfo> groundTruthClassInfoPixelIndexedMap) {
        this.groundTruthClassInfoPixelIndexedMap = groundTruthClassInfoPixelIndexedMap;
    }

    public void saveGroundTruthFeatureMetadata() {
        ProjectInfo projectInfo = projectManager.getMetaInfo();
        projectInfo.resetGroundTruthFeatureInfo();
        for (GroundTruthClassInfo groundTruthClassInfo : groundInfoClassList.values()) {

            GroundTruthFeatureInfo groundTruthFeatureInfo = new GroundTruthFeatureInfo();
            groundTruthFeatureInfo.setKey(groundTruthClassInfo.getKey());
            groundTruthFeatureInfo.setLabel(groundTruthClassInfo.getLabel());
            groundTruthFeatureInfo.setPixelValue(groundTruthClassInfo.getPixelValue());
            groundTruthFeatureInfo.setColor(groundTruthClassInfo.getColor().getRGB());
            groundTruthFeatureInfo.setClassIndex(groundTruthClassInfo.getClassIndex());
            projectInfo.addGroundTruthFeature(groundTruthFeatureInfo);
        }
        projectInfo.setNumOfTrainingInstances(customNumOfTrainingInstances);
        projectInfo.setGroundTruthClasses(groundInfoClassList.size());
        projectManager.writeMetaInfo(projectInfo);
    }

    public boolean setGroundTruthFeatureMetadata() {
        boolean alreadysetClass = false;
        ProjectInfo projectInfo = projectManager.getMetaInfo();
        for (GroundTruthFeatureInfo groundTruthFeatureInfo : projectInfo.getGroundTruthFeatureInfoList()) {
           alreadysetClass = true;
            GroundTruthClassInfo groundTruthClassInfo = new GroundTruthClassInfo(groundTruthFeatureInfo.getKey(), groundTruthFeatureInfo.getLabel(),
                    new Color(groundTruthFeatureInfo.getColor()), groundTruthFeatureInfo.getPixelValue(), groundTruthFeatureInfo.getClassIndex());
            groundInfoClassList.put(groundTruthFeatureInfo.getKey(), groundTruthClassInfo);
            groundTruthClassInfoPixelIndexedMap.put(groundTruthFeatureInfo.getPixelValue(), groundTruthClassInfo);
            groundTruthClassInfoIndexedMap.put(groundTruthFeatureInfo.getClassIndex(), groundTruthClassInfo);
        }
        customNumOfTrainingInstances = projectInfo.getNumOfTrainingInstances();
        setGroundTruthDirs(this.featureManager.getProjectManager().getProjectDir().get(ASCommon.TRAININGLABELSDIR));
        for (int i = 0; i < groundTruthImages.size(); i++) {
            imageToGroundTruthMap.put(this.featureManager.getImages().get(i), groundTruthImages.get(i));
        }
        if (imageToGroundTruthMap.size() != 0) {
            overlayImage = new ImagePlus(imageToGroundTruthMap.get(featureManager.getImages().get(featureManager.getCurrentSlice() - 1)));
        }if(featureManager.getClassifiedImage().getImage()!=null){
            classifiedImage = featureManager.getClassifiedImage();
        }
        return alreadysetClass;
    }

}