package cluster;

public class IdGenerator {
	private int id;
		
	public IdGenerator() {
		this.id = 0;
	}
	
	public int getId() {
		this.id = this.id + 1;
		return this.id;
	}

}
