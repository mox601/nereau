package model;

public class User {

	private UserModel userModel;
	private String username;
	private String password;
	private String firstName;
	private String lastName;
	private String email;
	private int role;
	private int userID;
	
	private User() {
		this.userModel = new UserModel(this);
	}
	
	public User(String username, String password,
			String firstName, String lastName, String email, int role,
			int userID) {
		this();
		this.username = username;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.role = role;
		this.userID = userID;
	}
	
	public User(String username) {
		this();
		this.username = username;
		this.password = "prova";
	}

	public User(String username, String password) {
		this();
		this.username = username;
		this.password = password;
	}

	public User(String username, int userID) {
		this(username);
		this.userID = userID;
	}

	public User(int userID) {
		this();
		this.userID = userID;
	}

	public UserModel getUserModel() {
		return this.userModel;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setUserModel(UserModel userModel) {
		this.userModel = userModel;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getUserID() {
		return this.userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}



}
