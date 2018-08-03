package it.cnr.istc.stlab.mira.pdf.section;

import it.cnr.istc.stlab.mira.pdf.AnvurPDF;

public class SectionParserBuilder {

	public static PDFSectionParser buildParser(AnvurPDF anvurPDF, AnvurPDFApplicationSection section){
		PDFSectionParser parser;
		
		switch (section) {
		case SELECTED_PUBLICATIONS:
			parser = new SelectedPublicationParser(anvurPDF);
			break;
		case ALL_PUBLICATIONS:
			parser = new PublicationParser(anvurPDF);
			break;
		case QUALIFICATION:
			parser = new QualificationParser(anvurPDF);
			break;
		default:
			parser = null;
			break;
		}
		
		return parser;
	}
	
}
