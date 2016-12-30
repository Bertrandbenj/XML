package project;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XPathDOM {

	static Map<String,Map<String, Long>> all = new HashMap<String,Map<String, Long>>();
	
	public static void main(String... args) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException{
		Long time = System.currentTimeMillis();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile("//w[@lemma and not(contains('"+Common.simpleWords+"', @lemma)) and string-length(@lemma) > 2]");
		
		for(File f : Arrays.asList(Common.f1,Common.f2,Common.f3,Common.f4,Common.f5)){
			Document doc = builder.parse(f);
			NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			all.put(f.getName(), new HashMap<String,Long>());
			for(int i=nl.getLength()-1; i>0 ; i--){
				Node item = nl.item(i);
				String lemme = item.getAttributes().getNamedItem("lemma").getNodeValue();		
				File ff = new File(item.getOwnerDocument().getBaseURI().substring(5));
				Map<String, Long> map = all.get(ff.getName());
				map.put(lemme, map.getOrDefault(lemme,0L)+1);
			}
		}
		
		Document output = DOM.newDoc();
			
		DOM.mapToDoc(all, output);
		DOM.writeFile(output, new File("XPathDOM.xml"));
		

		Long delta = System.currentTimeMillis() -time;
		System.out.println("Delta t (ms) : " + delta );
	}
}
