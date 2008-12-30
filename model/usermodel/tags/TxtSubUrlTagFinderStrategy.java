package model.usermodel.tags;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import model.RankedTag;

public class TxtSubUrlTagFinderStrategy extends SubUrlTagFinderStrategy {

	@Override
	protected Map<String, Integer> extractTags(String pageContent) {
		//NEVER USED!
		return null;
	}

	@Override
	public Set<RankedTag> findTagsForSubUrl(String urlString, double relevance, boolean exactUrl) {

		File tagFile;
		Scanner scanner;
		Set<RankedTag> result = new HashSet<RankedTag>();
		tagFile = new File(urlString);
		try {
			//extract data from file
			scanner = new Scanner(tagFile);
			StringBuffer pageContent = new StringBuffer();
			while(scanner.hasNextLine())
				pageContent.append(scanner.nextLine() + '\n');
			
			//put data into result set
			scanner = new Scanner(pageContent.toString());
			while(scanner.hasNextLine()) {
				String tag, rankString, next;
				double rank;
				next = scanner.nextLine();
				tag = next.substring(0, next.indexOf(" "));
				rankString = next.substring(next.indexOf(" ")+1);
				rank = Double.parseDouble(rankString);
				result.add(new RankedTag(tag,rank));
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}

}
