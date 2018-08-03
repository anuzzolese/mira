package it.cnr.istc.stlab.mira.altmetrics.plum.scholarlydata;

import static it.cnr.istc.stlab.mira.altmetrics.plum.PlumScraper.COUNT;
import static it.cnr.istc.stlab.mira.altmetrics.plum.PlumScraper.METRICS;
import static it.cnr.istc.stlab.mira.altmetrics.plum.PlumScraper.SOURCES;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
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
import org.openrdf.rio.turtle.TurtleWriterFactory;

import com.bigdata.rdf.internal.XSD;

import au.com.bytecode.opencsv.CSVReader;
import it.cnr.istc.stlab.mira.altmetrics.plum.PlumScraper;
import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;
import it.cnr.istc.stlab.mira.commons.Urifier;
import it.cnr.istc.stlab.mira.pdf.AnvurPDFHeaders;

public class ScholarlyData {

	private static String indicatorNs = "https://w3id.org/scholarlydata/indicator/";
	private static String metricNs = "https://w3id.org/scholarlydata/metric/";
	private static String parameterNs = "https://w3id.org/scholarlydata/parameter/";
	private static String indicatorSourceNs = "https://w3id.org/scholarlydata/indicator-source/";
	private static String indicatorValueNs = "https://w3id.org/scholarlydata/indicator-value/";
	
	private static String indicatorOntologyNs = "https://w3id.org/scholarlydata/ontology/indicators-ontology.owl#";
	
	public Model addMetrics(URI article, String doi) {
		JSONArray jsonArray = PlumScraper.scrape(doi);
		return json2rdf(article, jsonArray); 
	}
	
	
	
