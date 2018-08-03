package it.cnr.istc.stlab.mira.analytics.qualified;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class YearCSVResumeCreator {

	public static void main(String[] args) {
		
		String fileName = "pvalues.csv";
		
		String[] disciplinaryAreas = {"01-B1", "04-A1", "06-N1", "09-H1", "13-A1"};
		String[] years = {"2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016"};
		for(String disciplinaryArea : disciplinaryAreas){
			for(int metricsLevel = 1; metricsLevel<4; metricsLevel++){
				for(int level = 1; level<3; level++){
					String outfile = "/Users/andrea/Desktop/anvur/sessione_1/step6-analytics-by-year/MetricheLivello" + metricsLevel + "/" + disciplinaryArea + "/fascia-" + level + "/" + fileName;
					try {
						CSVWriter writer = new CSVWriter(new FileWriter(outfile), ';');
					
						Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
						for(String year : years){
							String file = "/Users/andrea/Desktop/anvur/sessione_1/step6-analytics-by-year/MetricheLivello" + metricsLevel + "/" + disciplinaryArea + "/fascia-" + level + "/" + year + "/" + fileName;
							
							try {
								CSVReader reader = new CSVReader(new FileReader(file), ';');
								
								String[] headers = null;
								String[] row = null;
								boolean stop = false;
								for(int i=0; !stop && ((row = reader.readNext()) != null); i++) {
									if(i==0) headers = row;
									else if(row[0].equals("(Citations)Citations") || row[0].equals("Citations")){
										stop = true;
										System.out.println(row[0]);
									}
								}
								
								Map<String, String> yearMap = new HashMap<String, String>();
								for(int k=1; k<row.length; k++){
									yearMap.put(headers[k], row[k]);
								}
									
								
								for(int i=Integer.valueOf(year); i>2005;i--){
									Map<String, String> previousYearMap = map.get(String.valueOf(i));
									if(previousYearMap != null){
										for(String key : yearMap.keySet()){
											if(!previousYearMap.containsKey(key)) previousYearMap.put(key, "0");
										}
										for(String key : previousYearMap.keySet()){
											if(!yearMap.containsKey(key)) yearMap.put(key, "0");
										}
									}
								}
								
								map.put(year, yearMap);
								
								reader.close();
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						boolean headersWritten = false;
						List<String> ys = map.keySet()
							.stream()
							.sorted()
							.collect(Collectors.toList());
						
						for(String y : ys){
							Map<String, String> yearMap = map.get(y);
							Set<String> headers = yearMap.keySet();
							
							List<String> hs = headers
									.stream()
									.sorted()
									.collect(Collectors.toList());
							
							int size = hs.size();
							if(!headersWritten){
								headersWritten = true;
								String[] headersArr = new String[size];
								headersArr[0] = "Year";
								int z=1; 
								for(String head : hs) {
									if(!head.equals("(Citations)Citations") && !head.equals("Citations")){
										headersArr[z] = head;
										z++;
									}
								}
								writer.writeNext(headersArr);
							}
							
							String[] row = new String[size];
							row[0] = y;
							int z=1;
							for(String head : hs) {
								if(!head.equals("(Citations)Citations") && !head.equals("Citations")){
									row[z] = yearMap.get(head);
									z++;
								}
							}
							writer.writeNext(row);
							
						}
						
						writer.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
						
				}
				
			}
		}
		 
	}
	
}
