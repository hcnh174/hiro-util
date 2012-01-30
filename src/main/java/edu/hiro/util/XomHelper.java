package edu.hiro.util;

import java.io.IOException;
import java.io.StringReader;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;

public final class XomHelper
{	
	private static final String ELEMENT_IS_NULL="element is null";
	
	private XomHelper(){}
	
	/*
	public static Document parse(String xml)
	{
		try
		{
			  Builder parser = new Builder();
			  Document doc = parser.build(new StringReader(xml));
		  return doc;
		}
		catch (ParsingException e) {
			 throw new CException("malformed",e);
		}
		catch (IOException e) {
			 throw new CException("IO exception",e);
		}
	}
	*/
}