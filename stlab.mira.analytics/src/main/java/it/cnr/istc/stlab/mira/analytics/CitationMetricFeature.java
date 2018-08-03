package it.cnr.istc.stlab.mira.analytics;

import org.openrdf.model.URI;

public class CitationMetricFeature {

	private CitationMetricTypeFeature citationMetricTypeFeature;
	private URI paperURI;
	private String doi;
	private String year;
	private int count;
	
	public CitationMetricFeature(URI paperURI,
			String doi,
			CitationMetricTypeFeature citationMetricTypeFeature,
			int count) {
		this.paperURI = paperURI;
		this.doi = doi;
		this.citationMetricTypeFeature = citationMetricTypeFeature;
		this.count = count;
	}
	
	public CitationMetricFeature(URI paperURI,
			String doi,
			String year,
			CitationMetricTypeFeature citationMetricTypeFeature,
			int count) {
		this(paperURI, doi, citationMetricTypeFeature, count);
		this.year = year;
	}
	
	public URI getPaperURI() {
		return paperURI;
	}
	
	public String getDoi() {
		return doi;
	}
	
	public String getYear() {
		return year;
	}
	
	public CitationMetricTypeFeature getCitationMetricTypeFeature() {
		return citationMetricTypeFeature;
	}
	
	public boolean isOfType(CitationMetricTypeFeature cmtf){
		return cmtf.equals(this.citationMetricTypeFeature);
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CitationMetricFeature){
			CitationMetricFeature citationMetricFeature = (CitationMetricFeature) obj;
			return citationMetricFeature.getCitationMetricTypeFeature().equals(this.citationMetricTypeFeature) 
					&& citationMetricFeature.getPaperURI().equals(this.paperURI)
					&& (citationMetricFeature.getCount() == this.count)
					? true : false;
		}
		else return false;
	}
	
	
	@Override
	public int hashCode() {
		//return (citationMetricTypeFeature.hashCode() + (paperURI.toString() + count)).hashCode();
		return citationMetricTypeFeature.getFeatureLabel().hashCode();
	}
	
}
