package edu.hiro.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.common.io.LineReader;


//similar to a CTable but more geared to identifying cells using a double hash
// tables are row-based, dataframes are more column-based
public class DataFrame<T extends Object>
{
	protected Map<String,Column> columns=Maps.newLinkedHashMap();
	protected Set<T> rownames=Sets.newLinkedHashSet();
	protected boolean autoAddColumns=false;
	
	public DataFrame(){}
	
	public DataFrame(boolean autoAddColumns)
	{
		this.autoAddColumns=autoAddColumns;
	}
	
	public Column findOrCreateColumn(String colname)
	{
		Column column=getColumn(colname);
		if (column==null)
			column=addColumn(colname);
		return column;
	}
	
	public Column addColumn(String colname)
	{
		//System.out.println("adding column ["+colname+"]");
		if (this.columns.containsKey(colname))
			return this.columns.get(colname);
		Column column=new Column(this,colname);
		this.columns.put(column.getName(),column);
		return column;
	}

	public void addColumns(Collection<String> colnames)
	{
		for (String colname : colnames)
		{
			addColumn(colname);
		}
	}
	
	public Object getValue(String colname, T rowname)
	{
		Column column=this.columns.get(colname);
		if (column==null)
			throw new CException("column is null: "+colname);
		return column.getValue(rowname);
	}
	
	public Object getValue(String colname, T rowname, Object dflt)
	{
		Column column=this.columns.get(colname);
		if (column==null)
			return dflt;
		Object value=column.getValue(rowname);
		if (!StringHelper.hasContent(value))
			return dflt;
		return value;
	}
	
	public void setValue(String colname, T rowname, Object value)
	{
		//System.out.println("dataframe.setValue: colname="+colname+", rowname="+rowname+", value="+value);
		Column column=getColumn(colname);
		column.setValue(rowname,value);
	}
	
	public void setValue(String colname, T rowname, char value)
	{
		Column column=getColumn(colname);
		column.setValue(rowname,value);
	}
	
	public boolean hasValue(String colname, T rowname)
	{
		Object value=getValue(colname,rowname);
		return (value!=null);
	}
	
	public String getStringValue(String colname, T rowname)
	{
		Object value=getValue(colname,rowname);
		if (value==null)
			return null;
		return value.toString();
	}
	
	public Integer getIntValue(String colname, T rowname)
	{
		Object value=getValue(colname,rowname);
		if (value==null)
			return null;
		return Integer.valueOf(value.toString());
	}
	
	public Column getColumn(String colname)
	{
		if (!this.columns.containsKey(colname))
		{
			if (!autoAddColumns)
				throw new CException("can't find column named "+colname);
			else return addColumn(colname);
		}
		return this.columns.get(colname);
	}	
	
	public Collection<Column> getColumns()
	{
		return this.columns.values();
	}
	
	public int getNumRows()
	{
		return this.rownames.size();
	}
	
	public int getNumCols()
	{
		return this.columns.size();
	}
	
	public CTable getTable()
	{
		CTable table=new CTable();
		//table.getHeader().add("rowname");
		for (String colname : getColNames())
		{
			table.getHeader().add(colname);
		}
		for (T rowname : getRowNames())
		{
			CTable.Row row=table.addRow();
			//row.add(rowname);
			for (String colname : getColNames())
			{
				Column column=getColumn(colname);
				Object value=column.getValue(rowname);
				row.add(value);
			}
		}
		return table;
	}

	public List<String> getColNames()
	{
		List<String> colnames=Lists.newArrayList();
		colnames.addAll(this.columns.keySet());
		return colnames;
	}
	
	public boolean hasRow(T rowname)
	{
		return this.rownames.contains(rowname);
		//return this.rownames.containsKey(rowname);
	}
	
	public boolean hasColumn(String colname)
	{
		return this.columns.containsKey(colname);
	}
	
	public void registerRowName(T rowname)
	{
		addRow(rowname);
	}
	
	public void addRow(T rowname)
	{
		if (!hasRow(rowname))
			this.rownames.add(rowname);
	}
	
	public Collection<T> getRowNames()
	{
		return rownames;
	}
	
	public Collection<String> getStringRowNames()
	{
		return Collections2.transform(rownames, new Function<Object, String>()
		{
			@Override
			public String apply(Object obj)
			{
				return obj.toString();
			}
		});
	}
	
	public int size()
	{
		Column column=this.columns.values().iterator().next();
		return column.size();
	}
	
	
	public boolean isEmpty()
	{
		return this.columns.isEmpty();
	}

	public void appendColumns(DataFrame<T> other)
	{
		for (String colname : other.getColNames())
		{
			if (hasColumn(colname))
			{
				System.err.println("dataframe already has column "+colname+". skipping.");
				continue;
			}
			addColumn(colname);
			Column column=other.getColumn(colname);
			for (T rowname : column.getRowNames())
			{
				Object value=column.getValue(rowname);
				if (hasRow(rowname))
					setValue(colname,rowname,value);
			}
		}
	}
	
