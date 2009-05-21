package model.usermodel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import model.GlobalProfileModel;
import model.PersonalProfileModel;
import model.RankedTag;
import model.URLTags;
import model.User;
import model.UserModel;
import model.VisitedURL;
import model.usermodel.tags.TagFinder;

import persistence.PersistenceException;
import persistence.UserDAO;
import persistence.VisitedURLDAO;
import persistence.postgres.UserDAOPostgres;
import persistence.postgres.VisitedURLDAOPostgres;

import util.LogHandler;

public class UserModelUpdater {
	
	private static UserModelUpdater instance;
	
	private UserDAO userDAOHandler;
	private VisitedURLDAO visitedURLHandler;
	private UserMatrixCalculator userMatrixCalculator;
	private boolean alreadyRetrieved;
	private List<VisitedURL> vUrls;

	public static synchronized UserModelUpdater getInstance() {
		if(instance==null)
			instance = new UserModelUpdater();
		return instance;
	}
	
	public UserModelUpdater() {
		this.userDAOHandler = 
			new UserDAOPostgres();
		this.visitedURLHandler = 
			new VisitedURLDAOPostgres();
		this.userMatrixCalculator = 
			new UserMatrixCalculator();
		this.alreadyRetrieved = false;
	}
	
	public void update(User user, List<VisitedURL> alreadyRetrieved) {
		
		this.alreadyRetrieved = true;
		this.vUrls = alreadyRetrieved;
		update(user);
		
	}
	
