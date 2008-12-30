package persistence;

public class PersistenceException extends Exception {

	private static final long serialVersionUID = 1L;

	public PersistenceException(){
		super();
	}
	
	public PersistenceException(String message){
		super(message);
	}
	
	public PersistenceException(Exception e){
        super(e);
    }
}