	public void appendForeignColumns(String fkey_colname, DataFrame<T> other)
	{
		for (String other_colname : other.getColNames())
		{
			String colname=fkey_colname+"_"+other_colname;
			if (hasColumn(colname))
			{
				System.err.println("dataframe already has column "+colname+". skipping.");
				continue;
			}
			addColumn(colname);
			Column column=other.getColumn(other_colname);
			for (T other_rowname : column.getRowNames())
			{
				Object value=column.getValue(other_rowname);
				for (T rowname : this.getRownamesByColValue(fkey_colname, other_rowname))
				{
					//System.out.println("trying to setValue(colname="+colname+", rowname="+rowname+", value="+value);
					setValue(colname,rowname,value);
				}
			}
		}
	}

	public Collection<Object> getUniqueValues(String colname)
	{
		Column column=getColumn(colname);
		return column.getUniqueValues();
	}
	
	public Collection<T> getRownamesByColValue(String colname, Object value)
	{
		Column column=getColumn(colname);
		return column.getRownamesByColValue(value); 
	}
	
	@JsonProperty
	public Integer getTotalCount()
	{
		return rownames.size();
	}
	
	@JsonProperty
	public List<Row> getRows()
	{
		List<Row> rows=Lists.newArrayList();
		for (T rowname : getRowNames())
		{
			Row row=new Row();
			row.put("rowname",rowname);
			for (String colname : getColNames())
			{
				Object value=getValue(colname,rowname);
				row.put(colname,value);
			}
			rows.add(row);
		}
		return rows;
	}
	
	@SuppressWarnings("serial")
	public class Row extends LinkedHashMap<String,Object>
	{
		
	}
	
	public class Column
	{
		protected DataFrame<T> dataframe;
		protected String colname;
		protected Map<T,Object> values=Maps.newLinkedHashMap();
		
		public Column(DataFrame<T> dataframe, String colname)
		{
			this.dataframe=dataframe;
			this.colname=colname;
		}
		
		public String getName(){return this.colname;}
		
		public Set<T> getKeys()
		{
			return this.values.keySet();
		}
		
		public Object getValue(T rowname)
		{
			return this.values.get(rowname);
		}
		
		public void setValue(T rowname, Object value)
		{
			dataframe.registerRowName(rowname);
			this.values.put(rowname,value);
		}
		
		public Collection<T> getRowNames()
		{
			return this.values.keySet();
		}
		
		public Collection<Object> getValues()
		{
			return this.values.values();
		}
		
		public int size()
		{
			return this.values.size();
		}
		
		public Collection<Object> getUniqueValues()
		{
			Set<Object> uniquevalues=Sets.newLinkedHashSet();
			for (Object value : values.values())
			{
				if (!uniquevalues.contains(value))
					uniquevalues.add(value);
			}
			return uniquevalues;
		}

		public Collection<T> getRownamesByColValue(Object val)
		{
			Set<T> rownames=Sets.newLinkedHashSet();
			for (T rowname : values.keySet())
			{
				Object value=values.get(rowname);
				System.out.println("if "+value+".equals("+val+"): "+value.equals(val));
				if (value.equals(val))
					rownames.add(rowname);
			}
			return rownames;
		}
	}

	public interface IntervalListener
	{
		public boolean onInterval(Parser parser, int rownum);
	}
	
	public abstract static class Parser
	{
		protected Options options;
		protected int interval=10;
		protected List<IntervalListener> listeners=Lists.newArrayList();
		protected BiMap<Integer,String> headerfields=HashBiMap.create();
		protected DataFrame<String> dataframe;
		protected Map<Integer,DataFrame<String>.Column> columns;
		
		public Parser(){}
		
		public Parser(Options options)
		{
			this.options=options;
		}
		
		public int getInterval(){return this.interval;}
		public void setInterval(final int interval){this.interval=interval;}
		
		public void addIntervalListener(IntervalListener listener)
		{
			listeners.add(listener);
		}
		
		public DataFrame<String> getDataFrame(){return dataframe;}
		
		protected boolean readHeader(List<String> fields)
		{
			fields=preProcessHeader(fields);
			for (int index=0;index<fields.size();index++)
			{
				String colname=fields.get(index);
				headerfields.put(index, colname);
			}
			postProcessHeader(fields);
			setupRownames();
			System.out.println("idcols="+options.idcols);
			resetDataFrame();
			return true;
		}
		
