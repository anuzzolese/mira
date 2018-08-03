package it.cnr.istc.stlab.mira.analytics.classification;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Attributes {
	
	public static final String[] FULL_ATTRIBUTES = {"Captures","Citations","H-Index captures","H-Index citations","H-Index mentions","H-Index social-media","H-Index usage","Mentions","Social Media","Usage"};

	public static final String[] TRADITIONAL_METRICS_ATTRIBUTES = {"Citations","H-Index citations"};
	
	public static final String[] ALT_METRICS_ATTRIBUTES = {"Captures","H-Index captures","H-Index mentions","H-Index social-media","H-Index usage","Mentions","Social Media","Usage"};
	
	public static final String[] SELECTED_METRICS_ATTRIBUTES = {"Captures","Citations","H-Index captures","H-Index citations"};
	
	public static final String[] FULL_H_INDEXES = {"H-Index captures","H-Index citations","H-Index mentions","H-Index social-media","H-Index usage"};
	
	public static final String[] CITATIONS_ONLY_H_INDEXES = {"H-Index citations"};
	
	public static final String[] ALTMETRICS_ONLY_H_INDEXES = {"H-Index captures","H-Index mentions","H-Index social-media","H-Index usage"};
	
	public static final String[] SELECTED_H_INDEXES = {"H-Index captures","H-Index mentions"};
	
	public static final String[] FULL_CITATIONS_COUNT = {"Captures","Citations","Mentions","Social Media","Usage"};
	
	public static final String[] CITATION_CITATIONS_COUNT = {"Citations"};

	public static final String[] ALTMETRICS_CITATIONS_COUNT = {"Captures","Mentions","Social Media","Usage"};
	
	public static final String[] SELECTED_CITATIONS_COUNT = {"Captures","Citations"};
	
	public static List<String> getFeatureAttributes(){
		List<String> featureAttributes = new ArrayList<String>();
		Field[] fields = Attributes.class.getFields();
		
		for(Field field : fields){
			featureAttributes.add(field.getName());
		}
		
		return featureAttributes;
	}
	
	public static String[] getFeaturesOf(String attribute){
		String[] features = null;
		try {
			Field field = Attributes.class.getField(attribute);
			features = (String[]) field.get(String[].class);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return features;
	}
	
	public static void main(String[] args) {
		for(String attribute : Attributes.getFeatureAttributes()){
			System.out.println(attribute);
			for(String feature : Attributes.getFeaturesOf(attribute)){
				System.out.println('\t' + feature);
			}
		}
	}

}
