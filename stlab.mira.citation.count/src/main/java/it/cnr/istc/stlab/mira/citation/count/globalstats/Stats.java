package it.cnr.istc.stlab.mira.citation.count.globalstats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.turtle.TurtleParserFactory;

import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;

public class Stats {
	
	public void compute(File folder, File outFolder){
		outFolder.mkdirs();
		
		if(folder.exists() && folder.isDirectory()){
			File fistLevel = new File(folder, "fascia-1");
			File secondLevel = new File(folder, "fascia-2");
			
			System.out.println("Fascia 1");
			if(fistLevel.exists() && fistLevel.isDirectory()){
				Writer writer = null;
				try {
					writer = new FileWriter(new File(outFolder, "fascia-1.csv"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(writer != null){
					BufferedWriter bufferedWriter = new BufferedWriter(writer);
					Map<String, PublicationsStats> pubStats = computeStats(fistLevel);
					pubStats.forEach((area,pubStat)->{
						try {
							bufferedWriter.write(area + ";" + pubStat.getApplications() + ";" + pubStat.getTotalCount() + ";" + String.valueOf(pubStat.getAvg()).replace(".", ","));
							bufferedWriter.newLine();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					});
					
					try {
						bufferedWriter.close();
						writer.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
				
			}
			
			System.out.println("Fascia 2");
			if(secondLevel.exists() && secondLevel.isDirectory()){
				Writer writer = null;
				try {
					writer = new FileWriter(new File(outFolder, "fascia-2.csv"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(writer != null){
					BufferedWriter bufferedWriter = new BufferedWriter(writer);
					Map<String, PublicationsStats> pubStats = computeStats(secondLevel);
					pubStats.forEach((area,pubStat)->{
						try {
							bufferedWriter.write(area + ";" + pubStat.getApplications() + ";" + pubStat.getTotalCount() + ";" + String.valueOf(pubStat.getAvg()).replace(".", ","));
							bufferedWriter.newLine();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					});
					
					try {
						bufferedWriter.close();
						writer.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
				
			}
		}
	}
	
	private Map<String, PublicationsStats> computeStats(File level){
		Map<String, PublicationsStats> pubStats = new HashMap<String, PublicationsStats>();
		File[] disciplinaryAreas = level.listFiles(f->(f.isDirectory() && !f.isHidden()));
		for(File disciplinaryArea : disciplinaryAreas){
			System.out.println('\t' + disciplinaryArea.getName());
			File[] ttls = disciplinaryArea.listFiles(f->f.getName().endsWith(".ttl"));
			List<File> ttlList = new ArrayList<File>();
			Collections.addAll(ttlList, ttls);
			
			
			Stream<Integer> authorsPublications = ttlList.parallelStream().map(ttl -> {
				int numberOfPublications = 0;
				Model model = new LinkedHashModel();
				StatementCollector collector = new StatementCollector(model);
								
				RDFParserFactory factory = new TurtleParserFactory();
				RDFParser parser = factory.getParser();
				parser.setRDFHandler(collector);
				
				InputStream is;
				try {
					is = new FileInputStream(ttl);
					parser.parse(is, QualificationProcessOntology.NS);
					is.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(!model.isEmpty()){
					Set<Value> publicationsList = model.filter(null, QualificationProcessOntology.hasPublications, null).objects();
					for(Value publicationList : publicationsList){
						if(publicationList instanceof URI){
							numberOfPublications = model.filter((URI)publicationList, QualificationProcessOntology.hasPart, null).objects().size();
						}
					}
				}
				return numberOfPublications;
			});
			
			List<Integer> counts = authorsPublications.collect(Collectors.toList());
			
			long applications = ttlList.parallelStream().count();
			int total = counts.parallelStream().mapToInt(i->i).sum();
			double avg = counts.parallelStream().mapToInt(i->i).average().getAsDouble();
			PublicationsStats publicationsStats = new PublicationsStats(applications, total, avg);
			
			pubStats.put(disciplinaryArea.getName(), publicationsStats);
			
		}
		
		return pubStats;
		
	}
	
	private class PublicationsStats{
		private long applications;
		private int totalCount;
		private double avg;
		
		public PublicationsStats(long applications, int totalCount, double avg) {
			this.applications = applications;
			this.totalCount = totalCount;
			this.avg = avg;
		}
		
		public long getApplications() {
			return applications;
		}
		
		public int getTotalCount() {
			return totalCount;
		}
		
		public double getAvg() {
			return avg;
		}
	}
	
	public static void main(String[] args) {
		Stats stats = new Stats();
		stats.compute(new File("/Users/andrea/Desktop/anvur/sessione_1/cv-candidati-rdf"), new File("/Users/andrea/Desktop/anvur/sessione_1/stats"));
	}

}
