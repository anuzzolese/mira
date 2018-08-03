package it.cnr.istc.stlab.mira.analytics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class YearCitationMatrix {
	
	private Map<String, YearCitationMetricFeatures> map;
	private Set<CitationMetricTypeFeature> citationMetricTypeFeatures;
	
	public YearCitationMatrix() {
		this.map = new HashMap<String, YearCitationMetricFeatures>();
		citationMetricTypeFeatures = new HashSet<CitationMetricTypeFeature>();
	}
	
	public int size(){
		return map.size();
	}
	
	public void addAll(YearCitationMatrix citationMatrix) {
		Set<String> years = citationMatrix.map.keySet();
		for(String year : years){
			YearCitationMetricFeatures ycmf = this.map.get(year);
			if(ycmf == null) 
				add(citationMatrix.map.get(year));
			else {
				YearCitationMetricFeatures newYcmf = citationMatrix.getCitationMetricFeatures(year);
				ycmf.addAll(newYcmf);
				citationMetricTypeFeatures.addAll(newYcmf.getTypes());
			}
		}
	}
	
	
	public void add(YearCitationMetricFeatures citationMetricFeatures){
		map.put(citationMetricFeatures.getYear(), citationMetricFeatures);
		citationMetricTypeFeatures.addAll(citationMetricFeatures.getTypes());
	}
	
	public List<CitationMetricTypeFeature> getCitationMetricTypeFeatures() {
		return citationMetricTypeFeatures
				.stream()
				.sorted((cmtf1,cmtf2) -> {
					return cmtf1.getFeatureLabel().compareTo(cmtf2.getFeatureLabel());
				})
				.collect(Collectors.toList());
	}
	
	public double[][] toDoubleMatrix(){
		
		double[][] scoreValues = null;
		int k=0;
		
		List<CitationMetricTypeFeature> cmtfs = getCitationMetricTypeFeatures();
			
		System.out.println("___");
		for(CitationMetricTypeFeature cmtf : cmtfs){
			System.out.println(cmtf.getFeatureLabel());
			List<Double> scores = map.values().stream()
				.map(metrics -> {
					CitationMetricFeature cmf = metrics.getByType(cmtf);
					if(cmf != null){
						return (double)cmf.getCount();
					}
					else return 0.00;
					
				})
				.collect(Collectors.toList());
			if(scoreValues == null)
				scoreValues = new double[scores.size()][citationMetricTypeFeatures.size()];
			
			for(int i=0, j=scores.size(); i<j; i++){
				double s = scores.get(i);
				scoreValues[i][k] = s;
			}
			
			k++;
		}
		
		return scoreValues;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		map.forEach((key,val) -> {
			sb.append(key);
			sb.append(": ");
			boolean[] start = {true};
			val.forEach(cmf -> {
				if(!start[0]) sb.append(", ");
				else start[0] = !start[0];
				String label = cmf.getCitationMetricTypeFeature().getFeatureLabel();
				sb.append(label);
				int count = cmf.getCount();
				sb.append(" (");
				sb.append(count);
				sb.append(")");
			});
			sb.append('\n');
		});
		return sb.toString();
	}
	
	public YearCitationMetricFeatures getCitationMetricFeatures(String year){
		return map.get(year);
	}
	
	public int totalOf(CitationMetricTypeFeature cmtf){
		return map.values()
			.stream()
			.mapToInt(citationFeatures -> {
				CitationMetricFeature cmf = citationFeatures.getByType(cmtf);
				if(cmf == null) return 0;
				else return cmf.getCount();
			})
			.reduce((a,b) -> {return a+b;})
			.getAsInt();
	}
	
	public Map<CitationMetricTypeFeature, Integer> totals(){
		
		Map<CitationMetricTypeFeature, Integer> countMap = new HashMap<CitationMetricTypeFeature, Integer>();
		
		citationMetricTypeFeatures.forEach(cmtf -> {
			int total = totalOf(cmtf);
			countMap.put(cmtf, total);
		});
		
		return countMap;
	}
}
