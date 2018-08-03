package it.cnr.istc.stlab.mira.pdf;

import static it.cnr.istc.stlab.mira.pdf.AnvurPDFHeaders.NS_DATA;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class AnvurPDF {
	
	private PDDocument document;
	private int startPage, endPage, currentPage;
	private PDFTextStripper pdfStripper;
	private String applicationId, disciplinaryArea, level;
	private Applicant applicant;
	
	private URI currentSection, documentURI;
	
	public AnvurPDF(PDDocument document) {
		this.document = document;
		try {
			pdfStripper = new PDFTextStripper();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.startPage = 1;
		this.endPage = document.getNumberOfPages();
		getMetadata();
		
		this.currentPage = 2;
		
		documentURI = new URIImpl(NS_DATA + "resume/" + applicationId);
	}
	
	private void getMetadata(){
		pdfStripper.setStartPage(startPage);
		pdfStripper.setStartPage(startPage);
		
		try {
			String frontMatter = pdfStripper.getText(document);
			
			String[] lines = frontMatter.split("\\n");
			for(int i=1; i<lines.length; i++){
				String line = lines[i];
				if(i == 1) applicationId = line.replace("N. Progr. ", "").trim();
				else if(i==3) { 
					String applicantFullName = line.trim();
					this.applicant = new Applicant(applicantFullName);
				}
				else if(i==4) disciplinaryArea = line.replace("Settore Concorsuale domanda: ", "").trim();
				else if(i==5) level = line.trim();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public PDDocument getDocument() {
		return document;
	}
	
	public PDFTextStripper getPdfStripper() {
		return pdfStripper;
	}
	
	public int getStartPage() {
		return startPage;
	}
	
	public int getEndPage() {
		return endPage;
	}
	
	public int getCurrentPage() {
		return currentPage;
	}
	
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	
	public Applicant getApplicant() {
		return applicant;
	}
	
	public String getApplicationId() {
		return applicationId;
	}
	
	public String getDisciplinaryArea() {
		return disciplinaryArea;
	}
	
	public String getLevel() {
		return level;
	}
	
	public URI getCurrentSection() {
		return currentSection;
	}
	
	public void setCurrentSection(URI currentSection) {
		this.currentSection = currentSection;
	}
	
	public String getText(int startPage, int endPage){
		pdfStripper.setStartPage(startPage);
		pdfStripper.setEndPage(endPage);
		try {
			return pdfStripper.getText(document);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getTextOfPage(int pageNumber){
		return getText(pageNumber, pageNumber);
	}
	
	public URI getDocumentURI(){
		return documentURI;
	}

}
