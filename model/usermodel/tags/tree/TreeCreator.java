package model.usermodel.tags.tree;

import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import model.RankedTag;

import util.ParameterHandler;

public class TreeCreator {
	
	private static StringBuffer getPageContent(String urlString) {
		
		URL url = null;
		Scanner scanner = null;
		StringBuffer pageContent = null;
		
		try {
			
			url = new URL(urlString);
			URLConnection urlConnection = url.openConnection();
			urlConnection.setConnectTimeout(ParameterHandler.URL_TIMEOUT);
			scanner = new Scanner(urlConnection.getInputStream());
			
		}
		catch(Exception e) {
			
			System.err.println(e.getMessage());
			return null;
			
		}
		
		pageContent = new StringBuffer();
		while(scanner.hasNextLine())
			pageContent.append(scanner.nextLine());
		
		return pageContent;
		
	}
	
	private static Set<RankedTag> getCommonTags() {
		
		Set<RankedTag> result = new HashSet<RankedTag>();
		int startIndex, endIndex;
		
		StringBuffer pageContent = getPageContent("http://delicious.com/tag?sort=numsaves");
		
		//obtaining rough tag cloud
		startIndex = pageContent.indexOf("alphacloud");
		startIndex = pageContent.indexOf("<div>", startIndex) + "<div>".length();
		endIndex = pageContent.indexOf("</div>", startIndex);
		
		String tagcloud = pageContent.substring(startIndex, endIndex);
		String tag;
		Set<String> tagTemp = new HashSet<String>();
		startIndex = tagcloud.indexOf('>') + 1;
		endIndex = tagcloud.indexOf("</a>");
		
		int count = 0;
		
		while(startIndex>=0 && endIndex>=0) {

			tag = tagcloud.substring(startIndex, endIndex);
			//System.out.println("Retrieved tag: " + tag);
			tagTemp.add(tag);
			double rank = (double)getCommonBookmarks(tagTemp);
			//System.out.println("Bookmarks: " + (int)rank);
			//System.out.println("startIndex = " + startIndex + ", endIndex = " + endIndex + ".");
			result.add(new RankedTag(tag,rank));
			tagcloud = tagcloud.substring(endIndex + "</a>".length());
			startIndex = tagcloud.indexOf('>') + 1;
			endIndex = tagcloud.indexOf("</a>");
			count++;
			tagTemp.clear();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		//System.out.println(tagcloud);
		
		return result;
		
	}
	
	private static int getCommonBookmarks(Set<String> tags) {
		
		int result = 0;
		int startIndex, endIndex;
		StringBuffer tagString = new StringBuffer();
		for(String tag: tags)
			tagString.append(tag + "+");
		
		StringBuffer pageContent = getPageContent("http://delicious.com/tag/" + 
				tagString.substring(0, tagString.length()-1));
		
		startIndex = pageContent.indexOf("pagination");
		if(startIndex < 0) 
			return result;
		startIndex = pageContent.indexOf("<p>", startIndex) + "<p>".length();
		endIndex = pageContent.indexOf(" Bookmarks</p>", startIndex);
		if(startIndex < 0 || endIndex < 0)
			return result;
		
		String count = pageContent.substring(startIndex, endIndex);
		result = Integer.parseInt(count);
		
		return result;
		
	}
	
	public static void main(String[] args) {
		
		System.out.println("Getting common tags...");
		Set<RankedTag> tags = getCommonTags();
		System.out.println(tags.size() + " tags retrieved.");
		List<RankedTag> tagList = new LinkedList<RankedTag>();
		for(RankedTag r: tags) {
			System.out.println(r.getTag());
			tagList.add(r);
		}
		Set<String> couple = new HashSet<String>();
		Random rand = new Random();
		DecimalFormat df = new DecimalFormat("0.00");
		for(int i=0; i<200; i++) {
			int pos1, pos2;
			pos1 = rand.nextInt(tagList.size());
			pos2 = rand.nextInt(tagList.size());
			couple.add(tagList.get(pos1).getTag());
			couple.add(tagList.get(pos2).getTag());
			int common = getCommonBookmarks(couple);
			double sim = getSimilarity(tagList.get(pos1),tagList.get(pos2),common);
			System.out.println("'" + tagList.get(pos1).getTag() + "' and '" + 
					tagList.get(pos2).getTag() + "' are " + df.format(sim) + "% similar.");
			couple.clear();
			/*
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
		}
		
	}

	private static double getSimilarity(RankedTag rt1, RankedTag rt2, int common) {
		double f1, f2, f12;
		f1 = rt1.getRanking();
		f2 = rt2.getRanking();
		f12 = (double)common;
		return 100.0 * f12 * (f1*f1 + f2*f2) / (f1 * f2 * (f1 + f2));
	}

}
