package it.cnr.istc.stlab.mira.pdf.section;

import org.openrdf.model.URI;

import it.cnr.istc.stlab.mira.pdf.PDFSectionModel;

public interface PDFSectionParser {

	PDFSectionModel parse();
	
	URI getSection();
	
	URI getPreviousRecord();
	
}
