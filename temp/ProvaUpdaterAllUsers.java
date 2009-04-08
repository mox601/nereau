package temp;

import model.usermodel.UserModelUpdater;

public class ProvaUpdaterAllUsers {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		
		UserModelUpdater userUpdater = UserModelUpdater.getInstance();
		userUpdater.updateAll();
		
		
	}

}
