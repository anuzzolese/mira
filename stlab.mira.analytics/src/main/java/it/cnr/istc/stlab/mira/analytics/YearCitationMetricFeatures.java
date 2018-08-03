package it.cnr.istc.stlab.mira.analytics;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class YearCitationMetricFeatures implements Iterable<CitationMetricFeature>{

	private Set<CitationMetricFeature> citationMetricFeatures;
	
	private String year;
	
	public YearCitationMetricFeatures(String year) {
		this.citationMetricFeatures = new HashSet<CitationMetricFeature>();
		this.year = year;
	}
	
	@Override
	public Iterator<CitationMetricFeature> iterator() {
		return citationMetricFeatures.iterator();
	}
	
	public Set<CitationMetricTypeFeature> getTypes(){
		Set<CitationMetricTypeFeature> types = new HashSet<CitationMetricTypeFeature>();
		for(CitationMetricFeature cmf : citationMetricFeatures)
			types.add(cmf.getCitationMetricTypeFeature());
		
		return types;
	}
	
	public CitationMetricFeature getByType(CitationMetricTypeFeature cmtf){
		Iterator<CitationMetricFeature> it = citationMetricFeatures.iterator();
		
		while(it.hasNext()){
			CitationMetricFeature cmf = it.next();
			if(cmf.getCitationMetricTypeFeature().equals(cmtf))
				return cmf;
		}
		return null;
	}
	
	public String getYear() {
		return year;
	}
	
	public void add(CitationMetricFeature citationMetricFeature){
		citationMetricFeatures.add(citationMetricFeature);
	}
	
	public void sum(CitationMetricFeature citationMetricFeature){
		Iterator<CitationMetricFeature> cmfIt = citationMetricFeatures.iterator();
		boolean found = false;
		
		while(cmfIt.hasNext() && !found){
			CitationMetricFeature cmf = cmfIt.next();
			if(citationMetricFeature.getCitationMetricTypeFeature().getFeatureLabel().equals(cmf.getCitationMetricTypeFeature().getFeatureLabel())){
				cmf.setCount(cmf.getCount()+citationMetricFeature.getCount());
				found = true;
			}
		}
		if(!found) citationMetricFeatures.add(citationMetricFeature);
		
		//System.out.println(year + " - " + citationMetricFeature.getCitationMetricTypeFeature().getFeatureLabel() + " - " + citationMetricFeatures.size());
	}
	
	public void addAll(YearCitationMetricFeatures yearCitationMetricFeatures){
		yearCitationMetricFeatures.citationMetricFeatures.forEach(cmf -> {
			Iterator<CitationMetricFeature> cmfIt = this.citationMetricFeatures.iterator();
			boolean found = false;
			
			while(cmfIt.hasNext() && !found){
				CitationMetricFeature thisCmf = cmfIt.next();
				if(cmf.getCitationMetricTypeFeature().getFeatureLabel().equals(thisCmf.getCitationMetricTypeFeature().getFeatureLabel())){
					cmf.setCount((cmf.getCount()+thisCmf.getCount())/2);
					found = true;
				}
			}
			if(!found) citationMetricFeatures.add(cmf);
		});
	}
	
	public void remove(CitationMetricFeature citationMetricFeature){
		citationMetricFeatures.remove(citationMetricFeature);
	}
	
	public int size(){
		return citationMetricFeatures.size();
	}

}
