package it.cnr.istc.stlab.mira.analytics;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
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
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.turtle.TurtleParserFactory;
import org.openrdf.sail.memory.MemoryStore;

import com.bigdata.rdf.internal.XSD;

import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;
import it.cnr.istc.stlab.mira.commons.StatisticalOntology;

public class PersonLevelAnalyzerByYear {
	
	private URI citations = new URIImpl("http://w3id.org/anvur/nq/data/citation-count-type/citations");
	private URI citationIndexes = new URIImpl("http://w3id.org/anvur/nq/data/citation-count-type/citation-indexes");
	private URI scopus = new URIImpl("http://w3id.org/anvur/nq/data/citation-count-type/scopus");

	
	public CorrelationMatrix compute(Model personModel){
		CitationMetricTypeFeature[] citationMetricTypeFeatures;
		
		List<CitationMetricTypeFeature> citationMetricFeaturesList = personModel.filter(null, QualificationProcessOntology.hasCitationCount, null)
			.stream()
			.map(stmt -> {
				URI citationCount = (URI) stmt.getObject();
				URI citationCountType = (URI) personModel.filter(citationCount, QualificationProcessOntology.hasCitationCountType, null).objectResource();
				
				Literal labelLiteral = personModel.filter(citationCountType, RDFS.LABEL, null).objectLiteral();
				
				CitationMetricTypeFeature citationMetricFeature = new CitationMetricTypeFeature(citationCountType, labelLiteral.getLabel());
				
				return citationMetricFeature;
			})
			//.filter(feature -> feature != null)
			.distinct()
			.collect(Collectors.toList());
		citationMetricTypeFeatures = new CitationMetricTypeFeature[citationMetricFeaturesList.size()];
		citationMetricFeaturesList.toArray(citationMetricTypeFeatures);
	
		
		List<CitationMetricFeature> scopusCitationMetricFeatures = getScopusCitationMetricFeatures(personModel);

		scopusCitationMetricFeatures.sort((a,b) -> {
			if(a.getCount() > b.getCount()) 
				return 1; 
			else if(a.getCount() < b.getCount()) return -1;
			else return 0;
		});
		
		final CitationMetricTypeFeature[] cmtf = citationMetricTypeFeatures;
		List<CitationMetricFeatures> citationMFList = scopusCitationMetricFeatures.stream()
			.map(scopusCitationMetricFeature -> {
				CitationMetricFeatures citationMetricFeatures = new CitationMetricFeatures(scopusCitationMetricFeature.getDoi());
				citationMetricFeatures.add(scopusCitationMetricFeature);
				for(CitationMetricTypeFeature citationMetricTypeFeature : cmtf){
					CitationMetricFeature cmf = getCitationMetricFeatures(personModel, scopusCitationMetricFeature.getPaperURI(), citationMetricTypeFeature);

					if(cmf != null) citationMetricFeatures.add(cmf);
				}
				return citationMetricFeatures;
			})
			.collect(Collectors.toList());
		
		CitationMatrix citationMatrix = new CitationMatrix();
		citationMFList.forEach(cf -> citationMatrix.add(cf));
		
		double[][] doubleMatrix = citationMatrix.toDoubleMatrix();
		
		/*
		for(int i=0; i<doubleMatrix.length; i++){
			for(int j=0; j<doubleMatrix[i].length; j++)
				System.out.print(doubleMatrix[i][j]+ " ");
			System.out.println();
		}
		*/
		//System.out.println(getClass() + " double matrix : " + doubleMatrix);
		PearsonsCorrelation pc = new PearsonsCorrelation(doubleMatrix);
		RealMatrix correlationMatrix = pc.getCorrelationMatrix();
		RealMatrix pValues = pc.getCorrelationPValues();
		
		
		List<CitationMetricTypeFeature> featTypes = citationMatrix.getCitationMetricTypeFeatures();
		int featTypesSize = featTypes.size();
		CitationMetricTypeFeature[] headers = new CitationMetricTypeFeature[featTypesSize];
		int pos = 0;
		for(CitationMetricTypeFeature featType : featTypes){
			headers[pos] = featType;
			pos++;
		}
		
		return new CorrelationMatrix(headers, correlationMatrix, pValues);
	}
	
	public Model analyze(Model personModel){
		return analyze(personModel, 1, new CitationMetricTypeFeature[0]);
	}
	
