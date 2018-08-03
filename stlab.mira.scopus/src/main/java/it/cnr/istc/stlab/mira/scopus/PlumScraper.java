package it.cnr.istc.stlab.mira.scopus;

import java.io.IOException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.rdfxml.RDFXMLWriter;

import com.bigdata.rdf.internal.XSD;

import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;
import it.cnr.istc.stlab.mira.commons.Urifier;
import it.cnr.istc.stlab.mira.pdf.AnvurPDFHeaders;

public class PlumScraper {
	
	public static final String METRICS = "metrics";
	public static final String COUNT = "count";
	public static final String SOURCES = "sources";
	
	private static final String PLUM_API_ENDPOINT = "https://plu.mx/plum/a";
	private static final String DOI_QUERY_PARAM = "doi";
	/*
	public static Document renderPage(String url) {
        System.setProperty("phantomjs.binary.path", "libs/phantomjs"); // path to bin file. NOTE: platform dependent
        WebDriver ghostDriver = new PhantomJSDriver();
        try {
            ghostDriver.get(url);
            return Jsoup.parse(ghostDriver.getPageSource());
        } finally {
            ghostDriver.quit();
        }
    }
    */
	
	public static JSONArray scrape(String doi) {
        
		JSONArray jsonArray = null;
        try {
        	Document doc = Jsoup.connect(buildURLString(doi))
        		.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")
        		.referrer("http://www.google.com")
        		.timeout(10000)
        		.get();
        	
        	if(doc != null) jsonArray = doc2json(doc);
        	else jsonArray = new JSONArray();
		} catch (IOException e) {
			jsonArray = new JSONArray();
		}
        return jsonArray;
        
    }
	
	private static String buildURLString(String doi){
		return PLUM_API_ENDPOINT + "?" + DOI_QUERY_PARAM + "=" + doi;
	}
	
