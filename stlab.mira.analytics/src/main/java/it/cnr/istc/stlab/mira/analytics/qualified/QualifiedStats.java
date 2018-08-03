package it.cnr.istc.stlab.mira.analytics.qualified;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import au.com.bytecode.opencsv.CSVWriter;

public class QualifiedStats {
	
	public static int countQualified(File disciplinaryHtml){
		
		System.out.println(disciplinaryHtml.getAbsolutePath());
		int qualified = 0;
		try {
			Document document = Jsoup.parse(disciplinaryHtml, "UTF-8");
			
			Element table = document.select("table").get(0);
			Elements rows = table.select("tr");
			
			qualified = rows.size();
			
			
		} catch (Exception e) {
			System.err.println("0");
		}
		
		return qualified;
	}
	
	public static void main(String[] args) {
		String[] levels = {"fascia-1", "fascia-2"}; 
		File inputFolder = new File("/Users/andrea/Desktop/anvur/sessione_1/cv-candidati");
		
		for(String level : levels){
			File levelFolder = new File(inputFolder, level);
			
			CSVWriter writer = null;
			try {
				writer = new CSVWriter(new FileWriter("qualified_stats_" + level + ".csv"), ';');
				writer.writeNext(new String[]{"Field", "# of apps", "# of qualified"});
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//System.out.println(levelFolder.getAbsolutePath());
			File[] fieldFolders = levelFolder.listFiles(f -> f.isDirectory() && !f.isHidden());
			for(File fieldFolder : fieldFolders){
				File[] applications = fieldFolder.listFiles(f -> f.isFile() && !f.isHidden() && f.getName().endsWith(".pdf"));
				int numberOfApplications = applications.length;
				System.out.println("cazzo");
				File html = new File("/Users/andrea/Desktop/anvur/sessione_1/lista_abilitati/sessione-1/" + level + "/" + fieldFolder.getName() + ".html");
				int numberOfQualified = QualifiedStats.countQualified(html);
				
				if(writer != null){
					writer.writeNext(new String[]{fieldFolder.getName(), String.valueOf(numberOfApplications), String.valueOf(numberOfQualified)});
				}
			}
			if(writer != null){
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
