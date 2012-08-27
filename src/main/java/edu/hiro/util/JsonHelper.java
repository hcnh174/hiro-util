package edu.hiro.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class JsonHelper
{	
	public static String jackson(HttpServletResponse response, Object... args)
	{
		try
		{
			Object obj=(args.length>1) ? StringHelper.createMap(args) : args[0];
			response.setContentType(WebHelper.ContentType.JSON);
			response.setCharacterEncoding(FileHelper.ENCODING.toString());
			ObjectMapper mapper = getObjectMapper();
			//System.out.println("json="+mapper.writeValueAsString(obj));
			mapper.writeValue(response.getWriter(),obj);
			return null;
		}
		catch (Exception e)
		{
			throw new CException(e);
		}
	}
	
	public static String jackson(Object... args)
	{
		try
		{
			Object obj=(args.length>1) ? StringHelper.createMap(args) : args[0];
			ObjectMapper mapper = getObjectMapper();
			return mapper.writeValueAsString(obj);
		}
		catch (Exception e)
		{
			throw new CException(e);
		}
	}
	
	private static ObjectMapper getObjectMapper()
	{
		ObjectMapper mapper = new ObjectMapper();
		configureMapper(mapper);
		return mapper;
	}
	
	public static void configureMapper(ObjectMapper mapper)
	{
		mapper.setDateFormat(new SimpleDateFormat(DateHelper.YYYYMMDD_PATTERN));
	}
	
	@SuppressWarnings("rawtypes")
	public static Map<String,List<?>> getEnums(Class maincls)
	{
		Map<String,List<?>> map=Maps.newLinkedHashMap();
		for (Class<?> cls : maincls.getDeclaredClasses())
		{
			if (!cls.isEnum())
				continue;
			//System.out.println("declared class: "+cls.getName());
			map.put(cls.getSimpleName(),getEnumValues(cls));
		}
		//System.out.println(StringHelper.toString(map.keySet()));
		return map;
	}
	
	@SuppressWarnings("rawtypes")
	public static List<Map<String,String>> getEnumValues(Class cls)
	{
		if (!cls.isEnum())
			throw new CException("Class is not an enum: "+cls.getSimpleName());
		List<Map<String,String>> list=Lists.newArrayList();
		for (Object constant : Arrays.asList(cls.getEnumConstants()))
		{
			Map<String,String> map=Maps.newLinkedHashMap();
			map.put("value",((Enum)constant).name());
			map.put("display",constant.toString());
			list.add(map);
		}
		return list;
	}
}
