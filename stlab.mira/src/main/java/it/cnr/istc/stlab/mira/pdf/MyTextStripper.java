package it.cnr.istc.stlab.mira.pdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class MyTextStripper extends PDFTextStripper {
	
	private final double titleX_1 = 70.01000213623047;
	private final double titleX_2 = 43.880001068115234;
	private double previousX = 0.0;
	private final double lineWidth = 16.45996;
	private boolean isTitle = false;
	private boolean isContent = false;
	
	private StringBuilder titleSb = null;
	private StringBuilder contentSb = null;
	private Map<String, String> titles;

	public MyTextStripper() throws IOException {
		super();
		titles = new HashMap<String, String>();
	}
	
	@Override
	protected void processTextPosition(TextPosition text) {
		// TODO Auto-generated method stub
		super.processTextPosition(text);
		
		double x = text.getX();
		//System.out.println("X: " + x + " . " + text.getUnicode());
		
		if(x == titleX_1 || x == titleX_2){
			if(!isTitle){
				if(titleSb != null && contentSb != null){
					String title = titleSb.toString().replaceAll("- [0-9]+ -", "");
					String content = contentSb.toString().replaceAll("- [0-9]+ -", "");
					titles.put(title, content);
				}
				titleSb = new StringBuilder();
				isTitle = true;
				isContent = false;
			}
			else titleSb.append(" ");
		}
		else if(x < previousX && (x > titleX_1 || x > titleX_2) && isTitle) {
			isTitle = false;
			contentSb = new StringBuilder();
			isContent = true;
		}
		else if(x < previousX && !isTitle && isContent)
			contentSb.append(" ");
		
		if(isTitle) titleSb.append(text.getUnicode());
		else if(isContent) contentSb.append(text.getUnicode());
		
		previousX = x;
		
		//System.out.println("::: " + text.getX() + ":" + text.getY() + " - " + text.getUnicode());
	}
	
	
	public Map<String, List<String>> getTitles(PDDocument document) {
		
		
		
		try {
			getText(document);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(titleSb != null && contentSb != null){
			String title = titleSb.toString().replaceAll("- [0-9]+ -", "");
			String content = contentSb.toString().replaceAll("- [0-9]+ -", "");
			titles.put(title, content);
		}
		
		Map<String, List<String>> titles = new HashMap<String, List<String>>();
		this.titles.forEach((title,content)-> {
			Pattern pattern = Pattern.compile("dal [0-9][0-9]-[0-9][0-9]-[0-9][0-9][0-9][0-9] a(l)? (([0-9][0-9]-[0-9][0-9]-[0-9][0-9][0-9][0-9])|oggi)");
			Matcher matcher = pattern.matcher(content);
			
			List<String> tits = new ArrayList<String>();
			int offset = 0;
			while(matcher.find()){
				int end = matcher.end();
				String tit = content.substring(offset, end).trim();
				tits.add(tit);
				offset = end+1;
			}
			titles.put(title, tits);
					
		});
		return titles;
	}
	

}
