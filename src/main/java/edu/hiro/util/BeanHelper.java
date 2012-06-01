package edu.hiro.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.propertyeditors.CustomDateEditor;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;

public class BeanHelper
{
	//protected String logfile="c:/temp/beanutilerrors.txt";
	//protected boolean logerrors=true;
	
	public void copyProperties(Object target, Object src)
	{
		if (target==null)
			throw new CException("BeanHelper.copy(): target object is null");
		if (src==null)
			throw new CException("BeanHelper.copy(): source object is null");
		try
		{
			BeanUtils.copyProperties(src, target);
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			System.err.println("target="+target.getClass().getCanonicalName()+", source="+src.getClass().getCanonicalName());
			//FileHelper.appendFile(logfile,e.getMessage());
		}
	}
	
	public void copyProperties(Object target, Object src, Collection<String> ignore)
	{
		String[] ignoreProperties=new String[ignore.size()];
		ignore.toArray(ignoreProperties);
		copyProperties(target,src,ignoreProperties);
	}
	
	public void copyProperties(Object target, Object src, String...ignore)
	{
		if (target==null)
			throw new CException("BeanHelper.copy(): target object is null");
		if (src==null)
			throw new CException("BeanHelper.copy(): source object is null");
		try
		{
			BeanUtils.copyProperties(src, target, ignore);
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			System.err.println("target="+target.getClass().getCanonicalName()+", source="+src.getClass().getCanonicalName());
		}
	}
	
	
	public Object getProperty(Object target, String property)
	{
		try
		{
			BeanWrapper wrapper=PropertyAccessorFactory.forBeanPropertyAccess(target);
			return wrapper.getPropertyValue(property);
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			//FileHelper.appendFile(logfile,e.getMessage());
			return null;
		}
	}
	
	public boolean setProperty(Object target, String property, Object value)
	{
		try
		{
			BeanWrapper wrapper=PropertyAccessorFactory.forBeanPropertyAccess(target);
			wrapper.setPropertyValue(property,value);
			return true;
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			//FileHelper.appendFile(logfile,e.getMessage());
			return false;
		}
	}
	
	public boolean setPropertyFromString(Object target, String property, String value)
	{
		try
		{
			value=StringHelper.trim(value);
			BeanWrapper wrapper=PropertyAccessorFactory.forBeanPropertyAccess(target);
			wrapper.setPropertyValue(property,value);
			return true;
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			//FileHelper.appendFile(logfile,e.getMessage());
			return false;
		}
	}
	
