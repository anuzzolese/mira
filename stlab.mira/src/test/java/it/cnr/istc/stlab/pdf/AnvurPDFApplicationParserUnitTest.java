package it.cnr.istc.stlab.pdf;

import static it.cnr.istc.stlab.mira.pdf.AnvurPDFHeaders.ALL_PUBLICATIONS_HEADER;
import static it.cnr.istc.stlab.mira.pdf.AnvurPDFHeaders.NS_DATA;
import static it.cnr.istc.stlab.mira.pdf.AnvurPDFHeaders.QUALIFICATIONS_HEADER;
import static it.cnr.istc.stlab.mira.pdf.AnvurPDFHeaders.SELECTED_PUBLICATIONS_HEADER;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
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
import it.cnr.istc.stlab.mira.pdf.AnvurPDFApplicationParser;
import it.cnr.istc.stlab.mira.pdf.section.AnvurPDFApplicationSection;
import it.cnr.istc.stlab.mira.pdf.section.PDFSectionParser;
import it.cnr.istc.stlab.mira.pdf.section.SectionParserBuilder;

public class AnvurPDFApplicationParserUnitTest {
	
	private static AnvurPDFApplicationParser pdfApplicationParser;
	
	@BeforeClass
	public static void setUp(){
		pdfApplicationParser = new AnvurPDFApplicationParser();
	}
	
	@AfterClass
	public static void tearDown(){
		pdfApplicationParser = null;
	}
	
	@Test
	public void parse(){
		File inFile = new File("/Volumes/ANVUR/data2016/cv_candidati/sessione_1/fascia-2/01-B1/14048_PRESUTTI_Valentina.pdf");
		PDDocument document;
		try {
			document = PDDocument.load(inFile);
			Model model = pdfApplicationParser.parse(document);
			model.filter(null, RDF.TYPE, QualificationProcessOntology.Publication)
				.subjects()
				.forEach(publication -> {
					System.out.println(publication);
				});
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
