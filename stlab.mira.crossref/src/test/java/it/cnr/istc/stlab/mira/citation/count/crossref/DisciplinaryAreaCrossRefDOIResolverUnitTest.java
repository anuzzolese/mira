package it.cnr.istc.stlab.mira.citation.count.crossref;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.turtle.TurtleParserFactory;

import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;
import it.cnr.istc.stlab.mira.crossref.DisciplinaryAreaCrossRefDOIResolver;
import it.cnr.istc.stlab.mira.crossref.DisciplinaryAreaCrossRefDOIResolver.PublicationWithDoi;

public class DisciplinaryAreaCrossRefDOIResolverUnitTest {
	
	private static DisciplinaryAreaCrossRefDOIResolver doiResolver;
	private static Model model;
	
	@BeforeClass
	public static void setUp(){
		doiResolver = new DisciplinaryAreaCrossRefDOIResolver();
		RDFParser parser = new TurtleParserFactory().getParser();
		model = new LinkedHashModel();
		StatementCollector collector = new StatementCollector(model);
		parser.setRDFHandler(collector);
		
		try {
			parser.parse(new FileInputStream(new File("/Users/andrea/Desktop/anvur/sessione_1/data_altmetrics/rdf/fascia-1/01-B1/10562_SALOMONI_Paola.ttl")), QualificationProcessOntology.NS);
					
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void tearDown(){
		doiResolver = null;
		model = null;
	}
	
	@Test
	public void listPublicationsWithoutDOI(){
		List<Resource> resources = doiResolver.findPublicationsWithoutDoi(model);
		resources.forEach(resource -> {
			System.out.println(resource);
		});
	}
	
	@Test
	public void resolveDOIs(){
		List<PublicationWithDoi> publicationsWithDoi = doiResolver.resolveDOIs(model);
		publicationsWithDoi.forEach(publicationWithDoi -> {
			System.out.println(publicationWithDoi.getPublication() + " - " + publicationWithDoi.getDoi());
		});
	}
	
	@Test
	public void addMissingDOIs(){
		
		doiResolver.addMissingDOIs(model);
		
		model.filter(null, QualificationProcessOntology.doi, null)
			.forEach(stmt -> {System.out.println(stmt);});
	}
}
