package it.cnr.istc.stlab.mira.analytics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.turtle.TurtleParserFactory;

import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;

public class DisciplinaryLevelAnalyzerByYear {
	
	public Model analyze(int level, File personFolder, String year, int metricsLevel){
		System.out.println(level + " : " + personFolder.getPath() + " : " + year);
		PersonLevelAnalyzerByYear personLevelAnalyzer = new PersonLevelAnalyzerByYear();
		
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
					
					CitationMatrix citationMatrixTmp = personLevelAnalyzer.getMatrix(personModel, year, metricsLevel);
					
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
		
		String[][] doubleMatrixWithDOIs = citationMatrix.toDoubleMatrixWithDOIs();
		
		boolean start = true;
		StringBuilder doubleMatrixSb = new StringBuilder();
		StringBuilder correlationSb = new StringBuilder();
		StringBuilder pvaluesSb = new StringBuilder();
		StringBuilder standardErrorsSb = new StringBuilder();
		
		String[] headers = new String[citationMatrix.getCitationMetricTypeFeatures().size()];
		int l = 0;
		for(CitationMetricTypeFeature cmtf : citationMatrix.getCitationMetricTypeFeatures()){
		
			if(l>0) doubleMatrixSb.append(";");
			else doubleMatrixSb.append("DOI;");
			
			correlationSb.append(";");
			pvaluesSb.append(";");
			standardErrorsSb.append(";");
			doubleMatrixSb.append(cmtf.getFeatureLabel());
			correlationSb.append(cmtf.getFeatureLabel());
			pvaluesSb.append(cmtf.getFeatureLabel());
			standardErrorsSb.append(cmtf.getFeatureLabel());
			
			headers[l] = cmtf.getFeatureLabel();
			l++;	
		}
		
		doubleMatrixSb.append('\n');
		correlationSb.append('\n');
		pvaluesSb.append('\n');
		standardErrorsSb.append('\n');
		
		PearsonsCorrelation pc = new PearsonsCorrelation(doubleMatrix);
		RealMatrix correlationMatrix = pc.getCorrelationMatrix();
		RealMatrix pValues = pc.getCorrelationPValues();
		RealMatrix standardErrors = pc.getCorrelationStandardErrors();
		
		for(int i=0; i<doubleMatrixWithDOIs.length; i++){
			for(int k=0; k<doubleMatrixWithDOIs[i].length; k++){
				if(k>0) doubleMatrixSb.append(";");
				doubleMatrixSb.append(doubleMatrixWithDOIs[i][k]);
			}
			doubleMatrixSb.append('\n');
		}
		
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
			File folder = new File(personFolder.getName() + "/" + level + "/" + year);
			folder.mkdirs();
			
			BufferedWriter out = new BufferedWriter(new FileWriter(new File(folder, "doubleMatrix.csv")));
			out.write(doubleMatrixSb.toString());
			out.flush();
			out.close();
			
			out = new BufferedWriter(new FileWriter(new File(folder, "correlation.csv")));
			out.write(correlationSb.toString());
			out.flush();
			out.close();
			
			out = new BufferedWriter(new FileWriter(new File(folder, "pvalues.csv")));
			out.write(pvaluesSb.toString());
			out.flush();
			out.close();
			
			out = new BufferedWriter(new FileWriter(new File(folder, "standarderrors.csv")));
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
		String[] disciplinaryAreas = {"01-B1", "04-A1", "06-N1", "09-H1", "13-A1"};
		
		int metricsLevel = 3;
		
		DisciplinaryLevelAnalyzerByYear disciplinaryLevelAnalyzer = new DisciplinaryLevelAnalyzerByYear();
		for(int i=0; i<disciplinaryAreas.length; i++){
			for(int year=2006; year<2017; year++){
				for(int level=1; level<3; level++){
					File disciplinaryFolder = new File("/Users/andrea/Desktop/anvur/sessione_1/step4-scopus/rdf/fascia-" + level + "/" + disciplinaryAreas[i] + "/");
					disciplinaryLevelAnalyzer.analyze(level, disciplinaryFolder, String.valueOf(year), metricsLevel);
				}
			}
		}
			
	}
	
	
}
