package edu.hiro.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamResult;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.html.AbstractHtmlExporter;
import org.docx4j.convert.out.html.HtmlExporterNG2;
import org.docx4j.convert.out.pdf.PdfConversion;
import org.docx4j.convert.out.pdf.viaXSLFO.PdfSettings;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.Document;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;

import com.google.common.collect.Maps;

public class Docx4jHelper
{	
	public List<Object> xpath(WordprocessingMLPackage wordMLPackage, String xpath)
	{
		try
		{
			MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
			List<Object> list = documentPart.getJAXBNodesViaXPath(xpath, false);
			return list;
		}
		catch (Exception e)
		{
			throw new CException(e);
		}
	}
	
	public void addShape(WordprocessingMLPackage wordMLPackage, String xml)
	{
		try
		{
			org.docx4j.wml.P para = (P)XmlUtils.unmarshalString(xml);
			MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
			Document wmlDocumentEl=(Document)documentPart.getJaxbElement();
			Body body = wmlDocumentEl.getBody();
			body.getContent().add(para);
		}
		catch (Exception e)
		{
			throw new CException(e);
		}
	}
	
	public void applyTemplate(String infile, String outfile, HashMap<String,String> mappings)
	{
		WordprocessingMLPackage wordMLPackage=load(infile);
		wordMLPackage=applyTemplate(wordMLPackage,mappings);
		save(wordMLPackage,outfile);
	}
	
	public void applyTemplate(String infile, OutputStream os, HashMap<String,String> mappings)
	{
		WordprocessingMLPackage wordMLPackage=load(infile);
		wordMLPackage=applyTemplate(wordMLPackage,mappings);
		save(wordMLPackage,os);
	}
	
	public WordprocessingMLPackage load(String infile)
	{
		try
		{
			System.out.println("opening file: "+infile);
			WordprocessingMLPackage wordMLPackage=WordprocessingMLPackage.load(new File(infile));
			return wordMLPackage;
		}
		catch (Exception e)
		{
			throw new CException(e);
		}
	}
	
	public WordprocessingMLPackage applyTemplate(WordprocessingMLPackage wordMLPackage, HashMap<String,String> mappings)
	{
		try
		{
			replaceText(wordMLPackage,mappings);
			return wordMLPackage;
		}
		catch (Exception e)
		{
			throw new CException(e);
		}
	}
	
	public void save(WordprocessingMLPackage wordMLPackage, String outfile)
	{
		try
		{
			wordMLPackage.save(new File(outfile));
		}
		catch (Exception e)
		{
			throw new CException(e);
		}
	}
	
	public void save(WordprocessingMLPackage wordMLPackage, OutputStream os)
	{
		try
		{
			SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
			saver.save(os);
		}
		catch (Exception e)
		{
			throw new CException(e);
		}
	}
	
	public void html(WordprocessingMLPackage wordMLPackage, String stem)
	{
		try
		{
			AbstractHtmlExporter exporter = new HtmlExporterNG2();
			OutputStream os = new FileOutputStream(stem+".html");
			StreamResult result=new StreamResult(os);
			exporter.html(wordMLPackage, result, stem + "_files");
		}
		catch (Exception e)
		{
			throw new CException(e);
		}
	}
	
	public void pdf(WordprocessingMLPackage wordMLPackage, String outfile)
	{
		try
		{
			wordMLPackage.setFontMapper(new IdentityPlusMapper());
			PdfConversion converter=new org.docx4j.convert.out.pdf.viaXSLFO.Conversion(wordMLPackage);
			OutputStream os = new FileOutputStream(outfile);
			PdfSettings settings=new PdfSettings();
			converter.output(os,settings);
		}
		catch (Exception e)
		{
			throw new CException(e);
		}
	}
	
	public void openDocument(String infile, String outfile)
	{
		try
		{
			System.out.println("****************************************************");
			System.out.println("opening file: "+infile);
			WordprocessingMLPackage wordMLPackage=WordprocessingMLPackage.load(new File(infile));
			MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
			System.out.println("documentPart="+documentPart.toString());
			
			Document wmlDocumentEl=(Document)documentPart.getJaxbElement();
			Body body = wmlDocumentEl.getBody();
//			for (Object part : body.getContent())
//			{
//				System.out.println(part.getClass().getName()+": "+part.toString());
//				if (part instanceof P)
//				{
//					P para=(P)part;
//					for (Object part2 : para.getContent())
//					{
//						System.out.println("  "+part2.getClass().getName()+": "+part2.toString());
//						if (part2 instanceof R)
//						{							
//							R run=(R)part2;
//							for (Object part3 : run.getContent())
//							{
//								System.out.println("    "+part3.getClass().getName()+": "+part3.toString());
//								if (part3 instanceof JAXBElement)
//								{
//									JAXBElement el=(JAXBElement)part3;
//									System.out.println("JAXBElement="+el.getValue());
//									Text text=(Text)el.getValue();
//									System.out.println("text="+text.getValue());
//								}
//								if (part3 instanceof Text)
//								{							
//									Text text=(Text)part3;
//									System.out.println("text="+text.getValue());
//									if (text.getValue().startsWith("G"))
//									{
//										String filename="d:/projects/patientdb.etc/circle.png";
//										byte[] bytes=readImage(filename);
//										newImage(wordMLPackage, run, bytes, 500);
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//			
			HashMap<String,String> mappings=Maps.newHashMap();
			mappings.put("name","A. Patient");
			mappings.put("rs8099917","T/T");
			mappings.put("rs1137354","A/C");
			mappings.put("rs1012068","G/G");
			replaceText(wordMLPackage,mappings);
			
			
			//wordMLPackage.getMainDocumentPart().addObject(p);
			
			//addImage(wordMLPackage,bytes);
			
			//addParagraph(body,"SAMPLE TEXT");
			//addDrawing(body);
			wordMLPackage.save(new File(outfile));
			System.out.println("****************************************************");
		}
		catch (Exception e)
		{
			System.out.println(e.toString());
			e.printStackTrace();
			throw new CException(e);
		}
	}
	
