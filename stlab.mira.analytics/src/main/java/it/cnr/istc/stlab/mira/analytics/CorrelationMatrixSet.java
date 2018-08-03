package it.cnr.istc.stlab.mira.analytics;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CorrelationMatrixSet {

	
	private Set<CorrelationMatrix> correlationMaps;
	private Set<String> headers;
	
	public CorrelationMatrixSet() {
		this.correlationMaps = new HashSet<CorrelationMatrix>();
		this.headers = new HashSet<String>();
	}
	
	public void add(CorrelationMatrix correlationMatrix){
		Set<String> headers = correlationMatrix.getHeaders();
		correlationMaps.forEach(cm -> {
			headers.forEach(header -> {
				if(!cm.containsHeader(header))
					cm.add(header, 0.0, 0.0);
				if(!this.headers.contains(header))
					this.headers.add(header);
			});
		});
		
		this.headers.forEach(header -> {
			if(!correlationMatrix.containsHeader(header))
				correlationMatrix.add(header, 0.0, 0.0);
		});
		correlationMaps.add(correlationMatrix);
	}
	
	public double[] getAvgCorrelation(){
		double[] doubleVector = correlationMaps
				.stream()
				.map(correlationMatrix -> {
					Map<String, Double> correlationMap = correlationMatrix.getCorrelation();
					
					
					double[] valueVector = new double[correlationMap.size()];
					Set<String> headers = correlationMap.keySet();
					int pos = 0;
					for(String header : headers){
						valueVector[pos] = correlationMap.get(header);
						System.out.print(header+ ", ");
						//System.out.println("*** " + correlationMap.get(header));
						pos++;
					}
					System.out.println();
					
					return valueVector;
				})
				.reduce((a,b) -> {
					int length = a.length;
					
					
					double[] newarr = new double[length];
					for(int i=0; i<length; i++){
						//System.out.println(a[i] + "+" + b[i] + " = " + (a[i]+b[i]));
						newarr[i] = a[i]+b[i];
					}
					return newarr;
				}).get();
		
		for(int i=0; i<doubleVector.length; i++){
			//System.out.println("= " + doubleVector[i] + "/" + correlationMaps.size());
			
			doubleVector[i] = doubleVector[i] / correlationMaps.size();
		}
		return doubleVector;
	}
	
	public double[] getAvgPValues(){
		return correlationMaps
				.stream()
				.map(correlationMatrix -> {
					Map<String, Double> pValuesMap = correlationMatrix.getPValues();
					double[] valueVector = new double[pValuesMap.size()];
					Set<String> headers = pValuesMap.keySet();
					int pos = 0;
					for(String header : headers){
						valueVector[pos] = pValuesMap.get(header);
						pos++;
					}
					
					return valueVector;
				})
				.reduce((a,b) -> {
					int length = a.length;
					double[] avg = new double[length];
					for(int i=0; i<length; i++){
						avg[i] = (a[i]+b[i])/2;
					}
					return avg;
				}).get();
	}
	
	public Set<String> getHeaders() {
		return headers;
	}
}
