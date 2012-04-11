package edu.hiro.util;

import java.util.Collection;
import java.util.Date;

public enum DataType
{
	STRING(false,"string"),
	INTEGER(true,"int"),
	FLOAT(true,"float"),
	BOOLEAN(false,"boolean"),
	DATE(false,"date");
	
	private boolean numeric;
	private String json;
	
	DataType(boolean numeric, String json)
	{
		this.numeric=numeric;
		this.json=json;
	}
	
	public boolean isNumeric(){return this.numeric;}
	public String getJson(){return this.json;}
	
	
	public Object convert(String value)
	{
		switch(this)
		{
		case STRING:
			return value;
		case INTEGER:
			return Integer.valueOf(value);
		case FLOAT:
			return Float.valueOf(value);
		case BOOLEAN:
			return Boolean.valueOf(value);
		case DATE:
			return DateHelper.parse(value, DateHelper.POSTGRES_YYYYMMDD_PATTERN);				
		}
		throw new CException("no handler for DataType "+name());
	}
	
	public static DataType guessDataType(Collection<Object> values)
	{
		boolean is_integer=true;
		boolean is_float=true;
		boolean is_boolean=true;			
		for (Object obj : values)
		{
			String value=obj.toString().toLowerCase().trim();
			if (StringHelper.isEmpty(value))
				continue;
			if (!MathHelper.isFloat(value))
				is_float=false;
			if (!MathHelper.isInteger(value))
				is_integer=false;
			if (!"true".equals(value) && !"false".equals(value))
				is_boolean=false;
		}
		if (is_boolean)
			return DataType.BOOLEAN;
		if (is_integer)
			return DataType.INTEGER;
		if (is_float)
			return DataType.FLOAT;			
		return DataType.STRING;
	}
	
	public static DataType guessDataType(Object obj)
	{
		DataType type=guessDataTypeByClass(obj);
		if (obj==null || type!=DataType.STRING)
			return type;
		boolean is_integer=true;
		boolean is_float=true;
		boolean is_boolean=true;
		String value=obj.toString().toLowerCase().trim();
		if (StringHelper.isEmpty(value))
			return DataType.STRING;
		if (!MathHelper.isFloat(value))
			is_float=false;
		if (!MathHelper.isInteger(value))
			is_integer=false;
		if (!"true".equals(value) && !"false".equals(value))
			is_boolean=false;
		// determine data type
		if (is_boolean)
			return DataType.BOOLEAN;
		if (is_integer)
			return DataType.INTEGER;
		if (is_float)
			return DataType.FLOAT;			
		return DataType.STRING;
	}
	
	private static DataType guessDataTypeByClass(Object obj)
	{
		if (obj instanceof String)
			return DataType.STRING;
		else if (obj instanceof Integer)
			return DataType.INTEGER;
		else if (obj instanceof Float || obj instanceof Double)
			return DataType.FLOAT;
		else if (obj instanceof Boolean)
			return DataType.BOOLEAN;
		else if (obj instanceof Date)
			return DataType.DATE;
		else if (obj instanceof Byte)
			return DataType.STRING;
		if (obj!=null)
			throw new CException("no guessDataType handler for class "+obj.getClass().getName()+": ["+obj.toString()+"]");
			//System.err.println("no guessDataType handler for class "+obj.getClass().getName());
		return DataType.STRING;
	}
}
