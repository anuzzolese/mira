package it.cnr.istc.stlab.mira.analytics.classification;

import java.util.ArrayList;
import java.util.List;

public class Rate{
	private String rate;
	private List<Double> scores;
	
	public Rate(String rate) {
		this.rate = rate;
		this.scores = new ArrayList<Double>();
	}
	
	public Rate(String rate, List<Double> scores) {
		this.rate = rate;
		this.scores = scores;
	}
	
	public String getRate() {
		return rate;
	}
	
	public List<Double> getScores() {
		return scores;
	}
}