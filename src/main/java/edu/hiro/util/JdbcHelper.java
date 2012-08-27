package edu.hiro.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.google.common.collect.Lists;

public class JdbcHelper
{
	public static void execute(DataSource dataSource, String sql)
	{
		SimpleJdbcTemplate jdbcTemplate=new SimpleJdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}
	
	public static DataFrame executeQuery(DataSource dataSource, String sql)
	{
		SimpleJdbcTemplate jdbcTemplate=new SimpleJdbcTemplate(dataSource);
		DataFrameRowMapper mapper=new DataFrameRowMapper();
		jdbcTemplate.query(sql,mapper);
		return mapper.getDataFrame();
	}
	
	//http://stackoverflow.com/questions/6514876/converting-resultset-to-json-faster-or-better-way
	public static Object getValue(ResultSet rs, String column, int type)
	{
		try
		{
			if (type==java.sql.Types.ARRAY)
				return rs.getArray(column);			
			else if (type==java.sql.Types.BIGINT)
				return rs.getInt(column);			
			else if (type==java.sql.Types.BOOLEAN)
				return rs.getBoolean(column);			
			else if (type==java.sql.Types.BLOB)
				return rs.getBlob(column);			
			else if (type==java.sql.Types.DOUBLE)
				return rs.getDouble(column);			
			else if (type==java.sql.Types.FLOAT)
				return rs.getFloat(column);			
			else if (type==java.sql.Types.INTEGER)
				return rs.getInt(column);			
			else if (type==java.sql.Types.NVARCHAR)
				return rs.getNString(column);			
			else if (type==java.sql.Types.VARCHAR)
				return rs.getString(column);			
			else if (type==java.sql.Types.TINYINT)
				return rs.getInt(column);			
			else if (type==java.sql.Types.SMALLINT)
				return rs.getInt(column);
			else if (type==java.sql.Types.DATE)
				return rs.getDate(column);			
			else if (type==java.sql.Types.TIMESTAMP)
			   return rs.getTimestamp(column);
			else
			{
				//System.err.println("no handler for SQL type: "+type);
				return rs.getObject(column);
			}
			//else throw new CException("no handler for SQL type: "+type);
		}
		catch (SQLException e)
		{
			throw new CException("column="+column+", type="+type,e);
		}
	}
	
	public static abstract class MetadataRowMapper<T> implements RowMapper<T>
	{
		protected List<String> columns;
		protected List<Integer> types;
		
		public T mapRow(ResultSet rs, int rowNum) throws SQLException
		{
			getColumnNames(rs);
			T obj=createEntity(rs,rowNum);
			mapRow(rs, rowNum, obj);
			return obj;
		}
		
		protected abstract void mapRow(ResultSet rs, int rowNum, T obj);
		
		protected abstract T createEntity(ResultSet rs, int rowNum);
		
		public List<String> getColumns()
		{
			return columns;
		}
		
		protected void getColumnNames(ResultSet rs) throws SQLException
		{
			if (columns==null)
			{
				columns=Lists.newArrayList();
				types=Lists.newArrayList();				
				ResultSetMetaData metadata=rs.getMetaData();
				for (int column=1;column<=metadata.getColumnCount();column++)
				{
					columns.add(metadata.getColumnName(column));
					types.add(metadata.getColumnType(column));
				}
				//System.out.println("columns="+StringHelper.join(columns));
				//System.out.println("types="+StringHelper.join(types));
			}
		}
	}
	
	public static class DataFrameRowMapper extends MetadataRowMapper<Object>
	{
		protected DataFrame dataFrame=new DataFrame(true);

		@Override
		protected void mapRow(ResultSet rs, int rowNum, Object obj)
		{
			for (int index=0;index<columns.size();index++)
			{
				String column=columns.get(index);
				int type=types.get(index);
				Object value=getValue(rs, column, type);
				//System.out.println("trying to set column "+column+"="+value);
				dataFrame.setValue(column, rowNum, value);
			}
		}
		
		@Override
		protected Object createEntity(ResultSet rs, int rowNum)
		{
			return rowNum;
		}
		
		public DataFrame getDataFrame()
		{
			return dataFrame;
		}
	}
	
	public static abstract class BeanRowMapper<T> extends MetadataRowMapper<T>
	{
		protected BeanHelper beanhelper=new BeanHelper();

		@Override
		protected void mapRow(ResultSet rs, int rowNum, T obj)
		{
			for (int index=0;index<columns.size();index++)
			{
				String column=columns.get(index);
				int type=types.get(index);
				Object value=getValue(rs, column, type);
				setProperty(obj,column,value);
			}
		}

		protected void setProperty(T obj, String property, Object value)
		{
			//System.out.println("trying to set property "+property+"="+value);
			beanhelper.setProperty(obj, property, value);
		}
	}

	
}
