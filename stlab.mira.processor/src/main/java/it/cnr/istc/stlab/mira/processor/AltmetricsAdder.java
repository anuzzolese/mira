package it.cnr.istc.stlab.mira.processor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;

import it.cnr.istc.stlab.mira.altmetrics.AltmetricsModel;
import it.cnr.istc.stlab.mira.altmetrics.PublicationAltmetrics;
import it.cnr.istc.stlab.mira.altmetrics.plum.PlumScraper;
import it.cnr.istc.stlab.mira.commons.PublicationWithDoi;
import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;

public class AltmetricsAdder {

	public AltmetricsAdder() {
		
	}
	
	public Set<PublicationWithDoi> getPublicationsWithDOI(Model model){
		Resource publicationsList = model.filter(null, QualificationProcessOntology.hasPublications, null).objectResource();
		return model.filter(publicationsList, QualificationProcessOntology.hasPart, null)
				.objects()
				.stream()
				.map(biblioItem -> {
					return model.filter((URI) biblioItem, QualificationProcessOntology.refers, null).objectResource();
				})
				
				.map(publication -> {
					Literal doi = null;
					try{
						doi = model.filter((URI) publication, QualificationProcessOntology.doi, null).objectLiteral();
					} catch(Exception e){
						System.err.println("Error " + publication);
						//System.exit(-1);
					}
					//System.out.println('\t' + ((URI)publication).toString());
					return new PublicationWithDoi((URI) publication, doi);
				})
				.filter(publicationWithDoi -> publicationWithDoi.getDoi() != null)
				.collect(Collectors.toSet());
	}
	
	public AltmetricsModel addAltmetrics(Model model){
		
		Set<PublicationWithDoi> publicationWithDois = getPublicationsWithDOI(model);
		
		List<PublicationAltmetrics> publicationAltmetrics = publicationWithDois.stream()
				.map(publicationWithDoi -> {
					JSONArray jsonArray = PlumScraper.scrape(publicationWithDoi.getDoi().getLabel());
					Model altmetricsModel = PlumScraper.json2rdf(publicationWithDoi.getPublication(), jsonArray);
					return new PublicationAltmetrics(publicationWithDoi.getPublication(), jsonArray, altmetricsModel);
				})
				.collect(Collectors.toList());
		
		Model outModel = new LinkedHashModel();
		JSONObject outJson = new JSONObject();
		publicationAltmetrics.forEach(pubAltmetrics -> {
			outModel.addAll(pubAltmetrics.getModel());
			try {
				outJson.put(pubAltmetrics.getPublication().getLocalName(), pubAltmetrics.getJsonArray());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		outModel.addAll(model);
		
		return new AltmetricsModel(outJson, outModel);
	}
	
}