	public Model analyze(Model personModel, int level, CitationMetricTypeFeature...citationMetricTypeFeatures){
		if(citationMetricTypeFeatures == null || citationMetricTypeFeatures.length == 0){
			List<CitationMetricTypeFeature> citationMetricFeaturesList = personModel.filter(null, QualificationProcessOntology.hasCitationCount, null)
				.stream()
				.map(stmt -> {
					URI citationCount = (URI) stmt.getObject();
					URI citationCountType = (URI) personModel.filter(citationCount, QualificationProcessOntology.hasCitationCountType, null).objectResource();
					
					Literal labelLiteral = personModel.filter(citationCountType, RDFS.LABEL, null).objectLiteral();
					CitationMetricTypeFeature citationMetricFeature = new CitationMetricTypeFeature(citationCountType, labelLiteral.getLabel());
					
					return citationMetricFeature;
				})
				//.filter(feature -> feature != null)
				.distinct()
				.collect(Collectors.toList());
			citationMetricTypeFeatures = new CitationMetricTypeFeature[citationMetricFeaturesList.size()];
			citationMetricFeaturesList.toArray(citationMetricTypeFeatures);
		}
		
		List<CitationMetricFeature> scopusCitationMetricFeatures = getScopusCitationMetricFeatures(personModel);

		scopusCitationMetricFeatures.sort((a,b) -> {
			if(a.getCount() > b.getCount()) 
				return 1; 
			else if(a.getCount() < b.getCount()) return -1;
			else return 0;
		});
		
		final CitationMetricTypeFeature[] cmtf = citationMetricTypeFeatures;
		List<CitationMetricFeatures> citationMetricFeaturesList = scopusCitationMetricFeatures.stream()
			.map(scopusCitationMetricFeature -> {
				CitationMetricFeatures citationMetricFeatures = new CitationMetricFeatures(scopusCitationMetricFeature.getDoi());
				citationMetricFeatures.add(scopusCitationMetricFeature);
				for(CitationMetricTypeFeature citationMetricTypeFeature : cmtf){
					CitationMetricFeature cmf = getCitationMetricFeatures(personModel, scopusCitationMetricFeature.getPaperURI(), citationMetricTypeFeature);

					if(cmf != null) citationMetricFeatures.add(cmf);
				}
				return citationMetricFeatures;
			})
			.collect(Collectors.toList());
		
		double[][] scores = matrix(citationMetricFeaturesList, citationMetricTypeFeatures);
		
		/*
		double[] citations = new double[scores.length];
		for(int i=0; i<scores.length; i++){
			citations[i] = scores[i][0];
		}
		
		double[] captures = new double[scores.length];
		for(int i=0; i<scores.length; i++){
			captures[i] = scores[i][1];
		}
		*/
		
		
		PearsonsCorrelation pc = new PearsonsCorrelation(scores);
		RealMatrix correlationMatrix = pc.getCorrelationMatrix();
		RealMatrix pValues = new PearsonsCorrelation(scores).getCorrelationPValues();
		
		System.out.println(correlationMatrix);
		System.out.println(pValues);
		Model model = new LinkedHashModel();
		
		
		for(int i=0, j=correlationMatrix.getRowDimension(); i<j; i++){
			for(int k=0, z=correlationMatrix.getColumnDimension(); k<z; k++){
				CitationMetricTypeFeature metrics2 = citationMetricTypeFeatures[k];
				if(k>i){
					CitationMetricTypeFeature metrics1 = citationMetricTypeFeatures[i];
					
					String metricsLabel1 = metrics1.getFeatureURI().getLocalName();
					String metricsLabel2 = metrics2.getFeatureURI().getLocalName();
					
					URI pearsonCorrelationSubj = new URIImpl(QualificationProcessOntology.NS_DATA + "pearson/" + metricsLabel1 + "_" + metricsLabel2);
					model.add(pearsonCorrelationSubj, RDF.TYPE, StatisticalOntology.PearsonCorrelation);
					model.add(pearsonCorrelationSubj, StatisticalOntology.hasExaminedMetrics, metrics1.getFeatureURI());
					model.add(pearsonCorrelationSubj, StatisticalOntology.hasExaminedMetrics, metrics2.getFeatureURI());
					model.add(pearsonCorrelationSubj, StatisticalOntology.correlation, new LiteralImpl(String.format("%.12f", correlationMatrix.getEntry(i, k)), XSD.DOUBLE));
					model.add(pearsonCorrelationSubj, StatisticalOntology.correlation, new LiteralImpl(String.format("%.12f", pValues.getEntry(i, k)), XSD.DOUBLE));
					model.add(pearsonCorrelationSubj, StatisticalOntology.level, new LiteralImpl(String.valueOf(level), XSD.INT));
					
					//System.out.print(citationMetricTypeFeatures[i].getFeatureLabel() + " --- " + metrics + ": " + correlationMatrix.getEntry(i, k)+ "(" + pValues.getEntry(i, k) + ")" + "    ");
				}
			}
			//System.out.println();
		}
		
		for(CitationMetricTypeFeature feat : citationMetricTypeFeatures){
			if(!feat.getFeatureURI().equals(citations)){
				//System.out.println(feat.getFeatureURI());
				Set<CitationMetricTypeFeature> feats = getSubFeatures(personModel, feat);
				CitationMetricTypeFeature[] featsArray = new CitationMetricTypeFeature[feats.size()];
				feats.toArray(featsArray);
				for(int i=0; i<featsArray.length; i++)
					System.out.println(featsArray[i].getFeatureLabel());
				//model.addAll(analyze(personModel, level+1, featsArray));
				
			}
		}
		
		return model;
		
		/*
		double z = pc.correlation(citations, captures);
		System.out.println("Z : " + z);
		*/
	}
	
