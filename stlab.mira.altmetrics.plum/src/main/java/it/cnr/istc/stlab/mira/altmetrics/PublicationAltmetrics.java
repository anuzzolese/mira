package it.cnr.istc.stlab.mira.altmetrics;

import org.codehaus.jettison.json.JSONArray;
import org.openrdf.model.Model;
import org.openrdf.model.URI;

public class PublicationAltmetrics {
	
	private URI publication;
	private JSONArray jsonArray;
	private Model model;
	
	public PublicationAltmetrics(URI publication, JSONArray jsonArray, Model model) {
		this.publication = publication;
		this.jsonArray = jsonArray;
		this.model = model;
	}
	
	public URI getPublication() {
		return publication;
	}
	
	public JSONArray getJsonArray() {
		return jsonArray;
	}
	
	public Model getModel() {
		return model;
	}
	
	
}