	public boolean setPropertyFromString(Object target, String property, String value, String datepattern)
	{
		try
		{
			value=StringHelper.trim(value);
			BeanWrapper wrapper=PropertyAccessorFactory.forBeanPropertyAccess(target);
			wrapper.registerCustomEditor(Date.class,new CustomDateEditor(new SimpleDateFormat(datepattern),true));
			wrapper.setPropertyValue(property,value);
			return true;
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			//FileHelper.appendFile(logfile,e.getMessage());
			return false;
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	// for direct field access of private or public fields (case-insensitive, cached, and class-specific)
	//////////////////////////////////////////////////////////////////////////////////////
	
	private Map<String,Field> fieldmap=Maps.newHashMap();

	public void setFields(Object obj, Map<String,String> props)
	{
		for (String field : props.keySet())
		{
			setField(obj,field,props.get(field));
		}
	}
	
	public void setField(Object obj, String name, String value)
	{
		try
		{
			value=StringHelper.normalize(value);
			if (!StringHelper.hasContent(value))
				return;
			Field field = getFieldAccessor(obj,name);
			if (field==null)
				return;
			field.set(obj,value);
		}
		catch (Exception e)
		{
			StringHelper.println("Can't set field: "+name+": "+e);
		}
	}
	
	public void setField(Object obj, String name, Number value)
	{
		try
		{
			if (value==null)
				return;
			Field field = getFieldAccessor(obj,name);
			if (field==null)
				return;
			field.set(obj,value);
		}
		catch (Exception e)
		{
			StringHelper.println("Can't set field: "+name+": "+e);
		}
	}

	public void setField(Object obj, String name, Date value)
	{
		try
		{
			if (value==null)
				return;
			Field field = getFieldAccessor(obj,name);
			if (field==null)
				return;
			field.set(obj,value);
		}
		catch (Exception e)
		{
			StringHelper.println("Can't set field: "+name+": "+e);
		}
	}
	
	public Object getField(Object obj, String name)
	{
		try
		{
			Field field = getFieldAccessor(obj,name);
			if (field==null)
				return null;
			return field.get(obj);
		}
		catch (Exception e)
		{
			StringHelper.println("Can't get field: "+name+": "+e);
			return null;
		}
	}
	
	private Field getFieldAccessor(Object obj, String name) throws SecurityException, NoSuchFieldException
	{
		if (!StringHelper.hasContent(name))
		{
			StringHelper.println("Field name is null or empty: "+name);
			return null;
		}
		String key=getFieldAccessorKey(obj,name);
		if (!fieldmap.containsKey(key))
			cacheFieldAccessors(obj);
		Field field=fieldmap.get(key);
		if (field==null)
		{
			StringHelper.println("Can't find field: "+name);
			return null;
		}
		return field;
	}
		
	private void cacheFieldAccessors(Object obj)
	{
		String classkey=obj.getClass().getCanonicalName().toLowerCase();
		if (fieldmap.containsKey(classkey))
			return;
		StringHelper.println("caching field accessors for class: "+obj.getClass().getName());
		for (Field field : obj.getClass().getDeclaredFields())
		{
			String key=getFieldAccessorKey(obj,field.getName());
			//StringHelper.println("caching field accessor: "+key);
			field.setAccessible(true);
			fieldmap.put(key,field);
		}
		fieldmap.put(classkey,null); // hack!
	}
	
	private String getFieldAccessorKey(Object obj, String field)
	{
		String key=obj.getClass().getCanonicalName()+":"+field;
		key=key.toLowerCase();
		return key;
	}
	

//	public boolean setField(Object target, String property, Object value)
//	{
//		try
//		{
//			ConfigurablePropertyAccessor accessor=PropertyAccessorFactory.forDirectFieldAccess(this);
//			accessor.setPropertyValue(property,value);
//			return true;
//		}
//		catch(Exception e)
//		{
//			throw(new CException(e));
//			//System.err.println(e.getMessage());
//			//return false;
//		}
//	}
//	
//	public Object getField(Object target, String property)
//	{
//		try
//		{
//			ConfigurablePropertyAccessor accessor=PropertyAccessorFactory.forDirectFieldAccess(this);
//			return accessor.getPropertyValue(property);
//		}
//		catch(Exception e)
//		{
//			throw(new CException(e));
//			//System.err.println(e.getMessage());
//			//return null;
//		}
//	}
	
	public static List<String> getProperties(Object obj)
	{
		List<String> properties=new ArrayList<String>();
		try
		{
			BeanWrapper wrapper=PropertyAccessorFactory.forBeanPropertyAccess(obj);
			for (PropertyDescriptor property : wrapper.getPropertyDescriptors())
			{
				properties.add(property.getName());
			}
			return properties;
		}
		catch(Exception e)
		{
			throw new CException(e);
			//FileHelper.appendFile(logfile,e.getMessage());
		}
	}
	
	public static Object forName(String cls)
	{
		try
		{
			return Class.forName(cls);
		}
		catch (ClassNotFoundException e)
		{
			throw new CException(e);
		}
	}
	
	public static Object newInstance(Class<?> cls)
	{
		try
		{
			return cls.newInstance();
		}
		catch(IllegalAccessException e)
		{
			throw new CException(e);
		}
		catch(InstantiationException e)
		{
			throw new CException(e);
		}
	}
}

/*
BeanHelper helper=new BeanHelper();
//BeanHelper helper=new BeanHelper();
PegribaPatient patient=new PegribaPatient();
String field1="IFN既往";
String field2="fsfsdf";
//helper.setField(patient, field1, "test");
helper.setField(patient, field2, "test");
helper.setField(patient, field2, "test");
helper.setField(patient, field1, "test");
System.out.println("patient.ifn既往="+helper.getField(patient,field1));//patient.ifn既往);
*/