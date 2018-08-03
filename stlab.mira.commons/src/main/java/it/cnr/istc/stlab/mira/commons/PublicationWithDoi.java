package it.cnr.istc.stlab.mira.commons;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;

public class PublicationWithDoi {
	private URI publication;
	private Literal doi;
	
	public PublicationWithDoi(URI publication, Literal doi) {
		this.publication = publication;
		this.doi = doi;
	}
	
	public URI getPublication() {
		return publication;
	}
	
	public Literal getDoi() {
		return doi;
	}
	
	
}