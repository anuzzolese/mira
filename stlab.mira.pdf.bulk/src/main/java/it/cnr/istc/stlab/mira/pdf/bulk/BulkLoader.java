package it.cnr.istc.stlab.mira.pdf.bulk;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import it.cnr.istc.stlab.mira.pdf.Main;

public class BulkLoader {

	
	public static final String INPUT_OPTION = "i";
	public static final String INPUT_OPTIONS_LONG = "input";
	
	public static final String OUTPUT_OPTION = "o";
	public static final String OUTPUT_OPTIONS_LONG = "output";
	
	public static void main( String[] args ) {
		
		Options options = new Options();
		
		CommandLine commandLine = null;
        
        CommandLineParser cmdLineParser = new DefaultParser();
        try {
            commandLine = cmdLineParser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "bulkLoader", options );
        }
        
		if(commandLine != null){
        	String[] arguments = commandLine.getArgs();
        	if(arguments.length < 2){
        		System.out.println("No correct arguments provided as input.");
        		System.exit(0);
        	}
        	
        	String sessionFolderPath = commandLine.getArgs()[0];
        	File sessionFolder = new File(sessionFolderPath);
        	if(!sessionFolder.exists() || !sessionFolder.isDirectory()){
        		System.out.println("The folder representing the session does not exist.");
        		System.exit(0);
        	}
        	
        	String rdfFolderPath = commandLine.getArgs()[1];
        	File rdfFolder = new File(rdfFolderPath);
        	rdfFolder.mkdirs();
        	
        	File[] qualificationLevels = sessionFolder.listFiles(f -> {return f.isDirectory();});
        	for(File qualificationLevel : qualificationLevels){
        		System.out.println(qualificationLevel.getName());
        		
        		File[] disciplinarySectors = qualificationLevel.listFiles(f -> {return f.isDirectory();});
        		
        		File qualificationDestinationFolder = new File(rdfFolder, qualificationLevel.getName());
        		qualificationDestinationFolder.mkdirs();
        		
        		for(File disciplinarySector : disciplinarySectors){
        			String disciplinarySectorName = disciplinarySector.getName();
        			if(disciplinarySectorName.equals("01-B1")
        					|| disciplinarySectorName.equals("04-A1") 
        					|| disciplinarySectorName.equals("06-N1")
        					|| disciplinarySectorName.equals("09-H1")
        					|| disciplinarySectorName.equals("13-A1")){
	        			System.out.println('\t' + disciplinarySector.getName());
	        			File[] pdfs = disciplinarySector.listFiles(f -> {return f.getName().endsWith(".pdf");});
	        			
	        			File disciplinarySectorDestinationFolder = new File(qualificationDestinationFolder, disciplinarySector.getName());
	        			disciplinarySectorDestinationFolder.mkdirs();
	        			
	        			List<File> pdfFiles = new ArrayList<File>();
	        			Collections.addAll(pdfFiles, pdfs);
	        			pdfFiles.parallelStream().forEach(pdf -> {
	        				String rdfFileName = pdf.getName().replace(".pdf", ".ttl");
	        				File rdfDestinationFile = new File(disciplinarySectorDestinationFolder, rdfFileName);
	        				try{
	        					Main.main(new String[]{"-o", rdfDestinationFile.getAbsolutePath(), pdf.getAbsolutePath()});
	        				} catch(Exception e){
	        					e.printStackTrace();
	        					System.out.println("PDF " + pdf.getPath());
	        				}
	        				
	        			});
        			}
        		}
    		
        	}
		}	
		
    }
	
}
