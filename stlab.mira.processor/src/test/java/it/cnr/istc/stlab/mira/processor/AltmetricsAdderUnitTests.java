package it.cnr.istc.stlab.mira.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.codehaus.jettison.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.turtle.TurtleParserFactory;

import it.cnr.istc.stlab.mira.altmetrics.AltmetricsModel;
import it.cnr.istc.stlab.mira.commons.PublicationWithDoi;
import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;

public class AltmetricsAdderUnitTests {
	
	private static Model model;
	private static File folderForExtensiveTesting;
	
	
	@BeforeClass
	public static void setUp(){
		RDFParser parser = new TurtleParserFactory().getParser();
		model = new LinkedHashModel();
		StatementCollector collector = new StatementCollector(model);
		parser.setRDFHandler(collector);
		folderForExtensiveTesting = new File("/Users/andrea/Desktop/anvur/sessione_1/step2-crossref/fascia-1");
		try {
			parser.parse(new FileInputStream(new File("/Users/andrea/Desktop/anvur/sessione_1/step2-crossref/fascia-1/01-B1/10562_SALOMONI_Paola.ttl")), QualificationProcessOntology.NS);
					
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void tearDown(){
		model = null;
	}
	
	@Test
	public void getPublicationWithDOIExtensiveTest(){
		AltmetricsAdder altmetricsAdder = new AltmetricsAdder();
		
		File[] disciplinaryFolders = folderForExtensiveTesting.listFiles(f -> f.isDirectory() && !f.isHidden());
		for(File disciplianryFolder : disciplinaryFolders){
			System.out.println(disciplianryFolder.getName());
			File[] ttls = disciplianryFolder.listFiles(f -> f.isFile() && !f.isHidden() && f.getName().endsWith(".ttl"));
			List<File> ttlsList = new ArrayList<File>();
			Collections.addAll(ttlsList, ttls);
			
			ttlsList.parallelStream()
				.map(ttl -> {
					Model model = new LinkedHashModel();
					RDFParser parser = new TurtleParserFactory().getParser();
					StatementCollector collector = new StatementCollector(model);
					parser.setRDFHandler(collector);
					try {
						parser.parse(new FileInputStream(ttl), QualificationProcessOntology.NS);
						Set<PublicationWithDoi> publicationsWithDOI = altmetricsAdder.getPublicationsWithDOI(model);
						//System.out.println('\t' + ttl.getName() + " " + publicationsWithDOI.size());
					} catch (RDFParseException | RDFHandlerException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return model;
				})
				.collect(Collectors.toList());
				
		}
		
	}
	
	@Test
	public void addAltmetricsTest(){
		AltmetricsAdder altmetricsAdder = new AltmetricsAdder();
		AltmetricsModel altmetricsModel = altmetricsAdder.addAltmetrics(model);
		try {
			System.out.println(altmetricsModel.getJsonObject().toString(4));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
