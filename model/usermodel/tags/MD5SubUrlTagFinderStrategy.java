package model.usermodel.tags;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import model.RankedTag;
import persistence.PersistenceException;
import util.LogHandler;
import util.ParameterHandler;

public abstract class MD5SubUrlTagFinderStrategy extends
		SubUrlTagFinderStrategy {
	
	private String urlStart;
	private String urlEnd;
	
	public MD5SubUrlTagFinderStrategy(String urlStart, String urlEnd) {
		this.urlStart = urlStart;
		this.urlEnd = urlEnd;
	}

	public Set<RankedTag> findTagsForSubUrl(String urlString, double relevance, boolean exactUrl) {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		
		List<String> equivalentUrls = new LinkedList<String> ();
		try {
			if(!exactUrl) {
				equivalentUrls.add(this.convertToMD5("http://" + urlString));
				equivalentUrls.add(this.convertToMD5("http://" + urlString + "/"));
				equivalentUrls.add(this.convertToMD5("http://www." + urlString));
				equivalentUrls.add(this.convertToMD5("http://www." + urlString + "/"));
			}
			else
				equivalentUrls.add(this.convertToMD5(urlString));
			//equivalentUrls.add(this.convertToMD5("http://www." + urlString + "/home.htm"));
			//equivalentUrls.add(this.convertToMD5("http://www." + urlString + "/home.html"));
			//equivalentUrls.add(this.convertToMD5("http://www." + urlString + "/index.html"));
			//equivalentUrls.add(this.convertToMD5("http://www." + urlString + "/index.htm"));
			//equivalentUrls.add(this.convertToMD5("http://" + urlString + "/home.htm"));
			//equivalentUrls.add(this.convertToMD5("http://" + urlString + "/home.html"));
			//equivalentUrls.add(this.convertToMD5("http://" + urlString + "/index.html"));
			//equivalentUrls.add(this.convertToMD5("http://" + urlString + "/index.htm"));
		} catch (NoSuchAlgorithmException e) {
			System.exit(1);
		} catch (UnsupportedEncodingException e) {
			System.exit(1);
		}


		Map<String,Integer> rankedTags = new TreeMap<String,Integer>();
		for(String equivalentUrl: equivalentUrls) {
			URL deliciousUrl = null;
			Scanner scanner = null;
			try {
				deliciousUrl = 
					new URL(this.urlStart + 
							equivalentUrl +
							this.urlEnd);
				logger.info("md5 url da analizzare: " + deliciousUrl);
			} catch (MalformedURLException e) {
				continue;
			}
			try {
				//fixing timeout
				URLConnection urlConnection = deliciousUrl.openConnection();
				urlConnection.setConnectTimeout(ParameterHandler.URL_TIMEOUT);
				scanner = new Scanner(urlConnection.getInputStream());
			} catch (IOException e) {
				logger.info("url timeout!");
				e.printStackTrace();
				continue;
			}
			StringBuffer pageContent = new StringBuffer();
			while(scanner.hasNextLine()) {
				pageContent.append(scanner.nextLine());
			}
			this.updateResult(rankedTags,pageContent.toString());
			logger.info("risultato parziale aggiornato: " + rankedTags);
			try {
				//to avoid floodind (ip banning! damn!)
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Set<RankedTag> normalizedRankedTags = 
			this.normalizeRanking(rankedTags,relevance);
		
		try {
			super.save(normalizedRankedTags);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		
		logger.info("tags normalizzati: " + normalizedRankedTags);
		
		return normalizedRankedTags;
	}
	
	private String convertToMD5(String urlString) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(urlString.getBytes("UTF-8")); 
		return this.toHexString(md5.digest());
	}

	private String toHexString (byte [] v) {
		StringBuffer sb = new StringBuffer ();
		byte n1, n2;
		for (int c = 0; c < v.length; c++) {
			n1 = (byte)((v[c] & 0xF0) >>> 4);
			n2 = (byte)((v[c] & 0x0F));
			sb.append (n1 >= 0xA ? (char)(n1 - 0xA + 'a') : (char)(n1 + '0'));
			sb.append (n2 >= 0xA ? (char)(n2 - 0xA + 'a') : (char)(n2 + '0'));
		}
		return sb.toString();
	}

}
