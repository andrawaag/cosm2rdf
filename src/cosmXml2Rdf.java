import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;

import sun.misc.resources.*;


public class cosmXml2Rdf {

	/**
	 * @param args
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, MalformedURLException, SAXException, IOException {
		String cosmFeedId = constants.feedId();
		String urlString = "https://api.cosm.com/v2/feeds/"+cosmFeedId+".xml";
	    DefaultHttpClient httpclient = new DefaultHttpClient();    
	    HttpGet httpget = new HttpGet(urlString);
	    httpget.setHeader("X-ApiKey", constants.apiKey());
	    
	    
	    System.out.println("executing request" + httpget.getRequestLine());
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        System.out.println("----------------------------------------");
        System.out.println(response.getStatusLine());
        if (entity != null) {
            System.out.println("Response content length: " + entity.getContentLength());
        }
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        InputStream in = response.getEntity().getContent();
        Document doc = docBuilder.parse(in);
        System.out.println(doc.getElementsByTagName("lat").item(0).getTextContent());
        Model cosmModel = ModelFactory.createDefaultModel();
        Resource cosmFeed = cosmModel.createResource();
        cosmFeed.addProperty(Wgs84_pos.lat, doc.getElementsByTagName("lat").item(0).getTextContent());
        cosmFeed.addProperty(Wgs84_pos.long_, doc.getElementsByTagName("lon").item(0).getTextContent());
        cosmFeed.addProperty(DCTerms.title, doc.getElementsByTagName("title").item(0).getTextContent());
       
       NodeList includedSensors = doc.getElementsByTagName("data");
       for (int i=0;i<includedSensors.getLength(); i++){
    	   Resource sensorResource = cosmModel.createResource("#"+includedSensors.item(i).getAttributes().getNamedItem("id").getTextContent());
    	   sensorResource.addProperty(DCTerms.isPartOf, cosmFeed);
    	   Element sensorRoot = (Element) includedSensors.item(i);
    	   sensorResource.addLiteral(Qudt.value, sensorRoot.getElementsByTagName("current_value").item(0).getTextContent());
    	   sensorResource.addLiteral(Qudt.unit, sensorRoot.getElementsByTagName("unit").item(0).getTextContent());
       }
        
        
        
        FileOutputStream fout;
		fout = new FileOutputStream("/tmp/cosm.ttl");
		cosmModel.write(fout, "TURTLE");
        

        // When HttpClient instance is no longer needed, 
        // shut down the connection manager to ensure
        // immediate deallocation of all system resources
       // httpclient.getConnectionManager().shutdown(); 
	    
	    //System.out.println(response.getEntity().getContent());
	  
	    
	    

	    	


	  
	  }
		
		
		
		


/*		

        Model cosmModel = ModelFactory.createDefaultModel();
        Resource cosmFeed = cosmModel.createResource("https://api.cosm.com/v2/feeds/"+cosmFeedId+".rdf");
        cosmFeed.addProperty(Wgs84_pos.lat, doc.getElementsByTagName("lat").toString());
        cosmFeed.addProperty(Wgs84_pos.long_, doc.getElementsByTagName("long").toString());
	}*/

}
