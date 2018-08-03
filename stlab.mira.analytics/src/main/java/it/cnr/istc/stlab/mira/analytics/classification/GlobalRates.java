package it.cnr.istc.stlab.mira.analytics.classification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class GlobalRates {

	private Rates trueRates, falseRates, averageRates;
	
	public GlobalRates(Rates trueRates, Rates falseRates, Rates averageRates) {
		this.trueRates = trueRates;
		this.falseRates = falseRates;
		this.averageRates = averageRates;
	}
	
	public Rates getTrueRates() {
		return trueRates;
	}
	
	public Rates getFalseRates() {
		return falseRates;
	}
	
	public Rates getAverageRates() {
		return averageRates;
	}
	
	public void asCSVs(File folder){
		folder.mkdirs();
		File trueF = new File(folder, "true_rates.csv");
		File falseF = new File(folder, "false_rates.csv");
		File averageF = new File(folder, "average_rates.csv");
		try {
			trueRates.asCSV(new FileOutputStream(trueF));
			falseRates.asCSV(new FileOutputStream(falseF));
			averageRates.asCSV(new FileOutputStream(averageF));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
