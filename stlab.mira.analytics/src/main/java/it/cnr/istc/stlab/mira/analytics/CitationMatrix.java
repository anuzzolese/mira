package it.cnr.istc.stlab.mira.analytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CitationMatrix {
	
	private Map<String, CitationMetricFeatures> map;
	private Set<CitationMetricTypeFeature> citationMetricTypeFeatures;
	
	public CitationMatrix() {
		this.map = new HashMap<String, CitationMetricFeatures>();
		citationMetricTypeFeatures = new HashSet<CitationMetricTypeFeature>();
	}
	
	public int size(){
		return map.size();
	}
	
	public void addAll(CitationMatrix citationMatrix) {
		Set<String> dois = citationMatrix.map.keySet();
		for(String doi : dois)
			if(!this.map.containsKey(doi)) 
				add(citationMatrix.map.get(doi));
	}
	
	public void add(CitationMetricFeatures citationMetricFeatures){
		map.put(citationMetricFeatures.getDoi(), citationMetricFeatures);
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
			
		for(CitationMetricTypeFeature cmtf : cmtfs){
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
	
	public String[][] toDoubleMatrixWithDOIs(){
		
		String[][] scoreValues = null;
		
		List<CitationMetricTypeFeature> cmtfs = getCitationMetricTypeFeatures();
			
		
		List<List<String>> listOfScoreValues = new ArrayList<List<String>>();
		map.keySet().forEach(doi -> {
			List<String> scores = new ArrayList<String>();
			scores.add(doi);
			CitationMetricFeatures metrics = map.get(doi);
			for(CitationMetricTypeFeature cmtf : cmtfs){
				
				CitationMetricFeature cmf = metrics.getByType(cmtf);
				if(cmf != null) scores.add(String.valueOf(cmf.getCount()));
				else scores.add("0.0");
			}
			
			listOfScoreValues.add(scores);
			
			
		});
		
		scoreValues = new String[listOfScoreValues.size()][listOfScoreValues.get(0).size()];
		for(int i=0; i<scoreValues.length; i++){
			for(int j=0; j<scoreValues[i].length; j++){
				scoreValues[i][j] = listOfScoreValues.get(i).get(j);
			}
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
	
	public CitationMetricFeatures getCitationMetricFeatures(String doi){
		return map.get(doi);
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
