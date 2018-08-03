package it.cnr.istc.stlab.mira.pdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Option.Builder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.turtle.TurtleWriterFactory;

import com.bigdata.rdf.internal.XSD;

public class Main {
	
	
	
	public static final String OUTPUT_OPTION = "o";
	public static final String OUTPUT_OPTIONS_LONG = "output";
	
	public static void main( String[] args ) {
		
		Options options = new Options();
        
        Builder optionBuilder = Option.builder(OUTPUT_OPTION);
        Option outputOption = optionBuilder.argName("file")
                .desc("The name of the file where to store the resulting RDF. If no file is provided the RDF is printed on screen.")
                .hasArg()
                .required(false)
                .longOpt(OUTPUT_OPTIONS_LONG)
                .build();
        
        options.addOption(outputOption);
        
        CommandLine commandLine = null;
        
        CommandLineParser cmdLineParser = new DefaultParser();
        try {
            commandLine = cmdLineParser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "pdfParser", options );
        }
        
        PDDocument document = null;
		try {
    		
			if(commandLine != null){
	        	String[] arguments = commandLine.getArgs();
	        	if(arguments.length == 0){
	        		System.out.println("No file provided as input.");
	        		System.exit(0);
	        	}
	        	
	        	String pdfFilePath = commandLine.getArgs()[0];
	        	if(!pdfFilePath.endsWith(".pdf")){
	        		System.out.println("The file provided as input is not a PDF.");
	        		System.exit(0);
	        	}
	        	
	        	File inFile = new File(pdfFilePath);
	    		document = PDDocument.load(inFile);
	    		
	    		Model model = AnvurPDFApplicationParser.parse(document);
				
				OutputStream out = System.out;
				if(commandLine.hasOption(OUTPUT_OPTION)){
					try {
						String value = commandLine.getOptionValue(OUTPUT_OPTION);
						if(value != null){
							File outFile = new File(value);
							outFile = new File(outFile.getAbsolutePath());
							if(!outFile.getParentFile().exists()) outFile.getParentFile().mkdirs();
							
							out = new FileOutputStream(new File(value));
						}
						else out = System.out;
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						out = System.out;
					}
				}
				
				RDFWriter writer = new TurtleWriterFactory().getWriter(out);
				writer.handleNamespace("ref-list", "http://w3id.org/anvur/nq/data/reference-list/");
				writer.handleNamespace("biblio-item", "http://w3id.org/anvur/nq/data/bibliographic-item/");
				writer.handleNamespace("biblio-item-type", "http://w3id.org/anvur/nq/data/bibliographic-item-type/");
				writer.handleNamespace("application", "http://w3id.org/anvur/nq/data/application/");
				writer.handleNamespace("person", "http://w3id.org/anvur/nq/data/person/");
				writer.handleNamespace("resume", "http://w3id.org/anvur/nq/data/resume/");
				writer.handleNamespace("qualification-type", "http://w3id.org/anvur/nq/data/qualification-type/");
				writer.handleNamespace("qualification", "http://w3id.org/anvur/nq/data/qualification/");
				writer.handleNamespace("qualification-list", "http://w3id.org/anvur/nq/data/qualification-list/");
				writer.handleNamespace("publication", "http://w3id.org/anvur/nq/data/publication/");
				writer.handleNamespace("ont", "http://w3id.org/anvur/nq/ontology/");
				writer.handleNamespace("rdf", RDF.NAMESPACE);
				writer.handleNamespace("rdfs", RDFS.NAMESPACE);
				writer.handleNamespace("xsd", XSD.NAMESPACE);
				
				writer.startRDF();
				for(Statement stmt : model)
					writer.handleStatement(stmt);
				writer.endRDF();
				out.close();
				
			}
    		
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(document != null) document.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
    }
}
