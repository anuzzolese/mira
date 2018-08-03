package it.cnr.istc.stlab.mira.pdf;

import static it.cnr.istc.stlab.mira.pdf.AnvurPDFHeaders.ALL_PUBLICATIONS_HEADER;
import static it.cnr.istc.stlab.mira.pdf.AnvurPDFHeaders.NS_DATA;
import static it.cnr.istc.stlab.mira.pdf.AnvurPDFHeaders.QUALIFICATIONS_HEADER;
import static it.cnr.istc.stlab.mira.pdf.AnvurPDFHeaders.SELECTED_PUBLICATIONS_HEADER;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;
import it.cnr.istc.stlab.mira.commons.Urifier;
import it.cnr.istc.stlab.mira.pdf.section.AnvurPDFApplicationSection;
import it.cnr.istc.stlab.mira.pdf.section.PDFSectionParser;
import it.cnr.istc.stlab.mira.pdf.section.SectionParserBuilder;

public class AnvurPDFApplicationParser {
	
	public static Model parse(PDDocument document){
		
		AnvurPDF anvurPDF = new AnvurPDF(document);
		
		String applicationId = anvurPDF.getApplicationId();
		Applicant appl = anvurPDF.getApplicant();
		
		Model model = new LinkedHashModel();
		URI application = new URIImpl(NS_DATA + "application/" + applicationId);
		model.add(application, RDF.TYPE, QualificationProcessOntology.Application);
		
		URI qualificationLevel = new URIImpl(NS_DATA + "qualification-level/" + Urifier.toURI(anvurPDF.getLevel()));
		model.add(qualificationLevel, RDF.TYPE, QualificationProcessOntology.QualificationLevel);
		model.add(application, QualificationProcessOntology.forQualificaitonLevel, qualificationLevel);
		
		URI subjectArea = new URIImpl(NS_DATA + "qualification-level/" + Urifier.toURI(anvurPDF.getDisciplinaryArea()));
		model.add(subjectArea, RDF.TYPE, QualificationProcessOntology.SubjectArea);
		model.add(application, QualificationProcessOntology.forSubjectArea, subjectArea);
		
		Literal label = new LiteralImpl("Application " + applicationId + " submitted by " + appl.getFullName());
		model.add(application, RDFS.LABEL, label);
		
		URI person = new URIImpl(NS_DATA + "person/" + applicationId);
		model.add(person, RDFS.LABEL, label);
		model.add(person, QualificationProcessOntology.name, new LiteralImpl(appl.getFullName()));
		model.add(person, QualificationProcessOntology.givenName, new LiteralImpl(appl.getGivenName()));
		model.add(person, QualificationProcessOntology.familyName, new LiteralImpl(appl.getFamilyName()));
		
		model.add(application, QualificationProcessOntology.isApplicationOf, person);
		
		
		model.add(anvurPDF.getDocumentURI(), RDF.TYPE, QualificationProcessOntology.Document);
		label = new LiteralImpl("Resume of " + appl.getFullName() + "(application " + applicationId + ")");
		URI resumeType = new URIImpl(NS_DATA + "resume");
		model.add(anvurPDF.getDocumentURI(), QualificationProcessOntology.hasDocumentType, resumeType);
		model.add(application, QualificationProcessOntology.hasDocument, anvurPDF.getDocumentURI());
		
		PDFSectionParser pdfSectionParser = null;
		
		while(anvurPDF.getCurrentPage() < anvurPDF.getEndPage()){
			
			String text = anvurPDF.getTextOfPage(anvurPDF.getCurrentPage());
			
			String header = text.split("\n")[1];
			
			
			if(pdfSectionParser != null){
				URI previousRecord = pdfSectionParser.getPreviousRecord();
				if(previousRecord != null){
					model.add(pdfSectionParser.getSection(), QualificationProcessOntology.lastElement, previousRecord);
				}
				
			}
			
			if(header.equals(ALL_PUBLICATIONS_HEADER))
				pdfSectionParser = SectionParserBuilder.buildParser(anvurPDF, AnvurPDFApplicationSection.ALL_PUBLICATIONS);
			else if(header.equals(QUALIFICATIONS_HEADER))
				pdfSectionParser = SectionParserBuilder.buildParser(anvurPDF, AnvurPDFApplicationSection.QUALIFICATION);
			else if(header.equals(SELECTED_PUBLICATIONS_HEADER))
				pdfSectionParser = SectionParserBuilder.buildParser(anvurPDF, AnvurPDFApplicationSection.SELECTED_PUBLICATIONS);
				
			
			PDFSectionModel sectionModel = pdfSectionParser.parse();
			
			model.addAll(sectionModel.getModel());
			anvurPDF.setCurrentPage(anvurPDF.getCurrentPage() + 1);
			anvurPDF.setCurrentSection(sectionModel.getSection());
			
			
		}
		anvurPDF.setCurrentPage(anvurPDF.getCurrentPage() + 1);
		
		if(pdfSectionParser != null){
			URI lastSection = pdfSectionParser.getSection();
			model.add(anvurPDF.getDocumentURI(), QualificationProcessOntology.lastElement, lastSection);
		}
		
		return model;
	}
}
