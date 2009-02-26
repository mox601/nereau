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
		
		
		String urlString = "http://woork.blogspot.com/2009/02/free-ajax-components-for-advanced-web.html";
		Query query = new Query("ajax");
		Set<RankedTag> rTags = new HashSet<RankedTag>();
		rTags.add(new RankedTag("ajax",1.1));
		rTags.add(new RankedTag("components",2.6));
		ExpandedQuery expQuery = new ExpandedQuery("ajax components",rTags);
		VisitedURL vUrl = new VisitedURL(urlString,query,null);
		User user = new User("mox","pwd");
		
		try {
			vudao.saveVisitedURL(vUrl, user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		try {
			List<VisitedURL> vUrls = vudao.retrieveLastVisitedURLs(new User("mox"));
			
			System.out.println("num of results: " + vUrls.size());
			
			for(VisitedURL vUrl2: vUrls) {
				
				System.out.println("url=" + vUrl2.getURL() + ", date=" + vUrl2.getDate() + 
						", query=" + vUrl2.getQuery());
				if(vUrl2.getExpandedQuery()!=null) {
					System.out.println("expquery=" + vUrl2.getExpandedQuery() + 
							", tags=" + vUrl2.getExpandedQuery().getExpansionTags());
				}
				
			}
			
			//udao.saveLastUpdate("iddio", vUrls.get(0).getDate());
			
			
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		
		
	}

}
