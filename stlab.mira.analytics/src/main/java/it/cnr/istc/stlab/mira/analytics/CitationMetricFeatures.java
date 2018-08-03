package it.cnr.istc.stlab.mira.analytics;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CitationMetricFeatures implements Iterable<CitationMetricFeature>{

	private Set<CitationMetricFeature> citationMetricFeatures;
	
	private String doi;
	
	public CitationMetricFeatures(String doi) {
		this.citationMetricFeatures = new HashSet<CitationMetricFeature>();
		this.doi = doi;
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
	
	public String getDoi() {
		return doi;
	}
	
	public void add(CitationMetricFeature citationMetricFeature){
		citationMetricFeatures.add(citationMetricFeature);
	}
	
	public void sum(CitationMetricFeature citationMetricFeature){
		Iterator<CitationMetricFeature> cmfIt = citationMetricFeatures.iterator();
		boolean found = false;
		
		while(cmfIt.hasNext() && !found){
			CitationMetricFeature cmf = cmfIt.next();
			if(cmf.getCitationMetricTypeFeature().getFeatureLabel().equals(cmf.getCitationMetricTypeFeature().getFeatureLabel())){
				cmf.setCount(cmf.getCount()+citationMetricFeature.getCount());
				found = true;
			}
		}
		if(!found) citationMetricFeatures.add(citationMetricFeature);
	}
	
	public void remove(CitationMetricFeature citationMetricFeature){
		citationMetricFeatures.remove(citationMetricFeature);
	}
	
	public int size(){
		return citationMetricFeatures.size();
	}

}
