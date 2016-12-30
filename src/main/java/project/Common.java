package project;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class Common {

	public final static File f1 = new File("src/main/resources/sequoia-corpus-v4.0/annodis.er.xml");
	public final static File f2 = new File("src/main/resources/sequoia-corpus-v4.0/emea-fr-dev.xml");
	public final static File f3 = new File("src/main/resources/sequoia-corpus-v4.0/Europar.550.xml");
	public final static File f4 = new File("src/main/resources/sequoia-corpus-v4.0/emea-fr-test.xml");
	public final static File f5 = new File("src/main/resources/sequoia-corpus-v4.0/frwiki_50.1000.xml");

	public final static String DTD = "src/main/resources/dtd.dtd";
	
	public static final List<String> simpleWords =  Arrays.asList("","les","des","une","pour","est","qui","par","dans","que","qui","sur","avec","son");
	
	public static boolean isBlackListed(String s){
		return simpleWords.contains(s.toLowerCase().trim());
	}
	
	public static String printAll(Map<String,Map<String, Long>> all){
		return all
				.entrySet()
				.stream()
				.map(ent -> "<document name=\"" + ent.getKey() + "\">\n    " 
							+ ent.getValue()
								.entrySet()
								.stream()
								.sorted((ent1,ent2)-> ent2.getValue().compareTo(ent1.getValue()))
								.filter(e -> e.getValue() > 3L)
								.map(m -> "<wrd cnt=\""+m.getValue()+"\" lemme=\""+m.getKey() +"\"/>")
								.collect(Collectors.joining("\n    "))
							+ "\n  </document>"
				)
				.collect(Collectors.joining("\n  "));
	}
	
	public static void main(String... args) throws SAXException, IOException, ParserConfigurationException{
		// DTD Checking 
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setValidating(true);
		
		domFactory.newDocumentBuilder().parse("JDOMOutput.xml");
		domFactory.newDocumentBuilder().parse("DOMOutput.xml");
		domFactory.newDocumentBuilder().parse("SAXOutput.xml");
		//domFactory.newDocumentBuilder().parse("XSLTOutput.xml");
	}
}
