package model;

import java.util.Map;

import util.ParameterHandler;

public class RankedTag implements Comparable<RankedTag> {
	
	String tag;
	double ranking;
	
	public RankedTag(String tag) {
		this.tag = tag;
		this.ranking = 0.;
	}
	
	public RankedTag(String tag, double ranking) {
		this.tag = tag;
		this.ranking = ranking;
	}

	public String getTag() {
		return this.tag;
	}
	
	public boolean equals(Object o) {
		String tag1 = this.getTag();
		String tag2 = ((RankedTag)o).getTag();
		return tag1.equals(tag2);
	}
	
	public int hashCode() {
		return this.getTag().hashCode();
	}
	
	public String toString() {
		String result =
			"(tag=" + tag + ",rank=" + ranking + ")";
		return result;
	}

	public double getRanking() {
		return this.ranking;
	}
	
	public void setRanking(double ranking) {
		this.ranking = ranking;
	}

	public int compareTo(RankedTag rTag) {
		double ranking1 = this.getRanking();
		double ranking2 = rTag.getRanking();
		String tag1 = this.getTag();
		String tag2 = rTag.getTag();
		int result=0;
		if(ranking1<ranking2)
			result=1;
		else if(ranking1>ranking2)
			result=-1;
		else if(tag1!=tag2 && !tag1.equals(tag2))
			result = -1;
		return result;
	}

	public void updateRanking(String term, Map<String, Double> termsMap) {
		double ranking;
		if(this.getTag().equals(ParameterHandler.NULL_TAG.getTag()))
			ranking = ParameterHandler.NULL_TAG.getRanking();
		else {
			ranking = this.getRanking();
			for(String key: termsMap.keySet()) {
				double newRanking = termsMap.get(key);
				if(newRanking>ranking && !key.equals(term))
					ranking = newRanking;
			}
		}
		this.setRanking(ranking);
	}

	public void updateRanking(String term, Map<String, Double> termsMap1, Map<String, Double> termsMap2) {
		this.updateRanking(term,termsMap1);
		this.updateRanking(term,termsMap2);
	}

}
