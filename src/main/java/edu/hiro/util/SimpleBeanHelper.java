package edu.hiro.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleBeanHelper
{
	private Map<String,Method> setters=new HashMap<String,Method>();
	private Map<String,Method> getters=new HashMap<String,Method>();
	private String datepattern=DateHelper.YYYYMMDD_PATTERN;
		
	public SimpleBeanHelper(){}
	
	public SimpleBeanHelper(String datepattern)
	{
		this.datepattern=datepattern;
	}
	
	public void setDatePattern(String datepattern)
	{
		this.datepattern=datepattern;
	}
	
	// returns the old value
	public String updateProperty(Object obj, String property, Object value)
	{
		String oldvalue=getProperty(obj,property);
		setProperty(obj,property,value);
		return oldvalue;
	}
	
	public String getProperty(Object obj, String property)
	{
		try
		{
			if (obj==null)
				return null;
			Method getter=findGetter(obj,property);
			if (getter==null)
				return null;
			//System.out.println("trying to invoke setter: "+setter.getName());
			Object value=getter.invoke(obj,new Object[]{});
			if (value==null)
				return null;
			return value.toString();
		}
		catch (Exception e)
		{
			throw new CException(e);
		}
	}
	
	
	public void setProperty(List<? extends Object> items, String property, String value)
	{
		for (Object item : items)
		{
			setProperty(item,property,value);
		}
	}
	
	public boolean setPropertyFromString(Object obj, String property, String value)
	{
		if (obj==null)
			return false;
		// first determine type of property
		Class<?> cls=getType(obj,property);
		if (cls==null)
			return false;
		String classname=cls.getName();
		if ("java.lang.String".equals(classname))
			setProperty(obj,property,value);
		else if ("java.lang.Integer".equals(classname) || "int".equals(classname))
			setProperty(obj,property,MathHelper.parseInt(value));
		else if ("java.lang.Double".equals(classname) || "double".equals(classname))
			setProperty(obj,property,MathHelper.parseDouble(value));
		else if ("java.lang.Float".equals(classname) || "float".equals(classname))
			setProperty(obj,property,MathHelper.parseFloat(value));
		else if ("java.lang.Boolean".equals(classname) || "boolean".equals(classname))
			setProperty(obj,property,MathHelper.parseBoolean(value));
		else if ("java.util.Date".equals(classname))
			setProperty(obj,property,DateHelper.parse(value,datepattern,false));
		else if (cls.isEnum())
			setEnumProperty(obj,property,value,cls);
		else return false;
		return true;
	}
	
	private void setEnumProperty(Object obj, String property, String value, Class<?> cls)
	{		
		for (Object constant : Arrays.asList(cls.getEnumConstants()))
		{
			if (constant.toString().equals(value))
			{
				//System.out.println("found value: constant="+constant+" value="+value);
				setProperty(obj,property,constant);
				return;
			}
		}
		throw new CException("can't find enum property: "+property+"="+value+" for class "+cls.getName());
	}
	
	@SuppressWarnings("unchecked")
	public void setProperty(Object obj, String property, Object value)
	{
		try
		{
			if (obj==null)
				return;
			Class cls=getType(obj,property);
			Method setter=findSetter(obj,property,cls);
			if (setter==null)
				return;	
			//System.out.println("trying to invoke setter: "+setter.getName());
			setter.invoke(obj,new Object[]{value});
		}
		catch (Exception e)
		{
			throw new CException("failed to set property ["+property+"]=["+value+"] on object of type "+obj.getClass().getName(),e);
		}
	}

	@SuppressWarnings("unchecked")
	public static Class getType(Object obj, String property)
	{
		try
		{
			if (obj==null)
				return null;
			String getter=formatGetter(property);
			Method[] methods=obj.getClass().getMethods();
			for (int index=0;index<methods.length;index++)
			{
				Method method=methods[index];
				if (method.getName().equals(getter))
					return method.getReturnType();
			}
			System.err.println("can't find getter method: "+getter+" obj="+obj.getClass().getCanonicalName());
			return null;
			//throw new CException("can't find getter method: "+getter);
		}
		catch (Exception e)
		{
			throw new CException(e);
		}
	}
	
	private static String formatGetter(String property)
	{
		return "get"+property.substring(0,1).toUpperCase()+property.substring(1);
	}
	
	// note: property names are not case sensitive!	
	@SuppressWarnings("unchecked")
	public void copyProperties(Object dest, Object src)
	{
		if (dest==null)
			throw new CException("CBeanHelper.copy(): destination object is null");
		if (src==null)
			throw new CException("CBeanHelper.copy(): source object is null");
		try
		{
			Method[] methods=src.getClass().getMethods();
			for (int index=0;index<methods.length;index++)
			{
				Method method=methods[index];
				
				String name=method.getName();
				Class cls=method.getReturnType();
				if (!name.startsWith("get")) // getters should start with "get"
					continue;
				if (method.getParameterTypes().length!=0) // getters should have no parameters
					continue;
				
				// skip collections for now
				if (cls.isAssignableFrom(Collection.class) ||
						cls.isAssignableFrom(Set.class) ||
						cls.isAssignableFrom(List.class) ||
						cls.isAssignableFrom(Map.class))
					continue;
				
				Method getter=method;
				//System.out.println("--------------------");
				//System.out.println("Method: "+method.getReturnType()+" "+method.getName());
				
				String property=name.substring(3);
				Method setter=findSetter(dest,property,cls);
				if (setter==null)
					continue;
				
				//System.out.println("trying to invoke getter: "+method.getName());
				// does not set null properties
				Object obj=getter.invoke(src,new Object[]{});
				if (obj==null)
					continue;
				
				//System.out.println("return value= "+obj.toString());
				//System.out.println("trying to invoke setter: "+setter.getName());
				setter.invoke(dest,new Object[]{obj});
			}
		}
		catch (Exception e)
		{
			throw new CException("problem copying properties",e);// src="+CStringHelper.toString(src)+", dest="+CStringHelper.toString(dest),e);
		}
	}
	
	private String createKey(Object obj, String property)
	{
		return obj.getClass().getCanonicalName()+":"+property;
	}
	
	@SuppressWarnings("unchecked")
	private Method findSetter(Object obj, String property, Class type)
		//throws Exception
	{
		String methodname="set"+property;
		//String key=obj.getClass().getCanonicalName()+":"+methodname;
		String key=createKey(obj,property);
		if (this.setters.containsKey(key))
		{
			//System.out.println("found method in cache, key="+key);
			return this.setters.get(key);
		}
		//System.out.println("method not found in cache, key="+key);
		for (Method method : obj.getClass().getMethods())
		{
			if (!method.getName().equalsIgnoreCase(methodname))
				continue;
			//System.out.println("found a matching method name: "+methodname);
			Class[] params=method.getParameterTypes();
			if (params.length!=1)
				continue;
			if (params[0].getName().equals(type.getName()))
			{
				this.setters.put(key,method);
				//System.out.println("adding method to cache, key="+key+", method="+method.toGenericString());
				return method;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private Method findGetter(Object obj, String property)
		//throws Exception
	{
		String methodname="get"+property;
		String key=createKey(obj,property);
		if (this.getters.containsKey(key))
		{
			//System.out.println("found method in cache, key="+key);
			return this.getters.get(key);
		}
		//System.out.println("method not found in cache, key="+key);
		for (Method method : obj.getClass().getMethods())
		{
			if (!method.getName().equalsIgnoreCase(methodname))
				continue;
			//System.out.println("found a matching method name: "+methodname);
			Class[] params=method.getParameterTypes();
			if (params.length==0)
			{
				this.getters.put(key,method);
				//System.out.println("adding method to cache, key="+key+", method="+method.toGenericString());
				return method;
			}
		}
		return null;
	}

}