	public static Model json2rdf(URI publicationURI, JSONArray jsonArray){
		Model model = new LinkedHashModel();
	
		if(jsonArray.length() > 0){
			URI plumSource = new URIImpl(AnvurPDFHeaders.NS_DATA + "source/plum-api");
			model.add(plumSource, RDF.TYPE, QualificationProcessOntology.Source);
			model.add(plumSource, RDFS.LABEL, new LiteralImpl("PLUM API"));
			
			
			for(int i=0, j=jsonArray.length(); i<j; i++){
				try {
					JSONObject metrics = jsonArray.getJSONObject(i);
					json2rdfAux(publicationURI, null, metrics, model);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return model;
	}
	
	private static Model json2rdfAux(URI publicationURI, URI parentMetrics, JSONObject metrics, Model model){
		
		String publicationId = publicationURI.getLocalName();
		URI citationCountUM= new URIImpl(AnvurPDFHeaders.NS_DATA + "unit-of-measure/citation-count");
		
		String categoyMetrics;
		try {
			if(metrics.has(METRICS) && metrics.has(COUNT)){
				categoyMetrics = metrics.getString(METRICS);
				int categoryCount = metrics.getInt(COUNT);
				String urifiedCategoryMetrics = Urifier.toURI(categoyMetrics);
				
				URI altmetricsCountType = new URIImpl(AnvurPDFHeaders.NS_DATA + "citation-count-type/" + urifiedCategoryMetrics);
				model.add(altmetricsCountType, RDF.TYPE, QualificationProcessOntology.CitationCountType);
		    	model.add(altmetricsCountType, RDFS.LABEL, new LiteralImpl(categoyMetrics));
		    	
		    	URI altmetricsCount = new URIImpl(AnvurPDFHeaders.NS_DATA + "citation-count/" + publicationId + "_" + urifiedCategoryMetrics);
				model.add(altmetricsCount, RDF.TYPE, QualificationProcessOntology.CitationCount);
		    	model.add(altmetricsCount, RDFS.LABEL, new LiteralImpl(categoyMetrics));
	
		    	model.add(altmetricsCount, QualificationProcessOntology.hasCitationCountType, altmetricsCountType);
		    	model.add(altmetricsCount, QualificationProcessOntology.scoreValue, new LiteralImpl(String.valueOf(categoryCount), XSD.INT));
		    	model.add(altmetricsCount, QualificationProcessOntology.hasUnitOfMeasure, citationCountUM);
		    	
		    	if(parentMetrics != null) 
		    		model.add(parentMetrics, QualificationProcessOntology.hasPart, altmetricsCount);
		    	else model.add(publicationURI, QualificationProcessOntology.hasCitationCount, altmetricsCount);
		    	
		    	if(!categoyMetrics.equals("Clicks")){
		    		if(metrics.has(SOURCES)){
			    		JSONArray parts = metrics.getJSONArray(SOURCES);
			    		for(int i=0, j=parts.length(); i<j; i++){
			    			JSONObject subMetrics = parts.getJSONObject(i);
			    			json2rdfAux(publicationURI, altmetricsCount, subMetrics, model);
			    		}
		    		}
		    	}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return model;
		
		
	}
	
	private static JSONArray doc2json(Document doc){

		JSONArray jsonArray = new JSONArray();
		if(doc != null){
			doc.getElementsByClass("metrics-data").forEach(elem -> {
				Elements metricGroup = elem.select(".metric-group");
				if(metricGroup != null){
					metricGroup.forEach(mg -> {
						JSONObject obj = new JSONObject();
						mg.select("dt.metric-title").forEach(dt -> {
							String metrics = null;
							Elements macro = dt.select("span.metric-name");
							if(macro != null && macro.size() > 0)
								metrics = macro.text().trim();
							
							Elements value = dt.select("span.metric-total");
							String countString = null;
							if(value != null && value.size() > 0)
								countString =  value.text().trim();
							
							if(metrics != null && countString != null){
								try {
									int count = Integer.valueOf(countString);
									obj.put(METRICS, metrics);
									obj.put(COUNT, count);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});
						
						JSONArray sources = new JSONArray();
						mg.select("dd.metric-sources").forEach(dt -> {
							JSONObject sourceJson = new JSONObject();
							Elements macro = dt.select(".expander span.group-name");
							String metrics = null;
							if(macro != null && macro.size() > 0)
								metrics  = macro.text().trim();
							
							String countString = null;
							Elements value = dt.select(".expander span.count");
							if(value != null && value.size() > 0)
								countString =  value.text().trim();
							
							if(metrics != null && countString != null){
								try {
									int count = Integer.valueOf(countString);
									sourceJson.put(METRICS, metrics);
									sourceJson.put(COUNT, count);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
								
							
							JSONArray subSources = new JSONArray();
							dt.select(".collapser").forEach(collaps -> {
								JSONObject subSourceJson = new JSONObject();
								Elements e = collaps.select("span.count-name");
								
								String subMetrics = null;
								if(e != null && e.size() > 0){
									String text = e.text();
									int index = text.indexOf("{'name'");
									if(index > 0)
										text = text.substring(0, index);
									subMetrics = text.trim();
								}
								
								String subCountString = null;
								e = collaps.select("span.count");
								if(e != null && e.size() > 0)
									subCountString = e.text().trim();
								
								if(subMetrics != null && subCountString != null){
									
									try {
										int count = Integer.valueOf(subCountString);
										subSourceJson.put(METRICS, subMetrics);
										subSourceJson.put(COUNT, count);
										
										subSources.put(subSourceJson);
									} catch (Exception e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								}
							});
							
							try {
								sourceJson.put(SOURCES, subSources);
								sources.put(sourceJson);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						});
						
						try {
							obj.put(SOURCES, sources);
							jsonArray.put(obj);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});
				}
				
			});
		}
		
		return jsonArray;
	
	}
	
	/*public static Document renderPage(Document doc) {
        String tmpFileName = "$filePath${Calendar.getInstance().timeInMillis}.html";
        FileUtils.writeToFile(tmpFileName, doc.toString());
        return renderPage(tmpFileName);
    }*/
	
	public static void main(String[] args) {
		
		JSONArray jsonArray = PlumScraper.scrape("10.1371/journal.pone.0056");
		System.out.println(jsonArray.toString());
		
		Model model = PlumScraper.json2rdf(new URIImpl("https://plu.mx/plum/a/?doi=10.1371/journal.pone.0056506"), jsonArray);
		RDFWriter writer = new RDFXMLWriter(System.out);
		try {
			writer.startRDF();
			for(Statement stmt : model){
				writer.handleStatement(stmt);
			}
			writer.endRDF();
			
		} catch (RDFHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
