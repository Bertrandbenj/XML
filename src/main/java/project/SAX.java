package project;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAX extends DefaultHandler {
	
	static Map<String,Map<String, Long>> all = new HashMap<String,Map<String, Long>>();
	static PrintStream ps;
	
	final File file ;

	public SAX(File file) {
		this.file=file;
		all.put(file.getName(), new HashMap<String,Long>());
	}

	public void startElement(String nameSpace, String localName, String qName, Attributes attr) throws SAXException {

		if (qName.equals("w")) {

			String wrd = attr.getValue("lemma");

			if(wrd == null || wrd.length() < 3 || Common.isBlackListed(wrd))
				return;
			
			Map<String, Long> map = all.get(file.getName());
			map.put(wrd, map.getOrDefault(wrd,0L) + 1);
		}
	}

	public static void main(String[] args) {	
		Long time = System.currentTimeMillis();
		try {
			ps = new PrintStream("SAXOutput.xml");
			ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			ps.println("<!DOCTYPE Root SYSTEM \"src/main/resources/dtd.dtd\">");
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			saxParser.parse(Common.f1, new SAX(Common.f1));
			saxParser.parse(Common.f2, new SAX(Common.f2));
			saxParser.parse(Common.f3, new SAX(Common.f3));
			saxParser.parse(Common.f4, new SAX(Common.f4));
			saxParser.parse(Common.f5, new SAX(Common.f5));
			
			ps.print("<Root>\n  " + Common.printAll(all)+"\n</Root>\n");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		Long delta = System.currentTimeMillis() - time;
		System.out.println("Delta t (ms) : " + delta );
	}
}