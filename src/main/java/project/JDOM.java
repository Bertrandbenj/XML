package project;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.SAXException;

import project.Common;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.counting;
public class JDOM {
	
	static final Function<File, Document> build = f -> { 
		try {
			return (Document) new SAXBuilder().build(f);
		} catch (Exception e) { 
			e.printStackTrace(); 
			return null;
		}
	};
	
	
	static void makeDoc(Document elem){
		XMLOutputter xmlOutput = new XMLOutputter();
		Format format = Format
				.getPrettyFormat()
				.setEncoding("UTF-8")
				.setOmitDeclaration(false)
				.setOmitEncoding(false);
	
		try {
			xmlOutput.getXMLOutputProcessor().process(new FileWriter("JDOMOutput.xml"), format, elem);
		} catch (IOException e) {
			e.printStackTrace();
		}
	};
	
	public static String cleanStr(String e){
		return e.replaceAll("!", "").replaceAll("'", "").replaceAll("/", "").replaceAll("[\\(\\)\\=]", "");
	}
	
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
		
		Long time = System.currentTimeMillis();
		System.out.println("Loading... file exists? " + Files.exists(Paths.get("src/main/resources/sequoia-corpus-v4.0/annodis.er.xml")));
		
		Element newDoc =  
				Stream.of(Common.f1,Common.f2,Common.f3,Common.f4,Common.f5)
				.peek(x -> System.out.println(x))									// display
				.map(build)  														// build the documents
				.map(doc ->  doc.getRootElement())									// enter the root																
				.flatMap(s -> recursiveSearch(s))									// recurse all the element tree
				.filter(s -> s.getName().equals("w") )								// take words only
				.filter(s -> s.getAttributeValue("lemma") != null)
				.filter(s -> s.getAttributeValue("lemma").length() > 2	 )			// more than two letters
				.filter(s -> !Common.isBlackListed(s.getAttributeValue("lemma") ))	// blacklisted words
				.collect(groupingBy(
							e -> new File(e.getDocument().getBaseURI()).getName(),
							groupingBy(	
								wrd -> wrd.getAttributeValue("lemma"), 
								counting())
						))
				.entrySet()																
				.stream()
				.map(ent -> ent	.getValue()
								.entrySet()
								.stream()
								.sorted((ent1, ent2) -> ent2.getValue().compareTo(ent1.getValue()))
								.filter(e -> e.getValue() > 3)
								.reduce(
										new Element("document").setAttribute("name", ent.getKey()),
										(t,u) -> {
											if(t != null){
												Element wrd = new Element("wrd")
																	.setAttribute("cnt", u.getValue().toString())
																	.setAttribute("lemme", u.getKey());
												
												return  t.addContent(wrd);
											}
											return t;
										}
										,(t,u) -> t.addContent(u)
									))
				.reduce(new Element("Root"),
						(t,u) -> u==null?t:t.addContent(u));
		
		
		
		makeDoc(new Document(newDoc, new DocType("Root", Common.DTD)));
				
		Long delta = System.currentTimeMillis() -time ;
		System.out.println("Delta t (ms) : " + delta );
		
	}
	
	

	/**
	 * Cool thing with stream is that null get filtered so rather than
	 * having a stop condition we return the current node
	 * 
	 * @param e  the root of the tree we are searching 
	 * @return A stream containing all descendant of e 
	 */
	@SuppressWarnings("List element casting")
	static Stream<Element> recursiveSearch(Element e){
		return  StreamSupport.stream(e.getDescendants(new ElementFilter("w")).spliterator(),false);
		
//		return Stream.concat(
//				Stream.of(e), 				
//				((List<Element>)e.getChildren()).stream().flatMap(ee -> recursiveSearch(ee))
//				);	
	}

}
