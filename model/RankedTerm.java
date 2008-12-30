package model;

import java.util.Random;

public class RankedTerm implements Comparable<RankedTerm> {
	
	private String term;
	private double ranking;
	
	public RankedTerm(String term, double ranking) {
		this.term = term;
		this.ranking = ranking;
	}
	public double getRanking() {
		return ranking;
	}
	public void setRanking(double ranking) {
		this.ranking = ranking;
	}
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public int compareTo(RankedTerm rTerm) {
		double ranking1 = this.getRanking();
		double ranking2 = rTerm.getRanking();
		String term1 = this.getTerm();
		String term2 = rTerm.getTerm();
		Random random = new Random();
		int result=0;
		if(ranking1<ranking2)
			result=1;
		else if(ranking1>ranking2)
			result=-1;
		else
			if(!term1.equals(term2))
				while(result==0)
					result = random.nextInt();
		
		return result;
	}
	
	public int hashCode() {
		return term.hashCode();
	}
	
	public boolean equals(Object obj) {
		return this.getTerm().equals(((RankedTerm)obj).getTerm());
	}
	
	public String toString() {
		return "(term=" + this.getTerm() + ",ranking=" + this.getRanking() + ")";
	}

}
