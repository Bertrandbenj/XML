package project;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.SAXException;

public class DOM {

	static Map<String,Map<String, Long>> all = new HashMap<String,Map<String, Long>>();
	
	public static void main(String[] args) {
		Long time = System.currentTimeMillis();
		try {
			
			for(File f : Arrays.asList(Common.f1,Common.f2,Common.f3,Common.f4,Common.f5)){
				parse(f);
			}
			
			Document output = newDoc();
			mapToDoc(all, output);
			
			writeFile(output, new File("DOMOutput.xml"));

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Long delta = System.currentTimeMillis() -time;
		System.out.println("Delta t (ms) : " + delta );
	}
	
	public static Document newDoc(){
		try {
			Document output = DocumentBuilderFactory
					.newInstance()
					.newDocumentBuilder()
					.getDOMImplementation()
					.createDocument(null, "Root", null);
			output.setXmlStandalone(true);
			return output;
		} catch (DOMException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void writeFile(Document output, File outFile) {
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, Common.DTD);
			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			t.transform(new DOMSource(output), new StreamResult(outFile));
		} catch (TransformerConfigurationException | TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
	
	static void parse(File f) throws SAXException, IOException, ParserConfigurationException{
		Document input = DocumentBuilderFactory
				.newInstance()
				.newDocumentBuilder()
				.parse(f);
	
		all.put(f.getName(),  new HashMap<String,Long>());

		DocumentTraversal td = (DocumentTraversal) input;
		System.out.println("Document build, about to process "+f);

		TreeWalker tw = td.createTreeWalker(input, NodeFilter.SHOW_ELEMENT,
				n -> n.getNodeName().equals("w") && n.getAttributes() != null && n.getAttributes().getNamedItem("lemma") != null ? 
						NodeFilter.FILTER_ACCEPT
				   : n.hasChildNodes() ? 
						NodeFilter.FILTER_SKIP : 
						NodeFilter.FILTER_REJECT,
				true);

		for (Node n = tw.nextNode(); n != null; n = tw.nextNode()) {
			String lemme = n.getAttributes().getNamedItem("lemma").getNodeValue();

			if(lemme.length()>2 && !Common.isBlackListed(lemme)){
				File ff = new File(n.getOwnerDocument().getBaseURI().substring(5));
				Map<String, Long> map = all.get(ff.getName());
				map.put(lemme, map.getOrDefault(lemme,0L)+1);
			}
		}

	}
	
	public static void mapToDoc(Map<String,Map<String, Long>> all , Document out ){
		all.forEach((doc,map) -> {
			Element d = out.createElement("document");
			d.setAttribute("name", doc);
			map.entrySet()
				.stream()
				.sorted((ent1,ent2)-> ent2.getValue().compareTo(ent1.getValue()))
				.filter(ent -> ent.getValue() > 3L)
				.forEach(ent -> {
					Element w = out.createElement("wrd");
					w.setAttribute("lemme", ent.getKey());
					w.setAttribute("cnt", ent.getValue()+"");
					d.appendChild(w);
				});
			out.getDocumentElement().appendChild(d);
		});
	}
	

}
