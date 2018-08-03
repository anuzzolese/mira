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

import it.cnr.istc.stlab.mira.commons.PublicationWithDoi;
import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;
import it.cnr.istc.stlab.mira.commons.ciatationmodel.MetricsJSONSchema;

public class DataCollectorTest {

	
	private static String modelPath = "/Users/andrea/Desktop/anvur/sessione_1/step3-altmetrics/fascia-2/rdf/01-B1/14048_PRESUTTI_Valentina.ttl";
	private static Model model;
	
	@BeforeClass
	public static void setUp(){
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
	public void testDataCollection() {
		DataCollector dataCollector = new DataCollector();
		
		List<PublicationWithDoi> pubsWithDois = dataCollector.collect(model);
		pubsWithDois.forEach(pubWithDoi -> {
			System.out.println(pubWithDoi.getPublication() + " - " + pubWithDoi.getDoi());
		});
		
		System.out.println();
		System.out.println("---------");
		System.out.println();
		
		PublicationWithDoi pubWithDoi = pubsWithDois.get(10);
		
		
		JSONObject object = CitationModel.generateJSONObject(pubWithDoi);
		try {
			System.out.println(pubWithDoi.getDoi() + ": " + object.toString(4));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Model model = MetricsJSONSchema.asRDF(pubWithDoi.getPublication(), object);
		
		System.out.println(model.size());
	}
	
	@AfterClass
	public static void tearDown(){
		model = null;
	}
	
	
}
