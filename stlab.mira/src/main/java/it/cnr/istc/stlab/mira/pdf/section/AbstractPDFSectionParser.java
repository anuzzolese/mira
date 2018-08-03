package it.cnr.istc.stlab.mira.pdf.section;

import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;

import it.cnr.istc.stlab.mira.pdf.AnvurPDF;
import it.cnr.istc.stlab.mira.pdf.Applicant;

public abstract class AbstractPDFSectionParser implements PDFSectionParser {
	
	protected String applicationId;
	protected Applicant appl;
	protected URI previousSection, previousRecord, document;
	protected Model model;
	protected AnvurPDF anvurPDF;
	
	AbstractPDFSectionParser(AnvurPDF anvurPDF) {
		this.anvurPDF = anvurPDF;
		this.applicationId = anvurPDF.getApplicationId();
		this.appl = anvurPDF.getApplicant();
		this.previousSection = anvurPDF.getCurrentSection();
		this.document = anvurPDF.getDocumentURI();
		this.model = new LinkedHashModel();
	}
	
	public String getText() {
		return anvurPDF.getTextOfPage(anvurPDF.getCurrentPage());
	}
	
	public String getApplicationId() {
		return applicationId;
	}
	
	public Applicant getAppl() {
		return appl;
	}
	
	public URI getPreviousSection() {
		return previousSection;
	}
	
	public URI getDocument() {
		return document;
	}
	
	@Override
	public URI getPreviousRecord() {
		return previousRecord;
	}

}
