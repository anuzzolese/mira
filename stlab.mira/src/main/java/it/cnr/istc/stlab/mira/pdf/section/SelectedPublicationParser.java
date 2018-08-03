package it.cnr.istc.stlab.mira.pdf.section;

import static it.cnr.istc.stlab.mira.pdf.AnvurPDFHeaders.NS_DATA;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import com.bigdata.rdf.internal.XSD;

import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;
import it.cnr.istc.stlab.mira.commons.Urifier;
import it.cnr.istc.stlab.mira.pdf.AnvurPDF;
import it.cnr.istc.stlab.mira.pdf.PDFSectionModel;

public class SelectedPublicationParser extends PublicationParser {

	
	SelectedPublicationParser(AnvurPDF anvurPDF) {
		super(anvurPDF);
		super.model = new LinkedHashModel();
		super.referenceList = new URIImpl(NS_DATA + "reference-list/" + applicationId + "_selected-publications");
		
		super.model.add(super.referenceList, RDF.TYPE, QualificationProcessOntology.ReferenceList);
		Literal literal = new LiteralImpl("Selected publications of " + appl.getFullName() + " (application " + applicationId + ")");
		model.add(this.referenceList, RDFS.LABEL, literal);
		
		model.add(document, QualificationProcessOntology.hasSelectedPublications, super.referenceList);
		model.add(document, QualificationProcessOntology.hasPart, super.referenceList);
		if(previousSection == null)
			model.add(document, QualificationProcessOntology.firstElement, super.referenceList);
		else model.add(previousSection, QualificationProcessOntology.nextElement, super.referenceList);
	}
	
	@Override
	public PDFSectionModel parse(){
		
		String regex = "[0-9]([0-9])? [0-9][0-9][0-9][0-9](.?)+";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(getText());
		
		int offset = -1;
		
		String id = null, year = null;
		
		while(matcher.find()){
			int start = matcher.start();
			int end = matcher.end();
			
			if(offset > 0) {
				String record = getText().substring(offset, start);
				
				String[] recordLines = record.split("\n");
				StringBuilder sb = new StringBuilder();
				String publicationType = recordLines[0];
				
				for(int i=1; i<recordLines.length; i++){
					if(sb.length() > 0) sb.append(" ");
					sb.append(recordLines[i]);
				}
				record = sb.toString();
				
				URI currentRecord = new URIImpl(NS_DATA + "bibliographic-item/selected_" + applicationId + "_" + id);
				model.addAll(processRecord(applicationId, record, id, previousRecord, referenceList));
				
				publicationType = NS_DATA + "bibliographic-item-type/" + Urifier.toURI(publicationType);
				URI publicationTypeResource = new URIImpl(publicationType); 
				model.add(new StatementImpl(publicationTypeResource, RDF.TYPE, QualificationProcessOntology.BibliographicItemType));
				model.add(new StatementImpl(publicationTypeResource, RDFS.LABEL, new LiteralImpl(publicationType)));
				model.add(new StatementImpl(currentRecord, QualificationProcessOntology.hasBibliographicItemType, publicationTypeResource));
				
				previousRecord = currentRecord; 
				
			}
			offset = end+1;
			
			String idYear = getText().substring(start, end);
			String[] idYearParts = idYear.split(" ");
			id = idYearParts[0];
			year = idYearParts[1];
			
			
		}
		
		String record = null;
		try{
			record = getText().substring(offset, getText().length());
		}
		catch(Exception e) {
			
		}
		
		if(record != null){
			model.addAll(processRecord(applicationId, record, id, previousRecord, referenceList));
			//statements.add(new StatementImpl(referenceList, hasLastPredicate, previousRecord));
			URI currentRecord = new URIImpl(NS_DATA + "bibliographic-item/selected_" + applicationId + "_" + id);
			previousRecord = currentRecord;
			
		}		
		
		return new PDFSectionModel(referenceList, model);
	
		
	}
	
	private static List<Statement> processRecord(String applicantsId, String record, String id, URI previousRecord, URI referenceList){
    	
    	URI person = new URIImpl(NS_DATA + "person/" + applicantsId);
    	
    	List<Statement> statements = new ArrayList<Statement>();
    	URI biblioItem = new URIImpl(NS_DATA + "bibliographic-item/selected_" + applicantsId + "_" + id);
		Statement stmt = new StatementImpl(biblioItem, RDF.TYPE, QualificationProcessOntology.BibliographicItem);
		statements.add(stmt);
		
		record = record.replace('\n', ' ').trim();
		Literal litearal = new LiteralImpl(record);
		stmt = new StatementImpl(biblioItem, QualificationProcessOntology.content, litearal);
		statements.add(stmt);
		
		if(previousRecord != null){
			stmt = new StatementImpl(previousRecord, QualificationProcessOntology.nextElement, biblioItem);
			statements.add(stmt);
			
			stmt = new StatementImpl(referenceList, QualificationProcessOntology.hasPart, biblioItem);
			statements.add(stmt);
		}
		else {
			stmt = new StatementImpl(referenceList, QualificationProcessOntology.firstElement, biblioItem);
			statements.add(stmt);
			
			stmt = new StatementImpl(referenceList, QualificationProcessOntology.hasPart, biblioItem);
			statements.add(stmt);
		}
    	
		URI publication = new URIImpl(NS_DATA + "publication/selected_" + applicantsId + "_" + id);
		statements.add(new StatementImpl(publication, RDF.TYPE, QualificationProcessOntology.Publication));
		statements.add(new StatementImpl(publication, QualificationProcessOntology.hasAuthor, person));
		statements.add(new StatementImpl(biblioItem, QualificationProcessOntology.refers, publication));
		
    	String doiMarker = "doi:";
    	int index = record.indexOf(doiMarker);
    	String doi = null;
    	if(index > 0){
    		doi = record.substring(index+doiMarker.length()).trim();
    		statements.add(new StatementImpl(publication, QualificationProcessOntology.doi, new LiteralImpl(doi)));
    	}	
    	
    	
    	Pattern pattern = Pattern.compile("\\([0-9][0-9][0-9][0-9]\\)\\.");
    	Matcher matcher = pattern.matcher(record);
    	String title = null;
    	if(matcher.find()){
    		
    		String authorString = record.substring(0, matcher.start()).trim();
    		statements.add(new StatementImpl(publication, QualificationProcessOntology.authorString, new LiteralImpl(authorString)));
    		
    		String year = record.substring(matcher.start()+1, matcher.end()-2);
    		statements.add(new StatementImpl(publication, QualificationProcessOntology.year, new LiteralImpl(year, XSD.GYEAR)));
    		
    		int start = matcher.end()+1;
    		
    		int end = record.indexOf(".", start);
    		title = record.substring(start, end).trim().replace('\n', ' ');
    		
    		statements.add(new StatementImpl(publication, QualificationProcessOntology.title, new LiteralImpl(title)));
    		statements.add(new StatementImpl(publication, RDFS.LABEL, new LiteralImpl(title)));
    		//System.out.println("TITLE: " + title);
    		
    		if(doi == null) {
    			/*
    			doi = queryScopus(title);
    			System.out.println("DOI from Scopus is " + doi);
    			*/
    		}
    	}
    	
    	return statements;
    }
	
}
