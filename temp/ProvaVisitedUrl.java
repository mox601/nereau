package temp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.ExpandedQuery;
import model.Query;
import model.RankedTag;
import model.User;
import model.VisitedURL;
import persistence.PersistenceException;
import persistence.UserDAO;
import persistence.VisitedURLDAO;
import persistence.postgres.UserDAOPostgres;
import persistence.postgres.VisitedURLDAOPostgres;

@SuppressWarnings("unused")
public class ProvaVisitedUrl {
	
	public static void main(String[] args) {
		
		
		VisitedURLDAO vudao = new VisitedURLDAOPostgres();
		UserDAO udao = new UserDAOPostgres();
		
		/*
		String urlString = "http://www.miomio.com";
		Query query = new Query("baus");
		Set<RankedTag> rTags = new HashSet<RankedTag>();
		rTags.add(new RankedTag("amazon",1.1));
		rTags.add(new RankedTag("books",2.6));
		ExpandedQuery expQuery = new ExpandedQuery("baus bau",rTags);
		VisitedURL vUrl = new VisitedURL(urlString,query,null);
		User user = new User("iddio","padre");
		
		try {
			vudao.saveVisitedURL(vUrl, user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
		
		
		try {
			List<VisitedURL> vUrls = vudao.retrieveLastVisitedURLs(new User("iddio"));
			
			System.out.println("num of results: " + vUrls.size());
			
			for(VisitedURL vUrl: vUrls) {
				
				System.out.println("url=" + vUrl.getURL() + ", date=" + vUrl.getDate() + 
						", query=" + vUrl.getQuery());
				if(vUrl.getExpandedQuery()!=null) {
					System.out.println("expquery=" + vUrl.getExpandedQuery() + 
							", tags=" + vUrl.getExpandedQuery().getExpansionTags());
				}
				
			}
			
			//udao.saveLastUpdate("iddio", vUrls.get(0).getDate());
			
			
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		
		
	}

}