	private Model json2rdf(URI article, JSONArray jsonArray){
		Model model = new LinkedHashModel();

		if(jsonArray.length() > 0){
			
			for(int i=0, j=jsonArray.length(); i<j; i++){
				try {
					JSONObject metrics = jsonArray.getJSONObject(i);
					json2rdfAux(0, article, null, null, null, metrics, model);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return model;
	}
	
	private Model json2rdfAux(int level, URI article, String parentIndicator, URI parentIndicatorURI, URI parentParameter, JSONObject metrics, Model model){
		
		String publicationId = article.stringValue().replace("https://w3id.org/scholarlydata/inproceedings/", "");
		publicationId = Urifier.toURI(publicationId);
		
		URI isIndicatorOf = new URIImpl(indicatorOntologyNs + "isIndicatorOf");
		URI hasIndicator = new URIImpl(indicatorOntologyNs + "hasIndicator");
		URI indicatorType = new URIImpl(indicatorOntologyNs + "Indicator");
		URI hasSource = new URIImpl(indicatorOntologyNs + "hasSource");
		URI basedOnMetric = new URIImpl(indicatorOntologyNs + "basedOnMetric");
		URI hasParameter = new URIImpl(indicatorOntologyNs + "hasParameter");
		URI hasIndicatorValue = new URIImpl(indicatorOntologyNs + "hasIndicatorValue");
		URI isIndicatorValueOf = new URIImpl(indicatorOntologyNs + "isIndicatorValueOf");
		URI hasSubIndicator = new URIImpl(indicatorOntologyNs + "hasSubIndicator");
		URI hasSuperIndicator = new URIImpl(indicatorOntologyNs + "hasSuperIndicator");
		URI specialises = new URIImpl(indicatorOntologyNs + "specialises");
		URI isSpecialiedBy = new URIImpl(indicatorOntologyNs + "isSpecialisedBy");
		URI indicatorValue = new URIImpl(indicatorOntologyNs + "indicatorValue");
		URI scopus = new URIImpl(indicatorSourceNs + "scopus");
		URI plumx = new URIImpl(indicatorSourceNs + "plumx");
		URI scopusCitationCount = new URIImpl(metricNs + "scopus-citation-count");
		URI citationCountParameter = new URIImpl(parameterNs + "citation-count");
		
		URI indcatorSourceType = new URIImpl(indicatorOntologyNs + "IndicatorSource");
		URI metricType = new URIImpl(indicatorOntologyNs + "Metric");
		URI parameterType = new URIImpl(indicatorOntologyNs + "Parameter");
		URI valueType = new URIImpl(indicatorOntologyNs + "IndicatorValue");
		
		model.add(scopus, RDF.TYPE, indcatorSourceType);
		model.add(scopus, RDFS.LABEL, new LiteralImpl("Scopus"));
		model.add(plumx, RDF.TYPE, indcatorSourceType);
		model.add(plumx, RDFS.LABEL, new LiteralImpl("PlumX"));
		model.add(scopusCitationCount, RDF.TYPE, metricType);
		model.add(scopusCitationCount, hasParameter, citationCountParameter);
		model.add(scopusCitationCount, RDFS.LABEL, new LiteralImpl("Scopus citation count"));
		model.add(citationCountParameter, RDF.TYPE, parameterType);
		model.add(citationCountParameter, RDFS.LABEL, new LiteralImpl("Citation count"));
		
		
		
		String categoyMetrics;
		try {
			if(metrics.has(METRICS) && metrics.has(COUNT)){
				categoyMetrics = metrics.getString(METRICS);
				int categoryCount = metrics.getInt(COUNT);
				if(categoyMetrics.equals("Citations")){
					URI indicator = new URIImpl(indicatorNs + publicationId + "-citation-count");
					model.add(indicator, isIndicatorOf, article);
					model.add(indicator, hasSource, scopus);
					model.add(indicator, basedOnMetric, scopusCitationCount);
					model.add(indicator, RDF.TYPE, indicatorType);;
					model.add(article, hasIndicator, indicator);
					
					URI value = new URIImpl(indicatorValueNs + publicationId + "-citation-count-value");
					model.add(indicator, hasIndicatorValue, value);
					model.add(value, RDF.TYPE, valueType);
					model.add(value, isIndicatorValueOf, indicator);
					model.add(value, indicatorValue, new LiteralImpl(String.valueOf(categoryCount), XSD.INT));	
					
					
				}
				else{
					
					String urifiedCategoryMetrics = Urifier.toURI(categoyMetrics);
					URI indicator = null;
					URI metric = null;
					URI value = null;
					URI parameter = null;
					String parameterLabel = null;
					
					if(level < 2){
						indicator = new URIImpl(indicatorNs + publicationId + "-plumx-" + urifiedCategoryMetrics);
						metric = new URIImpl(metricNs + "plumx-" + urifiedCategoryMetrics);
						value = new URIImpl(indicatorValueNs + publicationId + "-plumx-" + urifiedCategoryMetrics + "-value");
						parameter = new URIImpl(parameterNs + urifiedCategoryMetrics);
						model.add(indicator, hasSource, plumx);
						parameterLabel = categoyMetrics;
						if(parentParameter != null){
							model.add(parameter, specialises, parentParameter);
							model.add(parentParameter, isSpecialiedBy, parameter);
						}
					}
					else {
						indicator = new URIImpl(indicatorNs + publicationId + "-" + parentIndicator + "-" + urifiedCategoryMetrics);
						metric = new URIImpl(metricNs + "-" + parentIndicator + "-" + urifiedCategoryMetrics);
						value = new URIImpl(indicatorValueNs + publicationId + "-" + parentIndicator + "-" + urifiedCategoryMetrics + "-value");
						parameter = parentParameter;
						URI source = new URIImpl(indicatorSourceNs + urifiedCategoryMetrics);
						model.add(source, RDF.TYPE, indcatorSourceType);
						model.add(source, RDFS.LABEL, new LiteralImpl(categoyMetrics));
						model.add(indicator, hasSource, source);
					}
					
					if(parentIndicatorURI != null){
						model.add(indicator, hasSuperIndicator, parentIndicatorURI);
						model.add(parentIndicatorURI, hasSubIndicator, indicator);
					}
					
					model.add(indicator, isIndicatorOf, article);
					model.add(indicator, basedOnMetric, metric);
					model.add(indicator, RDF.TYPE, indicatorType);;
					model.add(article, hasIndicator, indicator);
					
					model.add(metric, RDF.TYPE, metricType);
					model.add(metric, hasParameter, parameter);
					
					model.add(parameter, RDF.TYPE, parameterType);
					
					if(parameterLabel != null)
						model.add(parameter, RDFS.LABEL, new LiteralImpl(parameterLabel));
					
					model.add(metric, hasParameter, parameter);
					
					model.add(indicator, hasIndicatorValue, value);
					model.add(value, RDF.TYPE, valueType);
					model.add(value, isIndicatorValueOf, indicator);
					model.add(value, indicatorValue, new LiteralImpl(String.valueOf(categoryCount), XSD.INT));
					
					
					if(!categoyMetrics.equals("Clicks")){
			    		if(metrics.has(SOURCES)){
				    		JSONArray parts = metrics.getJSONArray(SOURCES);
				    		for(int i=0, j=parts.length(); i<j; i++){
				    			JSONObject subMetrics = parts.getJSONObject(i);
				    			json2rdfAux(level+1, article, urifiedCategoryMetrics, indicator, parameter, subMetrics, model);
				    		}
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
	
	public static void main(String[] args) {
		
		ScholarlyData sd = new ScholarlyData();
		
		Model globalModel = new LinkedHashModel();
		
		String sparql = "PREFIX conf: <https://w3id.org/scholarlydata/ontology/conference-ontology.owl#> "
				+ "SELECT DISTINCT ?paper ?doi "
				+ "WHERE{ "
				+ "?paper conf:doi ?doi "
				+ "}";
		
		try {
			HttpURLConnection.setFollowRedirects(true);
			HttpURLConnection conn = (HttpURLConnection)new URL("https://w3id.org/scholarlydata/sparql?query=" + URLEncoder.encode(sparql, "UTF-8")).openConnection();
			conn.addRequestProperty("Accept", "text/csv");
			
			InputStream is = conn.getInputStream();
			
			String newUrl = conn.getHeaderField("Location");
			
			conn = (HttpURLConnection)new URL(newUrl).openConnection();
			conn.addRequestProperty("Accept", "text/csv");

			is = conn.getInputStream();
			
			CSVReader reader = new CSVReader(new InputStreamReader(is, "UTF-8"));
			
			//System.out.println("https://w3id.org/scholarlydata/sparql?query=" + URLEncoder.encode(sparql, "UTF-8"));
			
			String[] row = null;
			int rowCounter = 0;
			while((row = reader.readNext()) != null){
				// Skip header
				if(rowCounter > 0){
					String articleString = row[0];
					String doi = row[1];
					
					URI article = new URIImpl(articleString);
					
					Model model = sd.addMetrics(article, doi);
					
					System.out.println(rowCounter + ": " + articleString + " - " + doi);
					
					globalModel.addAll(model);
				}
				rowCounter++;
			}
			reader.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//OutputStream out = new FileOutputStream(outRDFFile);
		OutputStream out;
		try {
			out = new FileOutputStream("indicators-scholarlydata.ttl");
			
			RDFWriter writer = new TurtleWriterFactory().getWriter(out);
			
			try {
				writer.handleNamespace("indicator", indicatorNs);
				writer.handleNamespace("metric", metricNs);
				writer.handleNamespace("parameter", parameterNs);
				writer.handleNamespace("indicator-source", indicatorSourceNs);
				writer.handleNamespace("indicator-value", indicatorValueNs);
				writer.handleNamespace("sd-ind", indicatorOntologyNs);
				writer.handleNamespace("sd", "https://w3id.org/scholarlydata/ontology/conference-ontology.owl#");
				writer.handleNamespace("rdf", RDF.NAMESPACE);
				writer.handleNamespace("rdfs", RDFS.NAMESPACE);
				writer.handleNamespace("xsd", XSD.NAMESPACE);
				
				writer.startRDF();
				for(Statement stmt : globalModel)
					writer.handleStatement(stmt);
				writer.endRDF();
				
				out.close();
			} catch (RDFHandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		};
		
		
	}
}
