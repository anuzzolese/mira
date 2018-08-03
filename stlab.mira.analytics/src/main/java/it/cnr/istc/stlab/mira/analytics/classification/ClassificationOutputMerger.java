package it.cnr.istc.stlab.mira.analytics.classification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class ClassificationOutputMerger {

	
	public static void main(String[] args) {
		String folderPath = "/Users/andrea/Desktop/anvur/sessione_1/step7-classification/output_new";
		
		String[] fasce = {"fascia-1", "fascia-2"};
		String[] disciplinaryFields = {"01-B1", "04-A1", "06-N1", "09-H1", "13-A1"};
		
		for(String fascia : fasce){
			for(String disciplinaryField : disciplinaryFields){
				File disciplineFolder = new File(folderPath, disciplinaryField);
				File fasciaFolder = new File(disciplineFolder, fascia);
				
				File[] featureFolders = fasciaFolder.listFiles(f->f.isDirectory() && !f.isHidden());
				
				boolean headers = true;
				Map<Integer, List<String>> sbMap = new HashMap<Integer, List<String>>();
				for(File featureFolder : featureFolders){
					try {
						CSVReader reader = new CSVReader(new FileReader(new File(featureFolder, "average_rates.csv")), ';');
						CSVWriter writer = new CSVWriter(new FileWriter(new File(fasciaFolder, "average_" + fascia + "_resume.csv")), ';');
						
						String[] row = null;
						
						int rowCounter = 0;
						List<String> columns = sbMap.get(rowCounter);
						if(columns == null){
							columns = new ArrayList<String>();
							sbMap.put(rowCounter, columns);
						}
						if(headers) columns.add("");
						
						String header = null;
						switch(featureFolder.getName()){
						case "SELECTED_METRICS_ATTRIBUTES":
							header = "Citations + Captures" + '\n' + "(count + h-index))";
							break;
						case "ALTMETRICS_CITATIONS_COUNT":
							header = "Altmetrics" + '\n' + "(count only)";
							break;
						case "SELECTED_H_INDEXES":
							header = "Citations + Captures" + '\n' + "(h-index only)";
							break;
						case "ALTMETRICS_ONLY_H_INDEXES":
							header = "Altmetrics" + '\n' + "(h-index only)";
							break;
						case "ALT_METRICS_ATTRIBUTES":
							header = "Altmetrics" + '\n' + "(count + h-index)";
							break;
						case "CITATION_CITATIONS_COUNT":
							header = "Citations" + '\n' + "(count only)";
							break;
						case "SELECTED_CITATIONS_COUNT":
							header = "Citations + Captures" + '\n' + "(count only)";
							break;
						case "FULL_CITATIONS_COUNT":
							header = "Citations + Altmetrics" + '\n' + "(count only)";
							break;
						case "TRADITIONAL_METRICS_ATTRIBUTES":
							header = "Citations" + '\n' + "(count + h-index)";
							break;
						case "FULL_ATTRIBUTES":
							header = "Citations + Altmetrics" + '\n' + "(count + h-index)";
							break;
						case "FULL_H_INDEXES":
							header = "Citations + Altmetrics" + '\n' + "(h-index only)";
							break;
						case "CITATIONS_ONLY_H_INDEXES":
							header = "Citations" + '\n' + "(h-index only)";
							break;
						}
						
						System.out.println(header);
						
						columns.add(header);
						
						rowCounter++;
						while((row = reader.readNext()) != null){
							
							columns = sbMap.get(rowCounter);
							if(columns == null){
								columns = new ArrayList<String>();
								sbMap.put(rowCounter, columns);
							}
							
							if(headers){
								columns.add(row[0]);
								columns.add(row[1].replace(".", ","));
							}							
							else{
								columns.add(row[1].replace(".", ","));
							}
							rowCounter++;
						}
						
						if(headers) headers = false;
						
						List<Integer> rowIds = sbMap.keySet().stream().sorted().collect(Collectors.toList());
						for(Integer rowId : rowIds){
							columns = sbMap.get(rowId);
							String[] line = new String[columns.size()];
							line = columns.toArray(line);
							writer.writeNext(line);
						}
						
						writer.flush();
						writer.close();
						reader.close();
						
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		}

	}
}