	public CitationMatrix getMatrix(Model personModel, String year, int metricsLevel){
		
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
				sparql = MetricsLevelQueries.LEVEL_2_BY_YEAR.replace("$1", "\"" + year + "\"^^<" + XSD.GYEAR.toString() + ">");
				break;
			case 3:
				sparql = MetricsLevelQueries.LEVEL_3_BY_YEAR.replace("$1", "\"" + year + "\"^^<" + XSD.GYEAR.toString() + ">");
				break;
			default:
				sparql = MetricsLevelQueries.LEVEL_1_BY_YEAR.replace("$1", "\"" + year + "\"^^<" + XSD.GYEAR.toString() + ">");
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
	
	public CitationMatrix getMatrixOld(Model personModel, CitationMetricTypeFeature...citationMetricTypeFeatures){
		
		CitationMatrix citationMatrix = new CitationMatrix();
		
		if(citationMetricTypeFeatures == null || citationMetricTypeFeatures.length == 0){
			List<CitationMetricTypeFeature> citationMetricFeaturesList = personModel.filter(null, QualificationProcessOntology.hasCitationCount, null)
				.stream()
				.map(stmt -> {
					URI citationCount = (URI) stmt.getObject();
					URI citationCountType = (URI) personModel.filter(citationCount, QualificationProcessOntology.hasCitationCountType, null).objectResource();
					
					Literal labelLiteral = personModel.filter(citationCountType, RDFS.LABEL, null).objectLiteral();
					CitationMetricTypeFeature citationMetricFeature = new CitationMetricTypeFeature(citationCountType, labelLiteral.getLabel());
					
					return citationMetricFeature;
				})
				//.filter(feature -> feature != null)
				.distinct()
				.collect(Collectors.toList());
			citationMetricTypeFeatures = new CitationMetricTypeFeature[citationMetricFeaturesList.size()];
			citationMetricFeaturesList.toArray(citationMetricTypeFeatures);
		}
		
		List<CitationMetricFeature> scopusCitationMetricFeatures = getScopusCitationMetricFeatures(personModel);

		scopusCitationMetricFeatures.sort((a,b) -> {
			if(a.getCount() > b.getCount()) 
				return 1; 
			else if(a.getCount() < b.getCount()) return -1;
			else return 0;
		});
		
		final CitationMetricTypeFeature[] cmtf = citationMetricTypeFeatures;
		List<CitationMetricFeatures> citationMetricFeaturesList = scopusCitationMetricFeatures.stream()
			.map(scopusCitationMetricFeature -> {
				CitationMetricFeatures citationMetricFeatures = new CitationMetricFeatures(scopusCitationMetricFeature.getDoi());
				citationMetricFeatures.add(scopusCitationMetricFeature);
				for(CitationMetricTypeFeature citationMetricTypeFeature : cmtf){
					CitationMetricFeature cmf = getCitationMetricFeatures(personModel, scopusCitationMetricFeature.getPaperURI(), citationMetricTypeFeature);

					if(cmf != null) citationMetricFeatures.add(cmf);
				}
				return citationMetricFeatures;
			})
			.collect(Collectors.toList());
		
		citationMetricFeaturesList.forEach(citationMetricFeatures -> {
			citationMatrix.add(citationMetricFeatures);
		});
		
		return citationMatrix;
	}
	
	private Set<CitationMetricTypeFeature> getSubFeatures(Model personModel, CitationMetricTypeFeature cmtf){
		Set<CitationMetricTypeFeature> ctmfs = new HashSet<CitationMetricTypeFeature>();
		
		Set<Resource> citationCounts = personModel.filter(null, QualificationProcessOntology.hasCitationCountType, cmtf.getFeatureURI()).subjects();
		
		citationCounts.forEach(citationCount -> {
			Set<Value> parts = personModel.filter(citationCount, QualificationProcessOntology.hasPart, null).objects();
			parts.forEach(part -> {
				URI citationCountType = (URI)personModel.filter((URI)part, QualificationProcessOntology.hasCitationCountType, null).objectResource();
				Literal labelLiteral = personModel.filter(citationCountType, RDFS.LABEL, null).objectLiteral();
				CitationMetricTypeFeature citationMetricTypeFeature = new CitationMetricTypeFeature(citationCountType, labelLiteral.getLabel());
				ctmfs.add(citationMetricTypeFeature);
			});
			
		});
		
		return ctmfs.stream().distinct().collect(Collectors.toSet());
		
	}
	
	private CitationMetricFeature getCitationMetricFeatures(Model personModel, URI paperURI, CitationMetricTypeFeature citationMetricTypeFeature){
		
		CitationMetricFeature cmf = null;
		
		if(!citationMetricTypeFeature.getFeatureURI().equals(citations)){
			try{ 
				cmf = personModel.filter(paperURI, QualificationProcessOntology.hasCitationCount, null)
					.stream()
					.map(stmt -> {
						URI citationCount = (URI) stmt.getObject();
						Model citationModel = personModel.filter(citationCount, QualificationProcessOntology.hasCitationCountType, citationMetricTypeFeature.getFeatureURI());
						if(!citationModel.isEmpty()){
							Literal scoreValue = personModel.filter(citationCount, QualificationProcessOntology.scoreValue, null).objectLiteral();
							String doi = personModel.filter(paperURI, QualificationProcessOntology.doi, null).objectLiteral().stringValue();
							return new CitationMetricFeature(paperURI, doi, citationMetricTypeFeature, scoreValue.intValue());
						}
						else return null;
							
					})
					.filter(s -> s != null)
					.collect(Collectors.maxBy((a,b) -> {
						if(a.getCount() > b.getCount()) return 1;
						else if(a.getCount() < b.getCount()) return -1;
						else return 0;
					})).get();
			} catch(NoSuchElementException e){
				String doi = personModel.filter(paperURI, QualificationProcessOntology.doi, null).objectLiteral().stringValue();
				cmf = new CitationMetricFeature(paperURI, doi, citationMetricTypeFeature, 0);
			}
		}
		
		return cmf;
	}
	
	private List<CitationMetricFeature> getScopusCitationMetricFeatures(Model personModel){
		
		return personModel.filter(null, QualificationProcessOntology.hasCitationCountType, citations)
				.stream()
				.flatMap(stmt -> {
					URI[] paper = new URI[1];
					URI paperCitationCount = (URI) stmt.getSubject();
					personModel.filter(null, QualificationProcessOntology.hasCitationCount, paperCitationCount)
						.subjects()
						.forEach(subj -> {
							paper[0] = (URI) subj;
						});
					List<CitationMetricFeature> subCitationMetricFeatures = personModel.filter(paperCitationCount, QualificationProcessOntology.hasPart, null)
						.stream()
						.flatMap(subStmt -> {
							URI subPart = (URI) subStmt.getObject();
							URI subPartType = (URI) personModel.filter(subPart, QualificationProcessOntology.hasCitationCountType, null).objectResource();
							List<CitationMetricFeature> subSubCitationMetricFeatures = null;
							if(subPartType.equals(citationIndexes)){
								subSubCitationMetricFeatures = personModel.filter(subPart, QualificationProcessOntology.hasPart, null)
									.stream()
									.map(subSubStmt -> {
										URI subSubPart = (URI) subSubStmt.getObject();
										URI subSubPartType = (URI) personModel.filter(subSubPart, QualificationProcessOntology.hasCitationCountType, null).objectResource();
										if(subSubPartType.equals(scopus)) {
											
											Literal scoreValue = personModel.filter(subSubPart, QualificationProcessOntology.scoreValue, null).objectLiteral();
											
											String doi = personModel.filter(paper[0], QualificationProcessOntology.doi, null).objectLiteral().stringValue();
											return new CitationMetricFeature(paper[0], doi, new CitationMetricTypeFeature(citations, "Citations"), scoreValue.intValue());
										}
										else return null;
											
									})
									.filter(s -> s != null)
									.collect(Collectors.toList());;
							}
							else subSubCitationMetricFeatures = Collections.emptyList();
							
							return subSubCitationMetricFeatures.stream();
							
						})
						.collect(Collectors.toList());
						
						
					
					return subCitationMetricFeatures.stream();

				})
				.collect(Collectors.toList());
		
	}
	
	private double[][] matrix(List<CitationMetricFeatures> citationMetricFeatures, CitationMetricTypeFeature...citationMetricTypeFeatures){
		
		double[][] scoreValues = null;
		
		
		int k=0;
		for(CitationMetricTypeFeature cmtf : citationMetricTypeFeatures){
			List<Double> scores = citationMetricFeatures.stream()
				.map(metrics -> {
					CitationMetricFeature cmf = metrics.getByType(cmtf);
					if(cmf != null){
						return (double)cmf.getCount();
					}
					else return 0.00;
					
				})
				.collect(Collectors.toList());
			if(scoreValues == null)
				scoreValues = new double[scores.size()][citationMetricTypeFeatures.length];
			
			
			for(int i=0, j=scores.size(); i<j; i++){
				double s = scores.get(i);
				scoreValues[i][k] = s;
			}
			
			k++;
		}
		
		return scoreValues;
	}
	
	/*
	public static void main(String[] args) {
		PersonLevelAnalyzer analyzer = new PersonLevelAnalyzer();
		RDFParser parser = new TurtleParserFactory().getParser();
		Model model = new LinkedHashModel();
		StatementCollector collector = new StatementCollector(model);
		parser.setRDFHandler(collector);
		
		File rdfFile = new File("/Users/andrea/Desktop/anvur/sessione_1/step4-scopus/rdf/fascia-2/01-B1/14048_PRESUTTI_Valentina.ttl");
		
		try {
			parser.parse(new FileInputStream(rdfFile), QualificationProcessOntology.NS);
			
			Model statisticalModel = analyzer.analyze(model);
			
			RDFWriter writer = new TurtleWriterFactory().getWriter(System.out);
			writer.startRDF();
			for(Statement stmt : statisticalModel)
				writer.handleStatement(stmt);
			writer.endRDF();
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	
	public static void main(String[] args) {
		
		PersonLevelAnalyzerByYear personLevelAnalyzer = new PersonLevelAnalyzerByYear();
		
		File personFolder = new File("/Users/andrea/Desktop/anvur/sessione_1/step4-scopus/rdf/fascia-2/01-B1");
		
		CorrelationMatrixSet correlationMatrixSet = new CorrelationMatrixSet();
		if(personFolder.isDirectory() && !personFolder.isHidden()){
			File[] personModelFiles = personFolder.listFiles(f-> f.getName().endsWith(".ttl"));
			
			for(File personModelFile : personModelFiles){
				RDFParser parser = new TurtleParserFactory().getParser();
				Model personModel = new LinkedHashModel();
				StatementCollector collector = new StatementCollector(personModel);
				parser.setRDFHandler(collector);
				
				try {
					parser.parse(new FileInputStream(personModelFile), QualificationProcessOntology.NS);
					
					CorrelationMatrix correlationMatrix = personLevelAnalyzer.compute(personModel);
					
					//System.out.println(personModelFile.getName() + "::: " + correlationMatrix.toString());
					correlationMatrixSet.add(correlationMatrix);
				} catch (Exception e) {
					System.err.println("Err " + personModelFile);
				}
			}
		}
		
		//System.out.println(correlationMatrix.correlation.size());
		
		double[] avgCorrelation = correlationMatrixSet.getAvgCorrelation();
		double[] avgPValues = correlationMatrixSet.getAvgPValues();
		
		System.out.println();
		correlationMatrixSet.getHeaders().forEach(header -> {
			System.out.print(header+ ", ");
		});
		System.out.println();
		
		for(double corr : avgCorrelation){
			System.out.print(corr + ", ");
		}
		System.out.println();
		
		for(double pval : avgPValues){
			System.out.print(pval + ", ");
		}
		System.out.println();
			
	}
	
	
}
