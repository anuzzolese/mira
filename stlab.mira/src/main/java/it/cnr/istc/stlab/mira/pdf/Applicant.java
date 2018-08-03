package it.cnr.istc.stlab.mira.pdf;

import org.apache.commons.lang3.StringUtils;

public class Applicant {

	private String fullName, givenName, familyName;
	
	public Applicant(String fullName) {
		this.fullName = fullName;
		
		String[] nameParts = fullName.split(" ");
		StringBuilder fnSb = new StringBuilder();
		StringBuilder gnSb = new StringBuilder();
		for(String namePart : nameParts){
			if(StringUtils.isAllUpperCase(namePart)){
				if(fnSb.length() > 0) fnSb.append(" ");
				fnSb.append(namePart);
			}
			else {
				if(gnSb.length() > 0) gnSb.append(" ");
				gnSb.append(namePart);
			}
		}
		
		this.givenName = gnSb.toString();
		this.familyName = fnSb.toString();
		
		
	}
	
	public String getFullName() {
		return fullName;
	}
	
	public String getGivenName() {
		return givenName;
	}
	
	public String getFamilyName() {
		return familyName;
	}
	
}
