package it.cnr.istc.stlab.mira.analytics.qualified;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
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

import it.cnr.istc.stlab.mira.analytics.CitationMetricFeature;
import it.cnr.istc.stlab.mira.analytics.CitationMetricTypeFeature;
import it.cnr.istc.stlab.mira.analytics.MetricsLevelQueries;
import it.cnr.istc.stlab.mira.analytics.YearCitationMatrix;
import it.cnr.istc.stlab.mira.analytics.YearCitationMetricFeatures;
import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;

public class YearBasedAnalytics {

	private YearCitationMatrix yearCitationMatrix;
	
	private YearBasedAnalytics(YearCitationMatrix yearCitationMatrix){
		this.yearCitationMatrix = yearCitationMatrix;
		
	}
	public static YearBasedAnalytics get(Model personModel, int metricLevel){
		
		YearCitationMatrix citationMatrix = new YearCitationMatrix();
		Repository repo = new SailRepository(new MemoryStore());
		
		try {
			repo.initialize();
			
			RepositoryConnection conn = repo.getConnection();
			conn.add(personModel);
			
			TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, MetricsLevelQueries.LEVEL_1_YEAR_BASED);
			TupleQueryResult result = tupleQuery.evaluate();
			
			Map<String, YearCitationMetricFeatures> citationMetricFtsMap = new HashMap<String, YearCitationMetricFeatures>();
			
			while(result.hasNext()){
				BindingSet bindingSet = result.next();
				URI publication = (URI) bindingSet.getValue("publication");
				Literal yearLiteral = (Literal) bindingSet.getValue("year");
				URI citationCountType = (URI) bindingSet.getValue("citationCountType");
				Literal labelLiteral = (Literal) bindingSet.getValue("label");
				Literal ctLabelLiteral = (Literal) bindingSet.getValue("ctLabel");
				Literal ctLabel2Literal = (Literal) bindingSet.getValue("ctLabel2");
				Literal scoreLiteral = (Literal) bindingSet.getValue("score");
				Literal doiLiteral = (Literal) bindingSet.getValue("doi");
				
				String label = null; 
				if(ctLabelLiteral != null){
					if(ctLabel2Literal != null) 
						label = "(" + ctLabelLiteral.getLabel() + " - " + ctLabel2Literal.getLabel() + ")" + labelLiteral.getLabel();
					else label = "(" + ctLabelLiteral.getLabel() + ")" + labelLiteral.getLabel();
				}
				else {
					if(ctLabel2Literal != null) 
						label = "(" + ctLabel2Literal.getLabel() + ")" + labelLiteral.getLabel();
					else label = labelLiteral.getLabel();
				}
				
				CitationMetricTypeFeature citationMetricFeature = new CitationMetricTypeFeature(citationCountType, label);
				CitationMetricFeature cmf = new CitationMetricFeature(publication, doiLiteral.getLabel(), citationMetricFeature, scoreLiteral.intValue());
				
				String year = yearLiteral.getLabel();
				
				YearCitationMetricFeatures cMFts = citationMetricFtsMap.get(year);
				if(cMFts == null){
					cMFts = new YearCitationMetricFeatures(year);
					citationMetricFtsMap.put(year, cMFts);
				}
				cMFts.sum(cmf);
				
				citationMetricFtsMap.values()
					.forEach(citationMetricFeatures -> {
						citationMatrix.add(citationMetricFeatures);
					});
				
			}
			
			conn.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
		return new YearBasedAnalytics(citationMatrix);
			
		
	}
	
	public YearCitationMatrix getYearCitationMatrix() {
		return yearCitationMatrix;
	}
	
	public void analyze(){
		
		double[][] doubleMatrix = yearCitationMatrix.toDoubleMatrix();
		
		PearsonsCorrelation pc = new PearsonsCorrelation(doubleMatrix);
		RealMatrix correlationMatrix = pc.getCorrelationMatrix();
		RealMatrix pValues = pc.getCorrelationPValues();
		RealMatrix standardErrors = pc.getCorrelationStandardErrors();
		
		System.out.println(correlationMatrix);
		System.out.println(pValues);
		System.out.println(standardErrors);
	}
	
	public static void main(String[] args) {
		RDFParser parser = new TurtleParserFactory().getParser();
		Model personModel = new LinkedHashModel();
		StatementCollector collector = new StatementCollector(personModel);
		parser.setRDFHandler(collector);
		
		String file = "/Users/andrea/Desktop/anvur/sessione_1/step4-scopus/rdf/fascia-2/01-B1/14048_PRESUTTI_Valentina.ttl";
		
		try {
			parser.parse(new FileInputStream(file), QualificationProcessOntology.NS);
			
			YearBasedAnalytics yearBasedAnalytics = YearBasedAnalytics.get(personModel, 1);
			
			System.out.println(yearBasedAnalytics.getYearCitationMatrix());
			
			yearBasedAnalytics.analyze();
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
