package it.cnr.istc.stlab.mira.analytics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.turtle.TurtleParserFactory;

import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;

public class DisciplinaryLevelAnalyzer {
	
	private URI citations = new URIImpl("http://w3id.org/anvur/nq/data/citation-count-type/citations");
	private URI citationIndexes = new URIImpl("http://w3id.org/anvur/nq/data/citation-count-type/citation-indexes");
	private URI scopus = new URIImpl("http://w3id.org/anvur/nq/data/citation-count-type/scopus");

	public Model analyze(File personFolder, int metricsLevel){
		PersonLevelAnalyzer personLevelAnalyzer = new PersonLevelAnalyzer();
		
		CitationMatrix citationMatrix = new CitationMatrix();
		if(personFolder.isDirectory() && !personFolder.isHidden()){
			File[] personModelFiles = personFolder.listFiles(f-> f.getName().endsWith(".ttl"));
			
			for(File personModelFile : personModelFiles){
				RDFParser parser = new TurtleParserFactory().getParser();
				Model personModel = new LinkedHashModel();
				StatementCollector collector = new StatementCollector(personModel);
				parser.setRDFHandler(collector);
				
				try {
					parser.parse(new FileInputStream(personModelFile), QualificationProcessOntology.NS);
					
					CitationMatrix citationMatrixTmp = personLevelAnalyzer.getMatrix(personModel, metricsLevel);
					
					//CitationMatrix citationMatrixTmp = personLevelAnalyzer.getMatrixOld(personModel, new CitationMetricTypeFeature[0]);
					citationMatrix.addAll(citationMatrixTmp);
				} catch (RDFParseException | RDFHandlerException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		double[][] doubleMatrix = citationMatrix.toDoubleMatrix();
		
		for(int i=0; i<doubleMatrix.length; i++){
			for(int j=0; j<doubleMatrix[i].length; j++){
				System.out.print(doubleMatrix[i][j] + ",");
			}
			System.out.println();
		}
		
		
		StringBuilder correlationSb = new StringBuilder();
		StringBuilder pvaluesSb = new StringBuilder();
		StringBuilder standardErrorsSb = new StringBuilder();
		
		String[] headers = new String[citationMatrix.getCitationMetricTypeFeatures().size()];
		int l = 0;
		for(CitationMetricTypeFeature cmtf : citationMatrix.getCitationMetricTypeFeatures()){
		
			correlationSb.append(";");
			pvaluesSb.append(";");
			standardErrorsSb.append(";");
			correlationSb.append(cmtf.getFeatureLabel());
			pvaluesSb.append(cmtf.getFeatureLabel());
			standardErrorsSb.append(cmtf.getFeatureLabel());
			
			headers[l] = cmtf.getFeatureLabel();
			l++;	
		}
		
		correlationSb.append('\n');
		pvaluesSb.append('\n');
		standardErrorsSb.append('\n');
		
		PearsonsCorrelation pc = new PearsonsCorrelation(doubleMatrix);
		RealMatrix correlationMatrix = pc.getCorrelationMatrix();
		RealMatrix pValues = pc.getCorrelationPValues();
		RealMatrix standardErrors = pc.getCorrelationStandardErrors();
		
		for(int i=0, j=correlationMatrix.getRowDimension(); i<j; i++){
			for(int k=0, z=correlationMatrix.getColumnDimension(); k<z; k++){
				if(k>0) correlationSb.append(";");
				else correlationSb.append(headers[i]+";");
				String entry = String.valueOf(correlationMatrix.getEntry(i, k)).replace(".", ",");
				correlationSb.append(entry);
			}
			correlationSb.append('\n');
		}
		
		for(int i=0, j=pValues.getRowDimension(); i<j; i++){
			for(int k=0, z=pValues.getColumnDimension(); k<z; k++){
				if(k>0) pvaluesSb.append(";");
				else pvaluesSb.append(headers[i]+";");
				String entry = String.valueOf(pValues.getEntry(i, k)).replace(".", ",");;
				pvaluesSb.append(entry);
			}
			pvaluesSb.append('\n');
		}
		
		for(int i=0, j=standardErrors.getRowDimension(); i<j; i++){
			for(int k=0, z=standardErrors.getColumnDimension(); k<z; k++){
				if(k>0) standardErrorsSb.append(";");
				else standardErrorsSb.append(headers[i]+";");
				String entry = String.valueOf(standardErrors.getEntry(i, k)).replace(".", ",");
				standardErrorsSb.append(entry);
			}
			standardErrorsSb.append('\n');
		}
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("correlation.csv"));
			out.write(correlationSb.toString());
			out.flush();
			out.close();
			
			out = new BufferedWriter(new FileWriter("pvalues.csv"));
			out.write(pvaluesSb.toString());
			out.flush();
			out.close();
			
			out = new BufferedWriter(new FileWriter("standarderrors.csv"));
			out.write(standardErrorsSb.toString());
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static void main(String[] args) {
		File disciplinaryFolder = new File("/Users/andrea/Desktop/anvur/sessione_1/step4-scopus/rdf/fascia-1/13-A1/");
		
		DisciplinaryLevelAnalyzer disciplinaryLevelAnalyzer = new DisciplinaryLevelAnalyzer();
		disciplinaryLevelAnalyzer.analyze(disciplinaryFolder, 3);
	}
	
	
}
