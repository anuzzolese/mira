package it.cnr.istc.stlab.mira.scopus;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
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

import it.cnr.istc.stlab.mira.analytics.CitationMatrix;
import it.cnr.istc.stlab.mira.analytics.CorrelationMatrix;
import it.cnr.istc.stlab.mira.analytics.PersonLevelAnalyzer;
import it.cnr.istc.stlab.mira.commons.PublicationWithDoi;
import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;
import it.cnr.istc.stlab.mira.commons.ciatationmodel.MetricsJSONSchema;

public class DataCollectorTest {

	
	private static String modelPath = "/Users/andrea/Desktop/anvur/sessione_1/step4-scopus/rdf/fascia-2/01-B1/26271_LANZI_Andrea.ttl";
	private static Model model;
	private static PersonLevelAnalyzer personLevelAnalyzer;
	@BeforeClass
	public static void setUp(){
		personLevelAnalyzer = new PersonLevelAnalyzer();
		RDFParser parser = new TurtleParserFactory().getParser();
		model = new LinkedHashModel();
		StatementCollector collector = new StatementCollector(model);
		parser.setRDFHandler(collector);
		try {
			parser.parse(new FileInputStream(modelPath), QualificationProcessOntology.NS);
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAnalysis() {
		CitationMatrix citationMatrix = personLevelAnalyzer.getMatrix(model, 1);
		System.out.println(citationMatrix.toString());
	}
	
	@AfterClass
	public static void tearDown(){
		model = null;
	}
	
	
}
