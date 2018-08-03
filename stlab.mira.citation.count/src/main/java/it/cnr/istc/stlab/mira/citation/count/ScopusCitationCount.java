package it.cnr.istc.stlab.mira.citation.count;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class ScopusCitationCount {

	
	
	public ScopusCitationCount() {
		
	}
	
	public int count(String doi){
		int citations = 0;
		
		URL url = ScopusAPIConfig.buildUrlRequest(doi);
		if(url != null){
			URLConnection connection;
			try {
				connection = url.openConnection();
				connection.setRequestProperty("Accept", "application/json");
				
				InputStream is = connection.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
				
				StringBuilder sb = new StringBuilder();
				String line = null;
				while((line = reader.readLine()) != null){
					sb.append(line);
				}
				
				JSONObject json = new JSONObject(sb.toString());
				if(json != null){
					JSONObject response = json.getJSONObject(ScopusAPIConfig.RESULTING_JSON_RESPONSE_KEY);
					if(response != null){
						JSONObject document = response.getJSONObject(ScopusAPIConfig.RESULTING_JSON_DOCUMENT_KEY);
						if(document != null){
							citations = document.getInt(ScopusAPIConfig.RESULTING_JSON_CITATIO_COUNT_KEY);
						}
					}
					
				}
				
				reader.close();
				is.close();
			} catch (IOException e) {
				citations = -2;
			} catch (JSONException e) {
				citations = -1;
			}
			
		}
		
		return citations;
	}
	
	public static void main(String[] args) {
		ScopusCitationCount scc = new ScopusCitationCount();
		int count = scc.count("10.3233/SW-2010-0020");
		System.out.println("Citation count: " + count);
	}
	
}