	public void updateAll() {
		
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		
		Set<User> users = new HashSet<User>();
		try {
			users = this.userDAOHandler.retrieveUsers();
		} catch (PersistenceException e) {
			logger.info("ATTENZIONE: impossibile reperire utenti dal db. motivo: \n"+e.getMessage());
		}
		
		for(User user: users)
			this.update(user);
	}

	
	/* questo metodo si occupa di aggiornare il profilo utente, basandosi sugli url 
	 * visitati dall'utente (nella storia o nell'immediato, se li passo nel metodo update) 
	 * */
	public void update(User user) {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		
		Map<RankedTag,Map<String,Map<String,Double>>> tempMatrix;
		
		//retrieve visited urls from db
		List<VisitedURL> visitedURLs = null;
		if(alreadyRetrieved)
			/* se gli url sono stati passati nel metodo update(User user, List<VisitedURL> alreadyRetrieved), 
			 * allora usa quelli che ti hanno passato */
			visitedURLs = this.vUrls;
		else {
			try {
				/* altrimenti, devi prendere dal db TUTTI gli url visitati dallo user */
				visitedURLs = this.visitedURLHandler.retrieveLastVisitedURLs(user);
				logger.info("url visitati: " + visitedURLs);
			}
			catch(PersistenceException e) {
				visitedURLs = new LinkedList<VisitedURL>();
				logger.info("ATTENZIONE: impossibile reperire url visitati dal db. motivo: \n"+e.getMessage());
			}
		}
		
		/* contiene la lista di urltags da passare al GlobalModel e al PersonalModel */
		LinkedList<URLTags> urls = new LinkedList<URLTags>();
		
		
		
		/* posso costruire un globalModel direttamente con i visitedURL 
		 * ed estrarre per conto mio i tag di delicious, senza aspettare 
		 * il processamento di tutte le matrici */
		
//		GlobalProfileModel fasterGlobalProfile = new GlobalProfileModel(visitedURLs);
		/* dopo che l'ho creato, posso salvare il GlobalProfileModel nel db 
		 * aggiungendo tutti gli url e le occorrenze come righe della tabella 
		 * tagsvisitedurl, usando per� gli id degli url e dei tags. */
//		fasterGlobalProfile.updateGlobalProfile();
		
		
		
		/* AGGIORNAMENTO DEL MODELLO UTENTE */
		/* itera su tutte le pagine visitate */
		//MODIFICA: lavoro su una pagina per volta (altrimenti la memoria non basta!)
		for(VisitedURL vu: visitedURLs) {
			
			List<VisitedURL> singleVURL = new LinkedList<VisitedURL>();
			singleVURL.add(vu);
			
			//creates the temporary matrix with data retrieved from visited urls
			tempMatrix = this.userMatrixCalculator.createTemporaryMatrix(visitedURLs,alreadyRetrieved);
			
			//print to log file
			StringBuffer tempMatrixForLog = new StringBuffer("{");
			for(RankedTag rTag: tempMatrix.keySet()) {
				tempMatrixForLog.append(rTag + "=" + tempMatrix.get(rTag).keySet() + ",");
			}

			tempMatrixForLog.append("}");
			//qui salvo nel log i tag estratti dalle pagine seguiti dai termini associati
			//logger.info("matrice temporanea (solo keyset): " + tempMatrixForLog);
			logger.info("matrice temporanea: " + tempMatrix);
			
			//now we update the existent user model
			Set<String> terms = this.getTermSet(tempMatrix);
			logger.info("insieme di termini: " + terms);
			
			UserModel userModel = user.getUserModel();
			Map<String, Map<RankedTag, Map<String, Double>>> subMatrix = 
				userModel.getSubMatrix(terms);
			//print to log file
			StringBuffer subMatrixForLog = new StringBuffer("{");
			for(String term: subMatrix.keySet()) {
				if(tempMatrix.containsKey(term))
					subMatrixForLog.append(term + "=" + tempMatrix.get(term).keySet() + ",");
			}
			subMatrixForLog.append("}");
			logger.info("sottoinsieme del modello utente (solo keyset): " + subMatrixForLog);
			
			//totally new data
			Map<String, Map<RankedTag, Map<String,Double>>> newInsertMatrix = 
				new HashMap<String, Map<RankedTag, Map<String,Double>>> ();
			//to be updated data
			Map<String, Map<RankedTag, Map<String,Double>>> newUpdateMatrix = 
				new HashMap<String, Map<RankedTag, Map<String,Double>>> ();
			//fills the two maps with new and to-be-updated data
			this.updateUserModel(subMatrix,tempMatrix,terms,newInsertMatrix,newUpdateMatrix);
			
			//print to log file
			StringBuffer newInsertMatrixForLog = new StringBuffer("{");
			for(String term: newInsertMatrix.keySet()) {
				newInsertMatrixForLog.append(term + "=" + newInsertMatrix.get(term).keySet() + ",");
			}
			newInsertMatrixForLog.append("}");
			logger.info("dati nuovi da inserire: " + newInsertMatrixForLog);
			
			StringBuffer newUpdateMatrixForLog = new StringBuffer("{");
			for(String term: newUpdateMatrix.keySet()) {
				newUpdateMatrixForLog.append(term + "=" + newUpdateMatrix.get(term).keySet() + ",");
			}
			newUpdateMatrixForLog.append("}");
			logger.info("dati da aggiornare: " + newUpdateMatrixForLog);
			
			//update user model with collected data
			userModel.update(newInsertMatrix,newUpdateMatrix,vu.getDate());
			
			logger.info("modello aggiornato con visitedUrls forniti...");
			
			/* ricerco un numero maggiore di tags per ogni url */
			/* ri-cerca dei tags, che poi vengono passati al GlobalModel*/
			
			Set<RankedTag> tagSet = new HashSet<RankedTag>();
			//uso una strategy che mi permetta l'estrazione di pi� tags
			TagFinder tagFinder = new TagFinder();
			tagSet = tagFinder.findTags(vu.getURL());
			URLTags url = new URLTags(vu, tagSet);
			urls.add(url);
				
		}
		
		/* l'aggiornamento dell'utente con tutti i suoi nuovi url � terminato. */

		/* passo gli url al GlobalProfileModel */
		
		GlobalProfileModel globalProfile = new GlobalProfileModel(urls);
		/* dopo che l'ho creato, posso salvare il GlobalProfileModel nel db 
		 * aggiungendo tutti gli url e le occorrenze come righe della tabella 
		 * tagsvisitedurl, usando per� gli id degli url e dei tags. */
		globalProfile.updateGlobalProfile();
		
		/* (potrei anticipare il passaggio degli url da usermodelupdater al 
		 * globalProfilemodel, passandoglieli senza tag. 
		 * se ne occuperebbe lui di estrarli, indipendentemente dai 
		 * calcoli massici di matrici che fa lo usermodelupdater.) 
		 * l'ho messo prima di tutti i calcoli*/
		
		
		
		/* tag co-occurrences 
		 * ora passo gli stessi urls ad un'altra classe che rappresenta le
		 * info da aggiornare per quanto riguarda il profilo personale delle
		 * co-occorrenze tag-tag */
		PersonalProfileModel personalProfile = new PersonalProfileModel(urls);
		personalProfile.updatePersonalProfile();
		
		
		
		/*
		//delete visited urls from db
		try{
			this.visitedURLHandler.deleteVisitedURLs(visitedURLs,user);
			logger.info("url visitati rimossi dal db.");
		} catch (PersistenceException e) {
			logger.info("ATTENZIONE: impossibile cancellare url visitati dal db. motivo: \n"+e.getMessage());
		}
		*/
	}

