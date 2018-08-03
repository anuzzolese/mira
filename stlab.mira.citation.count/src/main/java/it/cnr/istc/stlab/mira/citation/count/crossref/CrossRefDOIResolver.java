package it.cnr.istc.stlab.mira.citation.count.crossref;


import static it.cnr.istc.stlab.mira.citation.count.crossref.CrossRefJSONSchema.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;

import com.bigdata.rdf.internal.XSD;

import it.cnr.istc.stlab.mira.commons.DOIResolver;
import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;

public class CrossRefDOIResolver implements DOIResolver {
	
	private final String CROSSREF_API_ENDPOINT = "https://api.crossref.org";
	private final String CROSSREF_API_PATH = "/works";
	private final String CROSSREF_API_SORT_QUERY_PARAM = "sort";
	private final String CROSSREF_API_QUERY_QUERY_PARAM = "query";
	private final String CROSSREF_API_ROWS_PARAM = "rows";

	public CrossRefDOIResolver() {
		
	}
	
	@Override
	public String resolve(Object bibliographicRecord) {
		String doi = null;
		if(bibliographicRecord instanceof Model){
			Model bibliographicRecordModel = (Model) bibliographicRecord;
			Literal titleLiteral = bibliographicRecordModel.filter(null, QualificationProcessOntology.title, null).objectLiteral();
			Literal authorStringLiteral = bibliographicRecordModel.filter(null, QualificationProcessOntology.authorString, null).objectLiteral();
			Literal yearLiteral = bibliographicRecordModel.filter(null, QualificationProcessOntology.year, null).objectLiteral();
			
			String title = "";
			String authorString = "";
			String year = "";
			if(titleLiteral != null) title = titleLiteral.getLabel().trim();
			if(authorStringLiteral != null) authorString = authorStringLiteral.getLabel().trim();
			if(yearLiteral != null) year = yearLiteral.getLabel().trim();
			
			try {
				String queryString = buildQueryString(title, authorString, year);
				
				System.out.println("QS: " + queryString);
				URLConnection connection = new URL(queryString).openConnection();
				InputStream is = connection.getInputStream();
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				StringBuilder sb = new StringBuilder();
				String line = null;
				while((line = reader.readLine()) != null)
					sb.append(line);
				
				JSONObject jsonObject = new JSONObject(sb.toString());
				if(jsonObject != null){
					if(jsonObject.has(STATUS)){
						String status = jsonObject.getString(STATUS);
						if(status.equals("ok")){
							if(jsonObject.has(MESSAGE)){
								JSONObject message = jsonObject.getJSONObject(MESSAGE);
								if(message.has(ITEMS)){
									JSONArray items = message.getJSONArray(ITEMS);
									boolean found = false;
									for(int i=0, j=items.length(); i<j && !found; i++){
										JSONObject item = items.getJSONObject(i);
										if(item.has(TITLE)){
											JSONArray titles = item.getJSONArray(TITLE);
											
											for(int k=0, z=titles.length(); k<z && !found; k++){
												String tit = titles.getString(k);
												if(tit.equalsIgnoreCase(title))
													found = true;
											}
										}
										if(found && item.has(DOI))
											doi = item.getString(DOI);
									}
								}
							}
						}
						
					}
				}
				
				is.close();
				
			} catch (EmptyQueryStringException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
		}
		return doi;
	}
	
	private String buildQueryString(String title, String authorString, String year) throws EmptyQueryStringException {
		
		String queryString;
		if(authorString.isEmpty() && year.isEmpty() && title.isEmpty())
			throw new EmptyQueryStringException();
		else {
			
			try {
				queryString = URLEncoder.encode(authorString +" (" + year + "). " + title, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new EmptyQueryStringException();
			}
		}
		
		StringBuilder apiStringBuilder = new StringBuilder(); 
		apiStringBuilder.append(CROSSREF_API_ENDPOINT)
			.append(CROSSREF_API_PATH)
			.append("?")
			.append(CROSSREF_API_SORT_QUERY_PARAM)
			.append("=score&")
			.append(CROSSREF_API_QUERY_QUERY_PARAM)
			.append("=")
			.append(queryString)
			.append("&")
			.append(CROSSREF_API_ROWS_PARAM)
			.append("=10"); 
		
		
		
		return apiStringBuilder.toString();
	}
	
	public static void main(String[] args) {
		
		DOIResolver doiResolver = new CrossRefDOIResolver();
		
		URI publication = new URIImpl("http://w3id.org/anvur/nq/data/publication/217_1");
		Model model = new LinkedHashModel();
		model.add(publication, QualificationProcessOntology.authorString, new LiteralImpl("Saettler Aline, Laber Eduardo, Cicalese Ferdinando"));
		model.add(publication, QualificationProcessOntology.year, new LiteralImpl("2016", XSD.GYEAR));
		model.add(publication, QualificationProcessOntology.title, new LiteralImpl("Trading Off Worst and Expected Cost in Decision Tree Problems"));
		
		String doi = doiResolver.resolve(model);
		System.out.println(doi);
	}

	
	
}
