package it.cnr.istc.stlab.mira.analytics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.RealMatrix;

public class CorrelationMatrix {
	
	Map<String, Double> correlation;
	Map<String, Double> pValues;
	
	public CorrelationMatrix() {
		this.correlation = new HashMap<String, Double>();
		this.pValues = new HashMap<String, Double>();
	}
	
	public CorrelationMatrix(Map<String, Double> correlation, Map<String, Double> pValues) {
		this.correlation = correlation;
		this.pValues = pValues;
	}
	
	public CorrelationMatrix(CitationMetricTypeFeature[] headers, RealMatrix correlation, RealMatrix pValues) {
		this(); 
		
		for(int i=0, j=correlation.getRowDimension(); i<j; i++){
			for(int k=0, z=correlation.getColumnDimension(); k<z; k++){
				
				String header1 = headers[i].getFeatureURI().getLocalName();
				String header2 = headers[k].getFeatureURI().getLocalName();
				if(!header1.equals(header2)){		
					String key = header1.compareTo(header2) < 1 ? (header1 + "_" + header2) : (header2 + "_" + header1);		
							
					if(!this.correlation.containsKey(key)){
						double value = correlation.getEntry(i,k);
						if(!Double.isNaN(value)) this.correlation.put(key, value);
					}
				}
			}
		}
		
		for(int i=0, j=pValues.getRowDimension(); i<j; i++){
			for(int k=0, z=pValues.getColumnDimension(); k<z; k++){
				String header1 = headers[i].getFeatureURI().getLocalName();
				String header2 = headers[k].getFeatureURI().getLocalName();
				if(!header1.equals(header2)){
					String key = header1.compareTo(header2) < 1 ? (header1 + "_" + header2) : (header2 + "_" + header1);
					
					if(!this.pValues.containsKey(key))
						this.pValues.put(key, pValues.getEntry(i,k));
				}
			}
		}
	}
	
	public boolean containsHeader(String header){
		return correlation.keySet().contains(header);
	}
	
	public Set<String> getHeaders(){
		return correlation.keySet();
	}
	
	public Map<String, Double> getCorrelation() {
		return correlation;
	}
	
	public void add(String header, double correlationValue, double pValue){
		correlation.put(header, correlationValue);
		pValues.put(header, pValue);
	}
	
	public double getCorrelation(String key) {
		Double value = correlation.get(key);
		if(value == null) return 0.0;
		else return value;
	}
	
	public double getPValues(String key) {
		Double value = pValues.get(key);
		if(value == null) return 0.0;
		else return value;
	}
	
	public Map<String, Double> getPValues() {
		return pValues;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();	
		getHeaders().forEach(header -> {
			double correlation = getCorrelation(header);
			double pvalue = getPValues(header);
			sb.append(header + ": " + correlation + "," + pvalue + " ");	
		});
		return sb.toString();
	}

}
