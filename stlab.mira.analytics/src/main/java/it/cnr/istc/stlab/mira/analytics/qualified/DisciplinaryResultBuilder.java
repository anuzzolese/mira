package it.cnr.istc.stlab.mira.analytics.qualified;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.turtle.TurtleParserFactory;
import org.openrdf.sail.memory.MemoryStore;

import au.com.bytecode.opencsv.CSVWriter;
import it.cnr.istc.stlab.mira.analytics.CitationMatrix;
import it.cnr.istc.stlab.mira.analytics.CitationMetricFeature;
import it.cnr.istc.stlab.mira.analytics.CitationMetricFeatures;
import it.cnr.istc.stlab.mira.analytics.CitationMetricTypeFeature;
import it.cnr.istc.stlab.mira.analytics.PersonLevelAnalyzer;
import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;

public class DisciplinaryResultBuilder {
	
	public static void build(File folder, File disciplinaryHtml, File out, int metricsLevel){
		DisciplinaryResultBuilder builder = new DisciplinaryResultBuilder();
		Map<String, ASNPerson> nameBasedMap = new HashMap<String, DisciplinaryResultBuilder.ASNPerson>();
		File[] models = folder.listFiles(f->f.isFile() && f.getName().endsWith("ttl"));
		
		CitationMetricTypeFeature hIndexMetricTypeFeature = new CitationMetricTypeFeature(new URIImpl("urn://_h-index"), "H-Index");
		
		CitationMatrix cm = new CitationMatrix();
		for(File model : models){
			String fileName = model.getName();
			Pattern pattern = Pattern.compile("^[0-9]+_");
			Matcher matcher = pattern.matcher(fileName);
			
			String applicationId = null;
			String name = null;
			if(matcher.find()) {
				applicationId = fileName.substring(matcher.start(), matcher.end()-1);
				name = fileName.substring(matcher.end()).replaceAll("\\.ttl$", "").replaceAll("\\_", " ");
			}
			if(applicationId != null && name != null){
				ASNPerson person = builder.new ASNPerson(applicationId, name, model.getAbsolutePath());
				nameBasedMap.put(name.toLowerCase(), person);
				
				PersonLevelAnalyzer pla = new PersonLevelAnalyzer();
				RDFParser parser = new TurtleParserFactory().getParser();
				Model personModel = new LinkedHashModel();
				StatementCollector collector = new StatementCollector(personModel);
				parser.setRDFHandler(collector);
				
				try {
					parser.parse(new FileInputStream(person.getModelPath()), QualificationProcessOntology.NS);
				} catch (RDFParseException | RDFHandlerException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				String query = "SELECT DISTINCT ?doi ?score "
						+ "WHERE{"
						+ "?publication <" + QualificationProcessOntology.hasCitationCount + "> ?citationCount; "
						+ "    <" + QualificationProcessOntology.doi + "> ?doi . "
						+ "?citationCount <" + QualificationProcessOntology.hasCitationCountType + "> <http://w3id.org/anvur/nq/data/citation-count-type/citations>; "
						+ "    <"+ QualificationProcessOntology.scoreValue + "> ?score "
						+ "}"
						+ "ORDER BY DESC(?score)";
				
				
				Repository repo = new SailRepository(new MemoryStore());
				
				CitationMetricFeature hIndexMetricFeature = null;
				try {
					repo.initialize();
					RepositoryConnection conn = repo.getConnection();
					conn.add(personModel);
					
					TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
					TupleQueryResult result = tupleQuery.evaluate();
					int hIndex = 0;
					while(result.hasNext()){
						BindingSet bindingSet = result.next();
						Literal doiLiteral = (Literal) bindingSet.getValue("doi");
						Literal scoreLiteral = (Literal) bindingSet.getValue("score");
						int score = scoreLiteral.intValue();
						if(hIndex > score) break;
						hIndex++;
					}
					
					System.out.println("---- " + hIndex);
					
					hIndexMetricFeature = new CitationMetricFeature(new URIImpl("urn://_" + person.applicationID), person.applicationID, hIndexMetricTypeFeature, hIndex);
					
				} catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				CitationMatrix citationMatrix = pla.getMatrix(personModel, metricsLevel);
				Map<CitationMetricTypeFeature, Integer> totals = citationMatrix.totals();
				
				CitationMetricFeatures cmfs = new CitationMetricFeatures(person.applicationID);
				cmfs.add(hIndexMetricFeature);
				totals.entrySet().forEach(entry -> {
					CitationMetricFeature cmf = new CitationMetricFeature(new URIImpl("urn://_" + person.applicationID), person.applicationID, entry.getKey(), entry.getValue());
					cmfs.add(cmf);
				});
				
				
				cm.add(cmfs);
			}
		}
		
		try {
			Document document = Jsoup.parse(disciplinaryHtml, "UTF-8");
			
			Element table = document.select("table").get(0);
			Elements rows = table.select("tr");
			
			
			for(int i=1, j=rows.size(); i<j; i++){
				Element row = rows.get(i);
				Elements columns = row.select("td");
				String familyName = columns.get(0).html();
				String givenName = columns.get(1).html();
				String name = familyName + " " + givenName;
				ASNPerson person = nameBasedMap.get(name.toLowerCase());
				person.setQualified(true);
				
				
				
				
			}
			
			CSVWriter writer = new CSVWriter(new FileWriter(out));
			String[] header = new String[cm.getCitationMetricTypeFeatures().size()+3];
			header[0] = "ID";
			header[1] = "Name";
			header[2] = "Result";
			
			int[] index = {3};
			cm.getCitationMetricTypeFeatures().forEach(cmtf -> {
				try {
					header[index[0]] = cmtf.getFeatureLabel();
					index[0] = index[0]+1;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			});
			
			writer.writeNext(header);
			nameBasedMap.values().forEach(person -> {
				try {
					int value = person.isQualified() ? 1 : 0;
					
					StringBuilder sb = new StringBuilder();
					System.out.println(person.getApplicationID());
					
					String[] row = new String[header.length];
					row[0] = person.getApplicationID();
					row[1] = person.getName();
					row[2] = String.valueOf(person.isQualified());
					
					CitationMetricFeatures citationMetricFeatures = cm.getCitationMetricFeatures(person.applicationID);
					System.out.println("cmfs " + citationMetricFeatures);
					
					int[] i = {3}; 
					cm.getCitationMetricTypeFeatures().forEach(cmtf -> {
						CitationMetricFeature cmf = citationMetricFeatures.getByType(cmtf);
						String v = null;
						if(cmf == null) v = "0";
						else v = String.valueOf(cmf.getCount());
						
						row[i[0]] = v;
						i[0] = i[0]+1;
					
					});
					
					writer.writeNext(row);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(-1);
				}
			});
			
			writer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		DisciplinaryResultBuilder.build(
				new File("/Users/andrea/Desktop/anvur/sessione_1/step4-scopus/rdf/fascia-2/01-B1"),
				new File("/Users/andrea/Desktop/anvur/sessione_1/lista_abilitati/sessione-1/fascia-2/01-B1.html"),
				new File("01-B1_2.csv"),
				1);
	}
	
	public class ASNPerson{
		
		private String applicationID, name, modelPath;
		private boolean qualified;
		private CitationMetricFeatures citationMetricFeatures;
		
		public ASNPerson(String applicationID, String name, String modelPath){
			this.applicationID = applicationID;
			this.name = name;
			this.modelPath = modelPath;
			qualified = false;
			citationMetricFeatures = null;;
		}
		
		public String getApplicationID() {
			return applicationID;
		}
		
		public String getName() {
			return name;
		}
		
		public String getModelPath() {
			return modelPath;
		}
		
		public boolean isQualified() {
			return qualified;
		}
		
		public void setQualified(boolean qualified) {
			this.qualified = qualified;
		}
		
		public CitationMetricFeatures getCitationMetricFeatures() {
			return citationMetricFeatures;
		}
		
		public void setCitationMetricFeatures(CitationMetricFeatures citationMetricFeatures) {
			this.citationMetricFeatures = citationMetricFeatures;
		}
	}
}
