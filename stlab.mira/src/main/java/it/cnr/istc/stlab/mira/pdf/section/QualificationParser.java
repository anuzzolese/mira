package it.cnr.istc.stlab.mira.pdf.section;

import static it.cnr.istc.stlab.mira.pdf.AnvurPDFHeaders.NS_DATA;

import java.io.IOException;

import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;
import it.cnr.istc.stlab.mira.commons.Urifier;
import it.cnr.istc.stlab.mira.pdf.AnvurPDF;
import it.cnr.istc.stlab.mira.pdf.MyTextStripper;
import it.cnr.istc.stlab.mira.pdf.PDFSectionModel;

public class QualificationParser extends AbstractPDFSectionParser {

	private URI qualificationList;
	
	QualificationParser(AnvurPDF anvurPDF) {
		super(anvurPDF);
		this.qualificationList = new URIImpl(NS_DATA + "qualification-list/" + applicationId);
		
		model.add(qualificationList, RDF.TYPE, QualificationProcessOntology.QualificationList);
		model.add(qualificationList, RDFS.LABEL, new LiteralImpl("Qualification list of " + appl.getFullName() + " (application #" + applicationId + ")."));
		
		model.add(anvurPDF.getDocumentURI(), QualificationProcessOntology.hasPart, qualificationList);
		if(anvurPDF.getCurrentSection() == null) 
			model.add(anvurPDF.getDocumentURI(), QualificationProcessOntology.firstElement, qualificationList);
		else{
			model.add(anvurPDF.getCurrentSection(), QualificationProcessOntology.nextElement, qualificationList);
		}
		
		anvurPDF.setCurrentSection(qualificationList);
	}

	@Override
	public PDFSectionModel parse() {
		
		MyTextStripper stripper;
		try {
			stripper = new MyTextStripper();
			
			stripper.setStartPage(anvurPDF.getCurrentPage());
			stripper.setEndPage(anvurPDF.getEndPage());
			
			
			URI[] previousDocumentPart = {null};
			stripper.getTitles(anvurPDF.getDocument()).forEach((title, content) -> {
				
				String titleUri = Urifier.toURI(title);
				URI qualificationType = new URIImpl(NS_DATA + "qualification-type/" + titleUri);
				model.add(qualificationType, RDF.TYPE, QualificationProcessOntology.QualificationType);
				model.add(qualificationType, RDFS.LABEL, new LiteralImpl(title));
				
				int[] i = {0};
				
				content.forEach(tit -> {
					int element = i[0] + 1;
					
					tit = tit.replace('\n', ' ').trim();
					
					URI candidateQualification = new URIImpl(NS_DATA + "qualification/" + anvurPDF.getApplicationId() + "_" + titleUri + "_" + element);
					model.add(candidateQualification, RDF.TYPE, QualificationProcessOntology.Qualification);
					model.add(candidateQualification, RDFS.LABEL, new LiteralImpl(anvurPDF.getApplicant() + ": " + element + ". " + title));
					model.add(candidateQualification, QualificationProcessOntology.hasQualificationType, qualificationType);
					model.add(candidateQualification, QualificationProcessOntology.content, new LiteralImpl(tit));
					
					model.add(qualificationList, QualificationProcessOntology.hasPart, candidateQualification);
					if(previousDocumentPart[0] == null) 
						model.add(qualificationList, QualificationProcessOntology.firstElement, candidateQualification);
					else{
						model.add(previousDocumentPart[0], QualificationProcessOntology.nextElement, candidateQualification);
					}
					
					i[0] = element;
					previousDocumentPart[0] = candidateQualification;
				});
			});
			
			model.add(qualificationList, QualificationProcessOntology.lastElement, previousDocumentPart[0]);
			
			anvurPDF.setCurrentPage(anvurPDF.getEndPage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return new PDFSectionModel(qualificationList, model);
	}
	
	@Override
	public URI getSection() {
		return qualificationList;
	}

}
