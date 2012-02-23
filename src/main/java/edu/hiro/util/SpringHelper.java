package edu.hiro.util;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

public final class SpringHelper
{	
	private SpringHelper(){}
	
	public static void loadXmlBeanDefinitions(BeanDefinitionRegistry context, String... paths)
	{
		XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(context);
		for (String path : paths)
		{
			System.out.println("loading classpath XML bean definition: "+path);
			xmlReader.loadBeanDefinitions(new ClassPathResource(path));
		}
	}
	
	public static void loadPropertiesBeanDefinitions(BeanDefinitionRegistry context, String... paths)
	{
		PropertiesBeanDefinitionReader propReader = new PropertiesBeanDefinitionReader(context);
		try
		{
			for (String path : paths)
			{
				System.out.println("loading property bean definition: "+path);
				propReader.loadBeanDefinitions(new UrlResource("file:///"+path));
			}
		}
		catch(MalformedURLException e)
		{
			throw new CException(e);
		}
	}
	
	public static void loadPropertiesBeanDefinitions(BeanDefinitionRegistry context, Properties properties)
	{
		PropertiesBeanDefinitionReader rdr = new PropertiesBeanDefinitionReader(context);
		rdr.registerBeanDefinitions(properties);
	}
	
	public static void registerDataSource(GenericApplicationContext context, String name, DatabaseHelper.Params params)
	{
		registerDataSource(context,name,params.getDriver(),params.getUrl(),
			params.getUsername(),params.getPassword());
	}
	
	public static void registerDataSource(GenericApplicationContext context, String name, DatabaseHelper.Params params, String dbname)
	{
		registerDataSource(context,name,params.getDriver(),params.getUrl(dbname),
			params.getUsername(),params.getPassword());
	}
	
	public static void registerDataSource(GenericApplicationContext context, String name,
			String driver, String url,	String username, String password)
	{
		registerBean(context,name,SingleConnectionDataSource.class,
				"driverClassName",driver, "url",url,
				"username",username, "password",password,
				"suppressClose",true);//, "destroy-method","destroy");
	}

	public static void registerBean(GenericApplicationContext context, String name, Class<?> cls, Map<String,Object> properties)
	{
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(cls);
		for (String property : properties.keySet())
		{
			Object value=properties.get(property);
			//System.out.println("adding property: "+property+"="+value);
			builder.addPropertyValue(property,value);
		}
		context.registerBeanDefinition(name, builder.getBeanDefinition());
	}
	
	public static void registerBean(GenericApplicationContext context, String name, Class<?> cls, Object... args)
	{
		Map<String,Object> properties=StringHelper.createMap(args);
		registerBean(context,name,cls,properties);
	}
	
	public static void registerValue(GenericApplicationContext context, String name, Object value)
	{
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(String.class);
		builder.addConstructorArgValue(value);
		context.registerBeanDefinition(name, builder.getBeanDefinition());		
	}
	
	public static void registerPropertyPlaceholderConfigurer(GenericApplicationContext context, String... locations)
	{
		//Properties properties=FileHelper.getProperties(filename);
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(PropertyPlaceholderConfigurer.class);
		builder.addPropertyValue("fileEncoding",FileHelper.ENCODING.toString());
		builder.addPropertyValue("locations",locations);
		//builder.addPropertyValue("ignoreUnresolvablePlaceholders",true);
		context.registerBeanDefinition("propertyPlaceholderConfigurer",builder.getBeanDefinition());
	}
	
	public static String checkResolvedProperty(String property, String value)
	{
		if (!StringHelper.hasContent(value))
			throw new CException("property "+property+" is not set: "+value);
		if (value.indexOf("${")!=-1)
			throw new CException("property "+property+" has unresolved placeholder: "+value);
		return value;
	}	
}	