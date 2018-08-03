package it.cnr.istc.stlab.mira.analytics.classification;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Rates {

	private Map<Integer, Rate> map;
	
	public Rates() {
		this.map = new HashMap<Integer, Rate>();
	}
	
	public Collection<Rate> rates(){
		return map.values();
	}
	
	public Rate getRate(int position){
		return map.get(position);
	}
	
	public void addRate(int position, Rate rate){
		map.put(position, rate);
	}
	
	public void asCSV(OutputStream out){
		try {
			rates().forEach(rate -> {
				try {
					out.write(rate.getRate().getBytes());
					out.write(";".getBytes());
					Double value = rate.getScores().stream().mapToDouble(d->d).average().getAsDouble();
					out.write(value.toString().getBytes());
					out.write('\n');
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			
			
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
