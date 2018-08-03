package it.cnr.istc.stlab.mira.pdf;

import org.openrdf.model.Model;
import org.openrdf.model.URI;

public class PDFSectionModel {

	private URI section;
	private Model model;
	
	public PDFSectionModel(URI section, Model model) {
		this.section = section;
		this.model = model;
	}
	
	public URI getSection() {
		return section;
	}
	
	public Model getModel() {
		return model;
	}
	
	
	
}
