package temp;

import model.GlobalProfile;
import model.GlobalProfileModel;


public class ProvaGlobalProfileModel {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GlobalProfile globalProfile = new GlobalProfile();
		GlobalProfileModel globalModel = new GlobalProfileModel(globalProfile);
		
		globalModel.updateGlobalProfileModel();
		

	}

}
