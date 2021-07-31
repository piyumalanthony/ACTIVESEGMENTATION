package activeSegmentation.feature;

import activeSegmentation.ASCommon;
import activeSegmentation.IFeature;
import activeSegmentation.LearningType;
import activeSegmentation.ProjectType;
import activeSegmentation.classif.RoiInstanceCreator;
import activeSegmentation.learning.ClassifierManager;
import activeSegmentation.prj.ClassInfo;
import activeSegmentation.prj.ProjectInfo;
import activeSegmentation.prj.ProjectManager;
import activeSegmentation.util.GuiUtil;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroundTruthManager {

    private ProjectManager projectManager;
    private ProjectInfo projectInfo;
    private Random rand = new Random();
    private String projectString, featurePath;
    private int sliceNum, totalSlices;
    private List<String> images;
    private List<String> trainingLabels;
    private List<String> testImages;
    private Map<ProjectType, IFeature> featureMap = new HashMap<>();
    private Map<String, ClassInfo> classes = new TreeMap<>();
    private List<Color> defaultColors;
    private Integer NumClasses = 0;
    ClassifierManager learningManager;
    Map<String, Integer> pixelClasses;
    private Map<String, Integer> predictionResultClassification;

    public GroundTruthManager(ProjectManager projectManager, ClassifierManager learningManager) {
        this.projectManager = projectManager;
        this.learningManager = learningManager;
        this.projectInfo = this.projectManager.getMetaInfo();
        this.images = new ArrayList<>();
        this.projectString = this.projectInfo.getProjectDirectory().get(ASCommon.IMAGESDIR);
        //System.out.println(this.projectString);
        this.featurePath = this.projectInfo.getProjectDirectory().get(ASCommon.FEATURESDIR);
        this.totalSlices = loadImages(this.projectString);
        this.defaultColors = GuiUtil.setDefaultColors();
        if (this.totalSlices > 0) {
            this.sliceNum = 1;
        }

        featureMap.put(ProjectType.SEGM, new PixelInstanceCreator(projectInfo));
        featureMap.put(ProjectType.CLASSIF, new RoiInstanceCreator(projectInfo));
    }

    private int loadImages(String directory) {
        this.images.clear();
        File folder = new File(directory);
        File[] images = folder.listFiles();
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
                }
        );
        for (File file : images) {
            //System.out.println(file.getName());
            if (file.isFile()) {
                this.images.add(file.getName());
            }
        }
        return this.images.size();
    }

    public Integer getNumClasses() {
        return NumClasses;
    }

    public void setNumClasses(Integer numClasses) {
        NumClasses = numClasses;
    }

    public Map<String, Integer> getPixelClasses() {
        return pixelClasses;
    }

    public void setPixelClasses(Map<String, Integer> pixelClasses) {
        this.pixelClasses = pixelClasses;
    }

    public Set<String> getClassKeys() {
        return classes.keySet();
    }

    public String getClassLabel(String index) {
        return classes.get(index).getLabel();
    }

    public void setClassLabel(String key, String label) {
        ClassInfo info = classes.get(key);
        info.setLabel(label);
        classes.put(key, info);
    }

    public int getNumOfClasses() {
        return classes.size();
    }

    private Color getColor(int number) {
        if (number < defaultColors.size()) {
            return defaultColors.get(number);
        } else {
            float r = rand.nextFloat();
            float g = rand.nextFloat();
            float b = rand.nextFloat();
            Color randomColor = new Color(r, g, b);
            return randomColor;
        }

    }

    public void deleteClass(String key) {
        classes.remove(key);
    }

    public List<Color> getColors() {
        List<Color> colors = new ArrayList<>();
        for (ClassInfo classInfo : classes.values()) {
            colors.add(classInfo.getColor());
        }
        return colors;
    }

    public ProjectManager getProjectManager() {
        return projectManager;
    }

    public void setProjectManager(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }

    public void setProjectInfo(ProjectInfo projectInfo) {
        this.projectInfo = projectInfo;
    }

    public void addClass(Integer index) {
        String key = UUID.randomUUID().toString();
        if (!classes.containsKey(key)) {
            Map<String, List<Roi>> trainingRois = new HashMap<>();
            Map<String, List<Roi>> testingRois = new HashMap<>();
            ClassInfo classInfo = new ClassInfo(key, "label" + index.toString(), getColor(index), trainingRois, testingRois);
            classes.put(key, classInfo);
        }
    }
}
