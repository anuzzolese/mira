package it.cnr.istc.stlab.mira.commons.ciatationmodel;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import com.bigdata.rdf.internal.XSD;

import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;
import it.cnr.istc.stlab.mira.commons.Urifier;

public class MetricsJSONSchema {

	public static final String METRICS = "metrics";
	public static final String COUNT = "count";
	public static final String SOURCES = "sources";
	
	public static final URI scopusSource = new URIImpl(QualificationProcessOntology.NAMESPACE_DATA + "source/scopus");
	
	public static Model asRDF(URI publicationURI, JSONObject citationJsonObject){
		Model model = new LinkedHashModel();
		
		String publicationKey = publicationURI.getLocalName();
		
		JSONArray jsonArray = new JSONArray();
		try {
			jsonArray = citationJsonObject.getJSONArray(publicationKey);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		if(jsonArray.length() > 0){
			/*
			URI plumSource = new URIImpl(QualificationProcessOntology.NAMESPACE_DATA + "source/plum-api");
			model.add(plumSource, RDF.TYPE, QualificationProcessOntology.Source);
			model.add(plumSource, RDFS.LABEL, new LiteralImpl("PLUM API"));
			*/
			
			
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
		URI citationCountUM= new URIImpl(QualificationProcessOntology.NAMESPACE_DATA + "unit-of-measure/citation-count");
		
		String categoyMetrics;
		try {
			if(metrics.has(MetricsJSONSchema.METRICS) && metrics.has(MetricsJSONSchema.COUNT)){
				categoyMetrics = metrics.getString(MetricsJSONSchema.METRICS);
				int categoryCount = metrics.getInt(MetricsJSONSchema.COUNT);
				String urifiedCategoryMetrics = Urifier.toURI(categoyMetrics);
				
				URI altmetricsCountType = new URIImpl(QualificationProcessOntology.NAMESPACE_DATA + "citation-count-type/" + urifiedCategoryMetrics);
				model.add(altmetricsCountType, RDF.TYPE, QualificationProcessOntology.CitationCountType);
		    	model.add(altmetricsCountType, RDFS.LABEL, new LiteralImpl(categoyMetrics));
		    	
		    	
		    	URI altmetricsCount = new URIImpl(QualificationProcessOntology.NAMESPACE_DATA + "citation-count/" + publicationId + "_" + urifiedCategoryMetrics);
				model.add(altmetricsCount, RDF.TYPE, QualificationProcessOntology.CitationCount);
		    	model.add(altmetricsCount, RDFS.LABEL, new LiteralImpl(categoyMetrics));
		    	
		    	model.add(altmetricsCount, QualificationProcessOntology.hasSource, scopusSource);
	
		    	model.add(altmetricsCount, QualificationProcessOntology.hasCitationCountType, altmetricsCountType);
		    	model.add(altmetricsCount, QualificationProcessOntology.scoreValue, new LiteralImpl(String.valueOf(categoryCount), XSD.INT));
		    	model.add(altmetricsCount, QualificationProcessOntology.hasUnitOfMeasure, citationCountUM);
		    	
		    	if(parentMetrics != null) 
		    		model.add(parentMetrics, QualificationProcessOntology.hasPart, altmetricsCount);
		    	else model.add(publicationURI, QualificationProcessOntology.hasCitationCount, altmetricsCount);
		    	
		    	if(!categoyMetrics.equals("Clicks")){
		    		if(metrics.has(MetricsJSONSchema.SOURCES)){
			    		JSONArray parts = metrics.getJSONArray(MetricsJSONSchema.SOURCES);
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
	
}