		protected void setupRownames()
		{
			System.out.println("setupRownames");
			if (options.idcols!=null)
				return;
			if (options.idnames==null)
			{
				options.idcols=Lists.newArrayList(0);
				return;
			}
			options.idcols=Lists.newArrayList();
			for (String idname : options.idnames)
			{
				Integer idcol=headerfields.inverse().get(idname);
				options.idcols.add(idcol);
			}
			System.out.println("idcols="+options.idcols);
		}
		
		public void resetDataFrame()
		{
			dataframe=new DataFrame<String>();
			columns=Maps.newLinkedHashMap();
			for (Integer index : headerfields.keySet())
			{
				String colname=headerfields.get(index);
				DataFrame<String>.Column column=this.dataframe.addColumn(colname);
				this.columns.put(index,column);
			}
		}
		
		protected boolean readLine(List<String> values, int rownum)
		{
			values=preProcessLine(values,rownum);
			if (values.size()!=columns.size())
				throw new CException("numbers of fields and headings don't match: fields="+values.size()+", columns="+columns.size()+" in row "+rownum);
			String rowname=getRowname(values);
			System.out.println("rowname="+rowname);
			this.dataframe.rownames.add(rowname);
			for (int index=0;index<values.size();index++)
			{
				DataFrame<String>.Column column=this.columns.get(index);
				String value=values.get(index).trim();
				//System.out.println("setting col="+column.getName()+", row="+rownum+", value="+value);
				column.values.put(rowname,value);
			}
			postProcessLine(values,rownum);
			return true;
		}
		
		protected String getRowname(List<String> values)
		{
			List<String> list=Lists.newArrayList();
			for (Integer idcol : options.idcols)
			{
				list.add(values.get(idcol).trim());				
			}
			return StringHelper.join(list,options.keyDelimiter);
		}
		
		protected boolean notifyListeners(int rownum)
		{
			if (rownum%interval!=0)
				return true;
			boolean proceed=true;
			for (IntervalListener listener : listeners)
			{
				if (!listener.onInterval(this, rownum))
					proceed=false;
			}
			return proceed;
		}
		
		protected List<String> preProcessHeader(List<String> fields){return fields;}
		protected void postProcessHeader(List<String> fields){}
		
		protected List<String> preProcessLine(List<String> values, int rownum)
		{
			return values;
		}
	
		protected List<String> splitLine(String line)
		{
			return StringHelper.splitAsList(line,"\t");//line.split("\t");
		}
		
		protected void postProcessLine(List<String> values, int rownum)
		{
			if (rownum%options.ROW_STATUS_INTERVAL==0)
				System.out.println("reading line "+rownum);
		}
	}
	
	public static class TabFileParser extends Parser
	{	
		public TabFileParser(){}
		
		public TabFileParser(Options options)
		{
			super(options);
		}
		
		public void parseFile(String filename)
		{
			try		
			{
				Files.readLines(new File(filename), this.options.encoding, new LineProcessor<String>()
				{
					private int rownum=0;
					
					public boolean processLine(String line)
					{
						//System.out.println("line: "+line);
						if (rownum==0)
							readHeader(splitLine(line));
						else readLine(splitLine(line),rownum);
						if (!notifyListeners(rownum))
							return false;
						rownum++;
						return true;
					}
					
					public String getResult(){return null;};
				});	
			}
			catch (IOException e)
			{
				throw new CException(e);
			}
		}
		
		public void parse(String str)
		{
			try		
			{
				LineReader reader=new LineReader(new StringReader(str));
				String line;
				int rownum=0;
				while ((line=reader.readLine())!=null)
				{
					System.out.println("reading line: "+line);
					if (rownum==0)
						readHeader(splitLine(line));
					else readLine(splitLine(line),rownum);
					rownum++;
				}
			}
			catch (IOException e)
			{
				throw new CException(e);
			}
		}
	}
	
	public static DataFrame<String> parseTabFile(String filename)
	{
		return parseTabFile(filename,new Options());
	}
	
	public static DataFrame<String> parseTabFile(String filename, Options options)
	{
		TabFileParser parser=new TabFileParser(options);
		parser.parseFile(filename);
		return parser.getDataFrame();
	}
	
	public static DataFrame<String> parse(String str)
	{
		return parse(str,new Options());
	}
	
	public static DataFrame<String> parse(String str, Options options)
	{
		TabFileParser parser=new TabFileParser(options);
		parser.parse(str);
		return parser.getDataFrame();
	}
	
	public static class Options
	{
		public int ROW_STATUS_INTERVAL=100000;
		public Charset encoding=Charsets.UTF_8;
		public List<Integer> idcols;
		public List<String> idnames;
		public String keyDelimiter="_";
		
		public Options(){}
		
		public Options(Charset encoding)
		{
			this.encoding=encoding;
		}
		
		public Options(String...colnames)
		{
			this.idnames=Lists.newArrayList(colnames);
		}
		
		public Options(Integer...colnums)
		{
			this.idcols=Lists.newArrayList(colnums);
		}
	}
}