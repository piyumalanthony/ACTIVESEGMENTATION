
import javax.swing.SwingUtilities;

import activeSegmentation.gui.CreateProjectUI;
import activeSegmentation.prj.ProjectManager;
import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;

import java.io.File;


public class Active_Segmentation_ implements PlugIn {




	/** main GUI panel (containing the buttons panel on the left,
	 *  the image in the center and the annotations panel on the right */
	public Active_Segmentation_(){

	}


	/**
	 * This method will be an entry point into the Plugin. All the
	 * dependency are inject through this class. This method is written according to 
	 * ImageJ plugin loading requirements
	 * @param parameter for imageJ
	 *
	 */
	@Override
	public void run(String arg0) {
	
		ProjectManager dataManager= new ProjectManager();
		CreateProjectUI creatProject= new CreateProjectUI(dataManager);
		SwingUtilities.invokeLater(creatProject);
		IJ.log(arg0);

	}




	public static void main(String[] args) {
		try {
			File f=new File(args[0]);
			if (f.exists() && f.isDirectory() ) {
				System.setProperty("plugins.dir", args[0]);
	 		} else {
				throw new IllegalArgumentException();
			}
		}
		catch (Exception ex) {
			IJ.log("plugins.dir misspecified\n");
			ex.printStackTrace();
		}
		
		new ImageJ();
		Active_Segmentation_ as=new Active_Segmentation_();
		as.run("");
	}

}