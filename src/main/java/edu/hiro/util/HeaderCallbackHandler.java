package edu.hiro.util;

import java.util.Map;

import org.springframework.batch.item.file.LineCallbackHandler;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;

import edu.hiro.util.StringHelper;

public class HeaderCallbackHandler implements LineCallbackHandler {

	protected DelimitedLineTokenizer tokenizer=new DelimitedLineTokenizer();
	protected char delimiter='\t';
	protected Map<String,String> conversions=Maps.newHashMap();
	
	public void setLineTokenizer(DelimitedLineTokenizer tokenizer)
	{
		this.tokenizer=tokenizer;
	}
	
	public void setDelimiter(char delimiter)
	{
		this.delimiter=delimiter;
	}
	
	public void setConversions(Map<String,String> conversions)
	{
		//StringHelper.println("setting conversions: "+conversions,Charsets.UTF_16);
		this.conversions=conversions;
	}
	
	@Override
	public void handleLine(String line)
	{
		StringHelper.println("Header: "+line);
		String[] fields=StringHelper.splitAsArray(line,""+delimiter); //"\t"
		fields=adjustFieldNames(fields);
		System.out.println("fields.length="+fields.length);
		tokenizer.setNames(fields);
		tokenizer.setDelimiter(delimiter);
	}
	
	protected String[] adjustFieldNames(String[] fields)
	{
		for (int index=0;index<fields.length;index++)
		{
			fields[index]=adjustFieldName(fields[index]);
		}
		return fields;
	}
	
	protected String adjustFieldName(String field)
	{
		return field;
	}
}
