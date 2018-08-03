package it.cnr.istc.stlab.mira.scopus;

import java.net.MalformedURLException;
import java.net.URL;

public class ScopusAPIConfig {
	
	public static final String API_KEY = "5953888c807d52ee017df48501d3e598";
	public static final String API_ENDPOINT = "https://api.elsevier.com/content/abstract/citation-count";
	public static final String API_DOI_QUERY_PARAMETER = "doi";
	public static final String API_KEY_QUERY_PARAMETER = "apiKey";
	
	public static final String RESULTING_JSON_RESPONSE_KEY = "citation-count-response";
	public static final String RESULTING_JSON_DOCUMENT_KEY = "document";
	public static final String RESULTING_JSON_CITATIO_COUNT_KEY = "citation-count";
	
	public static URL buildUrlRequest(String doi){
		String urlString = API_ENDPOINT + 
				"?" + 
				API_DOI_QUERY_PARAMETER + "=" + doi + 
				"&" + 
				API_KEY_QUERY_PARAMETER + "=" + API_KEY;  
		try {
			return new URL(urlString);
		} catch (MalformedURLException e) {
			return null;
		}
	}

}
