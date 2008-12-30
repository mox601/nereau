package server;

import model.Nereau;
import model.User;

public class UpdateUserModelThread implements Runnable {
	
	private Nereau nereau;
	private User user;
	
	public UpdateUserModelThread(Nereau nereau, User user) {
		this.nereau = nereau;
		this.user = user;
	}

	@Override
	public void run() {
		nereau.updateUserModel(user);
	}

}