	public byte[] readImage(String filename) throws Exception
	{
		File file = new File(filename);
		InputStream is = new FileInputStream(filename);		
		long length = file.length();   
		// You cannot create an array using a long type. It needs to be an int type.
		if (length > Integer.MAX_VALUE) {
			throw new CException("File too large!!");
		}
		byte[] bytes = new byte[(int)length];
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0)
		{
			offset += numRead;
		}
		// Ensure all the bytes have been read in
		if (offset < bytes.length){
			System.out.println("Could not completely read file "+file.getName());
		}
		is.close();
		return bytes;
	}
	
	public void replaceText(WordprocessingMLPackage wordMLPackage, HashMap<String,String> mappings) throws JAXBException
	{
		String xml=marshaltoString(wordMLPackage);
		unmarshallFromTemplate(wordMLPackage,xml,mappings);
	}
	
	public String marshaltoString(WordprocessingMLPackage wordMLPackage) throws JAXBException
	{
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		Document doc=(Document)documentPart.getJaxbElement();
		String xml = XmlUtils.marshaltoString(doc, true);
		return xml;
	}
	
	public void unmarshallFromTemplate(WordprocessingMLPackage wordMLPackage, String xml, HashMap<String,String> mappings) throws JAXBException
	{
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		Document doc = (Document)XmlUtils.unmarshallFromTemplate(xml, mappings);
		documentPart.setJaxbElement(doc);
	}
//	
//	public void replaceText(WordprocessingMLPackage wordMLPackage, HashMap<String,String> mappings) throws JAXBException
//	{
//		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
//		Document wmlDocumentEl=(Document)documentPart.getJaxbElement();
//		String xml = XmlUtils.marshaltoString(wmlDocumentEl, true);
//		Object obj = XmlUtils.unmarshallFromTemplate(xml, mappings);
//		documentPart.setJaxbElement((Document) obj);
//	}
	
//	public void addImage(WordprocessingMLPackage wordMLPackage, byte[] bytes) throws Exception
//	{
//
//		P p = newImage(wordMLPackage, bytes, filenameHint, altText, id1, id2, 500);
//		// Now add our p to the document
//		wordMLPackage.getMainDocumentPart().addObject(p);
////		P p2 = newImage( wordMLPackage, bytes, filenameHint, altText, id1, id2, 3000);
////		// Now add our p to the document
////		wordMLPackage.getMainDocumentPart().addObject(p2);
////		P p3 = newImage( wordMLPackage, bytes, filenameHint, altText, id1, id2, 6000 );
////		// Now add our p to the document
////		wordMLPackage.getMainDocumentPart().addObject(p3);		
//	}	
	
	public static void newImage( WordprocessingMLPackage wordMLPackage, R run, byte[] bytes, long cx) throws Exception
	{       
		String filenameHint = null; String altText = null; int id1 = 0;	int id2 = 1; boolean link=false;
	    BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordMLPackage, bytes);	           
	    Inline inline = imagePart.createImageInline( filenameHint, altText, id1, id2, cx, link);
	    // Now add the inline in w:p/w:r/w:drawing
	    ObjectFactory factory = new ObjectFactory();
	    //P  p = factory.createP();
	    //R  run = factory.createR();             
	    //p.getContent().add(run);       
	    Drawing drawing = factory.createDrawing();               
	    run.getContent().add(drawing);
	    drawing.getAnchorOrInline().add(inline);	   
	}
	
	public static P newImage(WordprocessingMLPackage wordMLPackage, byte[] bytes,
			String filenameHint, String altText, int id1, int id2, long cx) throws Exception
	{       
	    BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordMLPackage, bytes);	           
	    Inline inline = imagePart.createImageInline( filenameHint, altText, id1, id2, cx);
	    // Now add the inline in w:p/w:r/w:drawing
	    ObjectFactory factory = new ObjectFactory();
	    P  p = factory.createP();
	    R  run = factory.createR();             
	    p.getContent().add(run);       
	    Drawing drawing = factory.createDrawing();               
	    run.getContent().add(drawing);
	    drawing.getAnchorOrInline().add(inline);	   
	    return p;
	}
	
	public void addParagraph(Body body, String simpleText)
	{
		 ObjectFactory factory = Context.getWmlObjectFactory();
		    // Create the paragraph
		    P para = factory.createP();
		    // Create the text element
		   Text t = factory.createText();
		    t.setValue(simpleText); 
		    // Create the run
		    R run = factory.createR();
		    run.getContent().add(t);
		    para.getContent().add(run);
		    // Now add our paragraph to the document body
		    //Body body = this.jaxbElement.getBody();
		    body.getContent().add(para);
	}
	
	public void addDrawing(Body body)
	{
		 ObjectFactory factory = Context.getWmlObjectFactory();
		    // Create the paragraph
		    Drawing drawing = factory.createDrawing();
		    body.getContent().add(drawing);
	}
}