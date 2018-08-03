package it.cnr.istc.stlab.mira.analytics;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.turtle.TurtleParserFactory;

import it.cnr.istc.stlab.mira.commons.QualificationProcessOntology;

public class DatasetStats {
	
	public static void main(String[] args) {
		long triples = 0;
		
		String mainFolderPath= "/Users/andrea/Desktop/anvur/sessione_1/cv-candidati-rdf";
		File mainFolder = new File(mainFolderPath);
		if(mainFolder.isDirectory()){
			File[] levelFolders = mainFolder.listFiles(f -> f.isDirectory() && !f.isHidden());
			for(File levelFolder : levelFolders){
				File[] academicRecruitmentFolders = levelFolder.listFiles(f->f.isDirectory() && !f.isHidden());
				for(File academicRecruitmentFolder : academicRecruitmentFolders){
					File[] ttls = academicRecruitmentFolder.listFiles(f->f.isFile() && !f.isHidden() && f.getName().endsWith(".ttl"));
					for(File ttl : ttls){
						RDFParser parser = new TurtleParserFactory().getParser();
						Model model = new LinkedHashModel();
						StatementCollector collector = new StatementCollector(model);
						parser.setRDFHandler(collector);
						
						try {
							parser.parse(new FileInputStream(ttl), QualificationProcessOntology.NS);
							
							triples += (long)model.size();
						} catch (RDFParseException | RDFHandlerException | IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		System.out.println("TRIPLES: " + triples);
	}

}
