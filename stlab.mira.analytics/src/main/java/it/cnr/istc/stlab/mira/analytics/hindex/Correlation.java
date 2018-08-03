package it.cnr.istc.stlab.mira.analytics.hindex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.util.Precision;
import org.openrdf.model.impl.URIImpl;

import au.com.bytecode.opencsv.CSVReader;
import it.cnr.istc.stlab.mira.analytics.CitationMatrix;
import it.cnr.istc.stlab.mira.analytics.CitationMetricFeature;
import it.cnr.istc.stlab.mira.analytics.CitationMetricFeatures;
import it.cnr.istc.stlab.mira.analytics.CitationMetricTypeFeature;

public class Correlation {

	private final String hIndexHeaderSuffix = "H-Index ";
	public void pearson(CSVReader reader, File outFolder){
		outFolder.mkdirs();
		boolean header = true;
		List<Integer> columns = new ArrayList<Integer>();
		
		Map<Integer,CitationMetricTypeFeature> cmtfs = new HashMap<Integer,CitationMetricTypeFeature>();
		
		String[] row = null;
		try {
			CitationMatrix citationMatrix = new CitationMatrix();
			while((row = reader.readNext()) != null){
				String submissionId = row[0].trim();
				if(header){
					header = false;
					for(int i=0; i<row.length; i++){
						String column = row[i];
						if(column.startsWith(hIndexHeaderSuffix)){
							columns.add(i);
							CitationMetricTypeFeature cmtf = new CitationMetricTypeFeature(new URIImpl("http://mira.it/" + column.replace(" ", "_")), column);
							cmtfs.put(i, cmtf);
						}
					}
				}
				else{
					CitationMetricFeatures cmfs = new CitationMetricFeatures(submissionId);
					for(Integer columnIndex : columns){
						String featureValue = row[columnIndex].trim();
						CitationMetricFeature cmf = new CitationMetricFeature(new URIImpl("http://mira.it/" + submissionId), submissionId, cmtfs.get(columnIndex), Integer.valueOf(featureValue));
						cmfs.add(cmf);
					}
					citationMatrix.add(cmfs);
				}
			}
			
			double[][] doubleMatrix = citationMatrix.toDoubleMatrix();
			
			
			PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation(doubleMatrix);
			RealMatrix correlationMatrix = pearsonsCorrelation.getCorrelationMatrix();
			RealMatrix pValues = pearsonsCorrelation.getCorrelationPValues();
			RealMatrix standardErrors = pearsonsCorrelation.getCorrelationStandardErrors();
			
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
				BufferedWriter out = new BufferedWriter(new FileWriter(new File(outFolder, "correlation.csv")));
				out.write(correlationSb.toString());
				out.flush();
				out.close();
				
				out = new BufferedWriter(new FileWriter(new File(outFolder, "pvalues.csv")));
				out.write(pvaluesSb.toString());
				out.flush();
				out.close();
				
				out = new BufferedWriter(new FileWriter(new File(outFolder, "standarderrors.csv")));
				out.write(standardErrorsSb.toString());
				out.flush();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
				
	}
	
	public static void main(String[] args) {
		String[] fasce = {"fascia-1", "fascia-2"};
		String[] disciplinaryFields = {"01-B1", "04-A1", "06-N1", "09-H1", "13-A1"};
		
		String inputPathFolder = "/Users/andrea/Desktop/anvur/sessione_1/step7-classification/input";
		String outputPathFolder = "/Users/andrea/Desktop/anvur/sessione_1/step8-hindex-correlation";
		
		Correlation correlation = new Correlation();
		
		for(String disciplinaryField : disciplinaryFields){
			for(String fascia : fasce){
				
				System.out.println(disciplinaryField + ": " + fascia);
				File input = new File(new File(inputPathFolder), disciplinaryField + "_" + fascia + ".csv");
				try {
					CSVReader reader = new CSVReader(new FileReader(input));
					correlation.pearson(reader, new File(outputPathFolder + "/" + disciplinaryField + "/" + fascia));
					
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
}
