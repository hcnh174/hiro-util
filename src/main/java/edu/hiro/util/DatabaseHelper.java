package edu.hiro.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

//import org.hibernate.Query;
//import org.hibernate.SessionFactory;
//import org.hibernate.connection.ConnectionProvider;
//import org.hibernate.engine.SessionFactoryImplementor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate3.LocalDataSourceConnectionProvider;

/*
http://docs.jboss.org/hibernate/stable/core/reference/en/html/session-configuration.html#configuration-logging
org.hibernate.SQL
org.hibernate.engine.query.HQLQueryPlan
org.hibernate.engine.QueryParameters
 .setResultTransformer( Transformers.ALIAS_TO_MAP )
*/
public final class DatabaseHelper
{
	private DatabaseHelper(){}
	
	public static String concatenateScripts(String folder)
	{
		String filelist=folder+"/files.txt";
		FileHelper.checkExists(filelist);
		
		StringBuilder buffer=new StringBuilder();
		String str=FileHelper.readFile(filelist);
		for (String filename : StringHelper.splitLines(str))
		{
			if (filename.indexOf('#')==0)
				continue;
			filename=folder+"/"+filename;
			String sql=FileHelper.readFile(filename);
			buffer.append(sql);
			buffer.append("\n");
		}
		return buffer.toString();
	}
	
	public static void createSetupFile(String folder, boolean overwrite)
	{
		String setupfile=folder+"/setup.sql";
		if (FileHelper.exists(setupfile) && !overwrite)
			return;
		System.out.println("creating database setup file: "+setupfile);
		String str=concatenateScripts(folder);		
		FileHelper.writeFile(setupfile,str);
	}
	
	/*
	public static void setParams(Query query, Map<String,Object> params)
	{
		for (String name : params.keySet())
		{
			Object value=params.get(name);
			//System.out.println("setting parameter: "+name+"="+value);
			query.setParameter(name,value);
		}
	}
	*/
	
	/*
	public static String getDetails(InvalidStateException e)
	{		
		StringBuilder buffer=new StringBuilder();
		for (InvalidValue invalid : e.getInvalidValues())
		{		
			buffer.append("Validation error on:" + invalid.getPropertyPath());
			buffer.append(": ");
			buffer.append(invalid.getPropertyName());
			buffer.append(": ");
			buffer.append(invalid.getMessage());
			buffer.append(" [");
			buffer.append(invalid.getValue());
			buffer.append("] ");
			buffer.append(StringHelper.toString(invalid.getBean()));
			buffer.append("\n");
		}
		return buffer.toString();
	}
	*/
	
	// some or all may be empty
	// remove empty ones, surround the rest with parentheses, and join with " AND "
	public static String joinSubqueries(String...subqueries)
	{
		List<String> items=StringHelper.clean(subqueries);
		items=StringHelper.wrap(items,"(",")");
		return StringHelper.join(items," AND ");
	}
	
	public static DataSource createDataSource(Params params, String database)
	{
		String url="jdbc:postgresql://"+params.host+":"+params.port+"/"+database;
		System.out.println("url="+url);
		DataSource datasource=new DriverManagerDataSource(url,params.username,params.password);
		return datasource;
	}
	
	public static void createDatabase(Params params)
	{
		DataSource datasource=createDataSource(params,params.basedb);
		JdbcTemplate jdbc=new JdbcTemplate(datasource);
		String sql="CREATE DATABASE "+params.name+"\n";
		sql+="WITH OWNER="+params.username+"\n";
		sql+="ENCODING = 'UTF8'\n";
		sql+="TEMPLATE = "+params.template+"\n";
		sql+="CONNECTION LIMIT = -1;";
		System.out.println("sql="+sql);
		jdbc.execute(sql);
	}
	
	public static void dropDatabase(Params params)
	{
		DataSource datasource=createDataSource(params,params.basedb);
		JdbcTemplate jdbc=new JdbcTemplate(datasource);
		String sql="DROP DATABASE "+params.name+";\n";
		System.out.println("sql="+sql);
		jdbc.execute(sql);
	}
	
	public static List<String> listDatabases(Params params)
	{
		DataSource datasource=createDataSource(params,params.basedb);
		JdbcTemplate jdbc=new JdbcTemplate(datasource);
		
		RowMapper<String> mapper = new RowMapper<String>()
		{
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException
	        {
	            return rs.getString("datname");
	        }
	    };
	    List <String> databases=jdbc.query("select datname from pg_catalog.pg_database",mapper);
		System.out.println("databases: "+StringHelper.join(databases,", "));
		return databases;
	}
	
	public static boolean databaseExists(Params params)
	{
		List <String> databases=listDatabases(params);
		for (String database : databases)
		{
			if (database.equals(params.name))
				return true;
		}
		return false;
	}
	
