package it.cnr.istc.stlab.mira.analytics;

import org.openrdf.model.URI;

public class CitationMetricTypeFeature {

	private URI featureURI;
	private String featureLabel;
	
	public CitationMetricTypeFeature(URI featureURI, String featureLabel) {
		this.featureURI = featureURI;
		this.featureLabel = featureLabel;
	}
	
	public URI getFeatureURI() {
		return featureURI;
	}
	
	public String getFeatureLabel() {
		return featureLabel;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CitationMetricTypeFeature){
			CitationMetricTypeFeature citationMetricFeature = (CitationMetricTypeFeature) obj;
			return citationMetricFeature.getFeatureURI().equals(this.featureURI) 
					&& citationMetricFeature.getFeatureLabel().equals(this.featureLabel) 
					? true : false;
		}
		else return false;
	}
	
	
	@Override
	public int hashCode() {
		//return (featureURI + featureLabel).hashCode();
		return featureLabel.hashCode();
	}
	
}
