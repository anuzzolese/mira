package it.cnr.istc.stlab.mira.analytics.qualified;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import au.com.bytecode.opencsv.CSVReader;

public class CSVNormalizer {

	
	public static void main(String[] args) {
		
		String[] disciplinaryAreas = {"01-B1", "04-A1", "06-N1", "09-H1", "13-A1"};
		String[] fasce = {"fascia-1", "fascia-2"};
		String[] years= {"2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013" ,"2014", "2015", "2016"};
		
		String path = "/Users/andrea/Desktop/anvur/sessione_1/step6-analytics-by-year/MetricheLivello";
		CSVNormalizer csvNormalizer = new CSVNormalizer();
		
		for(int level=1; level<4; level++){
			List<AnvurCSV> anvurCSVs = new ArrayList<AnvurCSV>();
			for(String disciplinaryArea : disciplinaryAreas){
				for(String fascia : fasce){
					
					AnvurCSV anvurCSV = csvNormalizer.new AnvurCSV(disciplinaryArea, fascia);
					File csv = new File(path + level + "/" + disciplinaryArea + "/" + fascia + "/correlations.csv");
					try {
						CSVReader reader = new CSVReader(new FileReader(csv), ';');
						
						String[] line = null;
						int row=0;
						String[] headers = null;
						while((line = reader.readNext()) != null){
							if(row == 0){
								headers = new String[line.length-1];
								for(int i=1; i<line.length; i++){
									headers[i-1] = line[i].trim();
								}
								
								int index = 0;
								for(AnvurCSV previousAnvurCSV : anvurCSVs){
									for(String header : headers){
										previousAnvurCSV.putMetrics(header, years);
									}
									if(index == 0){
										Set<String> metrics = previousAnvurCSV.getMetrics();
										for(String metric : metrics){
											boolean contains = false;
											for(int i=0; !contains && i<headers.length; i++){
												String header = headers[i];
												if(header.equals(metric)) contains = true;
											}
											if(!contains) {
												anvurCSV.putMetrics(metric, years);
											}
										}
									}
									index++;
								}
							}
							else{
								String year = line[0];
								for(int i=0; i<headers.length; i++){
									String metrics = headers[i];
									String value = line[i+1];
									anvurCSV.putMetrics(year, metrics, value);
								}
							}
							row++;
						}
						
						reader.close();
						
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					anvurCSVs.add(anvurCSV);
				}
			}
			
			for(AnvurCSV anvurCSV : anvurCSVs){
				File f = new File(path + level + "/" + anvurCSV.disciplinaryArea + "/" + anvurCSV.academicLevel + "/correlations_.csv");
				try {
					anvurCSV.write(new FileOutputStream(f));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	class AnvurCSV {
		private String disciplinaryArea;
		private String academicLevel;
		private Map<String, Map<String, String>> yearMap;
		
		public AnvurCSV(String disciplinaryArea, String academicLevel) {
			this.disciplinaryArea = disciplinaryArea;
			this.academicLevel = academicLevel;
			
			yearMap = new HashMap<String, Map<String, String>>();
		}
		
		public void putMetrics(String metrics, String...years){
			for(String year : years){
				Map<String, String> metricsMap = yearMap.get(year);
				if(metricsMap == null) {
					metricsMap = new HashMap<String,String>();
					yearMap.put(year, metricsMap);
				}
				if(!metricsMap.containsKey(metrics))
					metricsMap.put(metrics, "0");
			}
		}
		
		Set<String> getMetrics(){
			Set<String> metrics = new HashSet<String>();
			yearMap.values().stream().forEach(m->{
				metrics.addAll(m.keySet());
			});
			return metrics;
		}
		
		public void putMetrics(String year, String metrics, String value){
			Map<String, String> metricsMap = this.yearMap.get(year);
			if(metricsMap == null) {
				metricsMap = new HashMap<String, String>();
				this.yearMap.put(year, metricsMap);
			}
			metricsMap.put(metrics, value);
		}
		
		public void write(OutputStream out){
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
			
			List<String> years = yearMap.keySet().stream().sorted().collect(Collectors.toList());
			
			try {
				int row = 0;
				for(String year : years){
					Map<String, String> metricsMap = yearMap.get(year);
					List<String> metrics = metricsMap.keySet().stream().sorted().collect(Collectors.toList());
					if(row == 0){
						writer.write("Year");
						for(String metric : metrics){
							writer.write(";");
							writer.write(metric);
						}
						writer.flush();
						
					}
					
					writer.newLine();
					writer.write(year);
					for(String metric : metrics){
						writer.write(";");
						writer.write(metricsMap.get(metric));
					}
										
					row++;
				}
			
			
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
}