	public static void vacuum(Params params)
	{
		DataSource datasource=createDataSource(params,params.name);
		JdbcTemplate jdbc=new JdbcTemplate(datasource);
		String sql="VACUUM ANALYZE;\n";
		System.out.println("sql="+sql);
		jdbc.execute(sql);
	}
	
	public static void executeSql(Params params, String sql)
	{
		JdbcTemplate jdbc=new JdbcTemplate(createDataSource(params,params.name));
		jdbc.execute(sql);
	}
	
	public static void writeDataSourceFile(Params params, String deployDir)
	{
		String xml=createDataSourceFile(params);
		String filename=deployDir+params.jndi+"-ds.xml";
		if (FileHelper.exists(filename))
			FileHelper.copyFile(filename, filename+".bak");
		FileHelper.writeFile(filename,xml);
		System.out.println("writing DataSource XML file to "+filename);
	}
	
	public static String createDataSourceFile(Params params)
	{
		StringBuilder buffer=new StringBuilder();
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
		buffer.append("<datasources>\n");
		buffer.append("\t<local-tx-datasource>\n");
		buffer.append("\t\t<jndi-name>"+params.jndi+"DS</jndi-name>\n");
		buffer.append("\t\t<connection-url>"+params.getUrl()+"</connection-url>\n");
		buffer.append("\t\t<driver-class>"+params.driver+"</driver-class>\n");
		buffer.append("\t\t<user-name>"+params.username+"</user-name>\n");
		buffer.append("\t\t<password>"+params.password+"</password>\n");
		buffer.append("\t</local-tx-datasource>\n");
		buffer.append("</datasources>\n");		
		return buffer.toString();
	}
	
	/*
	public static DataSource getDataSource(SessionFactory sessionFactory)
	{
		if (sessionFactory instanceof SessionFactoryImplementor)
		{
			ConnectionProvider cp = ((SessionFactoryImplementor) sessionFactory).getConnectionProvider();
			if (cp instanceof LocalDataSourceConnectionProvider)
			{
				return ((LocalDataSourceConnectionProvider) cp).getDataSource();
			}
		}
		return null;
	}
	*/

	public static class Params
	{
		protected String driver="org.postgresql.Driver";
		protected String name;
		protected String jndi;
		protected String template="template0";
		protected String encoding="UTF8";
		protected String host="localhost";
		protected String port="5432";
		protected String username;
		protected String password;
		protected String basedb="";
		
		public String getDriver(){return this.driver;}
		public void setDriver(final String driver){this.driver=driver;}

		public String getName(){return this.name;}
		public void setName(final String name){this.name=name;}

		public String getJndi(){return this.jndi;}
		public void setJndi(final String jndi){this.jndi=jndi;}

		public String getTemplate(){return this.template;}
		public void setTemplate(final String template){this.template=template;}

		public String getEncoding(){return this.encoding;}
		public void setEncoding(final String encoding){this.encoding=encoding;}

		public String getHost(){return this.host;}
		public void setHost(final String host){this.host=host;}

		public String getPort(){return this.port;}
		public void setPort(final String port){this.port=port;}

		public String getUsername(){return this.username;}
		public void setUsername(final String username){this.username=username;}

		public String getPassword(){return this.password;}
		public void setPassword(final String password){this.password=password;}
		
		public String getBasedb(){return this.basedb;}
		public void setBasedb(final String basedb){this.basedb=basedb;}
		
		public Params(){}
		
		public Params(String name, String username, String password, String host, String port)
		{
			this.name=name;
			this.username=username;
			this.password=password;
			this.host=host;
			this.port=port;
		}
	
		public void validate()
		{
			driver=SpringHelper.checkResolvedProperty("driver",driver);
			name=SpringHelper.checkResolvedProperty("name",name);
			jndi=SpringHelper.checkResolvedProperty("jndi",jndi);
			template=SpringHelper.checkResolvedProperty("template",template);
			encoding=SpringHelper.checkResolvedProperty("encoding",encoding);
			host=SpringHelper.checkResolvedProperty("host",host);
			port=SpringHelper.checkResolvedProperty("port",port);
			username=SpringHelper.checkResolvedProperty("username",username);
			password=SpringHelper.checkResolvedProperty("password",password);
		}

		public String getUrl()
		{
			return "jdbc:postgresql://"+host+":"+port+"/"+name;
		}
		
		public String getUrl(String name)
		{
			return "jdbc:postgresql://"+host+":"+port+"/"+name;
		}
		
		public Properties getProperties()
		{
			Properties properties=new Properties();
			properties.setProperty("driver",getDriver());
			properties.setProperty("name",getName());
			properties.setProperty("template",getTemplate());
			properties.setProperty("host",getHost());
			properties.setProperty("port",getPort());
			properties.setProperty("username",getUsername());
			properties.setProperty("password",getPassword());
			return properties;
		}
		
		@Override
		public String toString()
		{
			return StringHelper.toString(this);
		}
	}
}
