package model.usermodel.tags;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;


import org.htmlparser.Parser;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.ParserException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.*;

public class DeliciousSubUrlTagFinderXpathStrategy extends
		MD5SubUrlTagFinderStrategy {
	
	private static final String DELICIOUS_URL_STARTSWITH = "http://del.icio.us/url/";
	private static final String DELICIOUS_URL_ENDSWITH = "?settagview=list";
	

	public DeliciousSubUrlTagFinderXpathStrategy() {
		super(DELICIOUS_URL_STARTSWITH,DELICIOUS_URL_ENDSWITH);
	}

	@Override
	protected Map<String, Integer> extractTags(String pageContent) {
		//result
		Map<String,Integer> extractedTags = new HashMap<String,Integer> ();
		
		/* per ora delicious Ž fatto cos’: 
		 * ho un div id="top-tags"
		 * al cui interno c'Ž una 
		 * ul class="list"
		 * ogni li contenuto ha il tag al suo interno ed Ž formato cos’: 
		 * 
            <a href="/tag/search" title="0 (10201)" class="showTag">
            	<span class="m" title="0 (10201)">
            		search
            		<em>10201</em>
            	</span>
            </a>
          */
		
		/* da una stringa pageContent devo costruire una struttura html 
		 * sulla quale si possano applicare gli xpath */
	
		
		
		return extractedTags;
	}

}
