package it.cnr.istc.stlab.mira.altmetrics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.codehaus.jettison.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.turtle.TurtleWriterFactory;

import com.bigdata.rdf.internal.XSD;

public class AltmetricsModel {
	
	private JSONObject jsonObject;
	private Model model;
	
	public AltmetricsModel(JSONObject jsonObject, Model model) {
		this.jsonObject = jsonObject;
		this.model = model;
	}
	
	public JSONObject getJsonObject() {
		return jsonObject;
	}
	
	public Model getModel() {
		return model;
	}
	
	public void writeRDFModel(OutputStream out){
		RDFWriter writer = new TurtleWriterFactory().getWriter(out);
		try {
			writer.handleNamespace("ref-list", "http://w3id.org/anvur/nq/data/reference-list/");
			writer.handleNamespace("biblio-item", "http://w3id.org/anvur/nq/data/bibliographic-item/");
			writer.handleNamespace("biblio-item-type", "http://w3id.org/anvur/nq/data/bibliographic-item-type/");
			writer.handleNamespace("application", "http://w3id.org/anvur/nq/data/application/");
			writer.handleNamespace("person", "http://w3id.org/anvur/nq/data/person/");
			writer.handleNamespace("resume", "http://w3id.org/anvur/nq/data/resume/");
			writer.handleNamespace("qualification-type", "http://w3id.org/anvur/nq/data/qualification-type/");
			writer.handleNamespace("qualification", "http://w3id.org/anvur/nq/data/qualification/");
			writer.handleNamespace("qualification-list", "http://w3id.org/anvur/nq/data/qualification-list/");
			writer.handleNamespace("publication", "http://w3id.org/anvur/nq/data/publication/");
			writer.handleNamespace("ont", "http://w3id.org/anvur/nq/ontology/");
			writer.handleNamespace("rdf", RDF.NAMESPACE);
			writer.handleNamespace("rdfs", RDFS.NAMESPACE);
			writer.handleNamespace("xsd", XSD.NAMESPACE);
			
			writer.startRDF();
			for(Statement stmt : model)
				writer.handleStatement(stmt);
			writer.endRDF();
			
			out.close();
		} catch (RDFHandlerException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public void writeJSONModel(OutputStream out){
		
		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(out));
		try {
			bufferedWriter.write(jsonObject.toString(4));
			bufferedWriter.flush();
			bufferedWriter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
}