	private void updateUserModel(Map<String, Map<RankedTag, Map<String, Double>>> subMatrix, 
			Map<RankedTag, Map<String, Map<String, Double>>> tempMatrix, Set<String> terms, 
			Map<String, Map<RankedTag, Map<String, Double>>> newInsertMatrix, Map<String, Map<RankedTag, Map<String, Double>>> newUpdateMatrix) {
		
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("inizio update del modello utente con nuove informazioni.");
		
		for(String term1: terms) {
			logger.info("analizzo il termine: " + term1);
			Map<RankedTag, Map<String, Double>> newInsertValues4term = 
				new HashMap<RankedTag,Map<String,Double>>();
			Map<RankedTag, Map<String, Double>> newUpdateValues4term = 
				new HashMap<RankedTag,Map<String,Double>>();
			Set<RankedTag> tags4term = this.getTags4term(tempMatrix,term1);
			logger.info("tags associati al termine " + term1 + ": " + tags4term);
			for(RankedTag tag: tags4term) {
				//co-occurrence values retrieved from user matrix
				Map<String,Double> oldValues4term4tag = null;
				if(subMatrix.containsKey(term1))
					oldValues4term4tag = subMatrix.get(term1).get(tag);
				logger.info("vecchi valori di co-occorrenza relativi a termine=" + term1 +
						", tag=" + tag + "): \n" + oldValues4term4tag);
				Map<String,Double> values4term4tag = 
					tempMatrix.get(tag).get(term1);
				logger.info("valori di co-occorrenza appena calcolati relativi a termine=" + term1 +
						", tag=" + tag + "): \n" + values4term4tag);
				if(oldValues4term4tag==null) {
					RankedTag newTag = new RankedTag(tag.getTag());
					newTag.updateRanking(term1,values4term4tag);
					newInsertValues4term.put(newTag, values4term4tag);
					logger.info("il modello utente non conteneva termine e/o tag associato; " +
							"nella newInsertValues4term è stato inserito: \n{" + newTag + "=" +
							values4term4tag + "}");
				}
				else {
					Map<String,Double> newInsertValues4term4tag = 
						new HashMap<String,Double>();
					Map<String,Double> newUpdateValues4term4tag = 
						new HashMap<String,Double>();
					for(String term2: values4term4tag.keySet()) {
						double value = 
							values4term4tag.get(term2);
						if(!oldValues4term4tag.containsKey(term2))
							newInsertValues4term4tag.put(term2, value);
						else {
							double oldValue =
								oldValues4term4tag.get(term2);
							double newValue = oldValue + value;
							newUpdateValues4term4tag.put(term2,newValue);
						}
					}
					RankedTag newTag = new RankedTag(tag.getTag());
					newTag.updateRanking(term1,newInsertValues4term4tag,newUpdateValues4term4tag);
					if(!newInsertValues4term4tag.isEmpty())
						newInsertValues4term.put(newTag,newInsertValues4term4tag);
					if(!newUpdateValues4term4tag.isEmpty())
						newUpdateValues4term.put(newTag,newUpdateValues4term4tag);
				}
			}
			if(!newInsertValues4term.isEmpty())
				newInsertMatrix.put(term1, newInsertValues4term);
			if(!newUpdateValues4term.isEmpty())
				newUpdateMatrix.put(term1, newUpdateValues4term);
		}
	}

	private Set<RankedTag> getTags4term(Map<RankedTag, Map<String, Map<String, Double>>> matrix, String term) {
		Set<RankedTag> tags4term = 
			new HashSet<RankedTag> ();
		for(RankedTag tag: matrix.keySet()) {
			if(matrix.get(tag).containsKey(term))
				tags4term.add(tag);
		}
		return tags4term;
	}

	private Set<String> getTermSet(Map<RankedTag, Map<String, Map<String, Double>>> matrix) {
		Set<String> terms = new HashSet<String>();
		for(RankedTag tag: matrix.keySet()) {
			Set<String> terms4tag = matrix.get(tag).keySet();
			terms.addAll(terms4tag);
		}
		return terms;
	}

}
