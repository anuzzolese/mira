package it.cnr.istc.stlab.mira.analytics.classification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
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
import org.openrdf.sail.memory.MemoryStore;

import it.cnr.istc.stlab.mira.analytics.CitationMatrix;
import it.cnr.istc.stlab.mira.analytics.CitationMetricFeature;
import it.cnr.istc.stlab.mira.analytics.CitationMetricFeatures;
import it.cnr.istc.stlab.mira.analytics.CitationMetricTypeFeature;
import it.cnr.istc.stlab.mira.analytics.MetricsLevelQueries;

public class Individual {
	
	public CitationMatrix getMatrix(Model personModel, int metricsLevel){
			
			CitationMatrix citationMatrix = new CitationMatrix();
			
			Repository repo = new SailRepository(new MemoryStore());
			try {
				repo.initialize();
				
				RepositoryConnection conn = repo.getConnection();
				conn.add(personModel);
				
				
				
				List<CitationMetricTypeFeature> citationMetricFeaturesList = new ArrayList<CitationMetricTypeFeature>();
				
				
				String sparql = null;
				switch (metricsLevel) {
				case 2:
					sparql = ClassificationMetricsLevelQueries.LEVEL_2;
					break;
				case 3:
					sparql = ClassificationMetricsLevelQueries.LEVEL_3;
					break;
				default:
					sparql = ClassificationMetricsLevelQueries.LEVEL_1;
					break;
				}
				
				
				Map<URI, CitationMetricFeatures> citationMetricFtsMap = new HashMap<URI, CitationMetricFeatures>();
				TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
				TupleQueryResult result = tupleQuery.evaluate();
				while(result.hasNext()){
					BindingSet bindingSet = result.next();
					URI publication = (URI) bindingSet.getValue("publication");
					URI citationCountType = (URI) bindingSet.getValue("citationCountType");
					Literal labelLiteral = (Literal) bindingSet.getValue("label");
					Literal ctLabelLiteral = (Literal) bindingSet.getValue("ctLabel");
					Literal ctLabel2Literal = (Literal) bindingSet.getValue("ctLabel2");
					Literal scoreLiteral = (Literal) bindingSet.getValue("score");
					Literal doiLiteral = (Literal) bindingSet.getValue("doi");
					Literal yearLiteral = (Literal) bindingSet.getValue("year");
					
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
					CitationMetricFeature cmf = new CitationMetricFeature(publication, doiLiteral.getLabel(), yearLiteral.getLabel(), citationMetricFeature, scoreLiteral.intValue());
					
					
					CitationMetricFeatures cMFts = citationMetricFtsMap.get(publication);
					if(cMFts == null){
						cMFts = new CitationMetricFeatures(doiLiteral.getLabel());
						citationMetricFtsMap.put(publication, cMFts);
					}
					cMFts.add(cmf);
					
				}
				
				citationMetricFtsMap.values()
					.forEach(citationMetricFeatures -> {
						citationMatrix.add(citationMetricFeatures);
					});
				
				
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
			
			return citationMatrix;
		}
}

