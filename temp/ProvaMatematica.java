package temp;

public class ProvaMatematica {

	public static void main(String[] args) {
		int similarity = 10;
		int scala = 10;
		
		while (similarity >= 0) {
			System.out.println("scala: " + scala);
			double value = (double) similarity / scala;
			System.out.println("similarity: " + value);
			similarity = similarity - 1;
		}
	}
	
}
