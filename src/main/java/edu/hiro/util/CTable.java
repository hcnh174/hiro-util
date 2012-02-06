package edu.hiro.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import au.com.bytecode.opencsv.CSVReader;

public class CTable
{
	protected String identifier;
	protected Row header=new Row();
	protected List<Row> rows=new ArrayList<Row>();
	protected boolean hasHeaders=true;
	protected boolean showHeader=true;
	protected Map<String,Integer> cache;
	
	public String getIdentifier(){return this.identifier;}
	public void setIdentifier(final String identifier){this.identifier=identifier;}
	
	public Row getHeader(){return this.header;}
	public void setHeader(Row header){this.header=header;}

	public List<Row> getRows(){return this.rows;}
	public void setRows(List<Row> rows){this.rows=rows;}
	
	public boolean getShowHeader(){return this.showHeader;}
	public void setShowHeader(final boolean showHeader){this.showHeader=showHeader;}
	
	public CTable(){}
	
	public CTable(boolean hasHeaders)
	{
		this.hasHeaders=hasHeaders;
	}
	
	public CTable(String str)
	{
		parse(str,this,"\t");
	}
	
	public CTable(String str, boolean hasHeaders)
	{
		this.hasHeaders=hasHeaders;
		if (!this.hasHeaders)
			str="header1\theader2\n"+str;
		parse(str,this,"\t");
	}
	
	public CTable(String str, String identifier)
	{
		this(str);
		this.identifier=identifier;
	}
	
	public CTable(Map<String,String> map, String column1, String column2)
	{
		getHeader().add(column1);
		getHeader().add(column2);
		for (Map.Entry<String,String> entry : map.entrySet())
		{
			Row row=addRow();
			row.add(entry.getKey());
			row.add(entry.getValue());
		}
	}
	
	public void add(Row row)
	{
		this.rows.add(row);
	}
	
	public void add(Column column)
	{
		this.header.getCells().add(column.getHeader().getCells().get(0));
		
		// make sure there are enough rows
		if (this.rows.size()<column.getCells().size())
		{
			for (int index=0;index<column.getCells().size();index++)
			{
				this.rows.add(new Row());
			}
		}
		
		for (int index=0;index<column.getCells().size();index++)
		{
			Cell cell=column.getCells().get(index);
			if (cell.getRow()!=null && cell.getRow().getId()!=null)
			{
				Row row=getRow(cell.getRow().getId());
				if (row==null)
				{
					row=this.rows.get(index);
					row.setId(cell.getRow().getId());
				}
				row.add(cell);
			}
			else this.rows.get(index).add(cell);
		}
	}
	
	// accepts a number outside of the range
	public List<Row> getRows(int start, int limit)
	{
		int end=start+limit;
		if (end>=this.rows.size())
			end=this.rows.size();
		return this.rows.subList(start,end);
	}
	
	public void deleteRow(int index)
	{
		this.rows.remove(index);
	}
	
	public void deleteColumn(int index)
	{
		this.header.getCells().remove(index);
		for (Row row : this.rows)
		{
			row.getCells().remove(index);
		}
	}
	
	public Column getColumn(int index)
	{
		Column column=new Column();
		column.getHeader().add(this.header.getCells().get(index));
		for (Row row : this.rows)
		{
			column.add(row.getCells().get(index));
		}
		return column;
	}
	
	public Row createRow()
	{
		return new Row();
	}
	
	public Row addRow()
	{
		Row row=createRow();
		add(row);
		return row;
	}
	
	public Row addRow(int id)
	{
		Row row=new Row(id);
		add(row);
		return row;
	}
	
	public Row getRow(int id)
	{
		for (Row row : this.rows)
		{
			if (row.getId()!=null && row.getId()==id)
				return row;
		}
		return null;
	}
	
	public int getColumn(String name)
	{
		Integer column=findColumn(name);
		if (column==null)
			throw new CException("can't find column "+name);
		return column;
	}

	// finds a column given several alternative names
	public Integer findColumn(String... colnames)
	{
		if (this.cache==null)
			createCache();
		for (String colname : colnames)
		{
			Integer col=this.cache.get(colname);
			if (col!=null)
				return col;
		}
		return null;
	}
	
	/*
	public Integer findColumn(String name)
	{
		if (this.cache==null)
			createCache();
		return this.cache.get(name);
	}
	*/
	
	public Row findRow(int column, String value)
	{
		for (Row row : this.rows)
		{
			if (row.getValue(column).equals(value))
				return row;
		}
		return null;
	}
	
	private void createCache()
	{
		this.cache=new LinkedHashMap<String,Integer>();
		for (int index=0;index<this.header.getCells().size();index++)
		{
			Cell cell=this.header.getCells().get(index);
			if (cell.getValue()!=null)
			{
				this.cache.put(cell.getValue().toString(),index);
				//System.out.println("caching column["+cell.getValue().toString()+" as "+index);
			}
		}
	}
	
	public Map<String,String> getColumnData(String column)
	{
		int col=findColumn(column);
		Map<String,String> map=new LinkedHashMap<String,String>();
		for (CTable.Row row : getRows())
		{
			String identifier=row.getValue(0);
			String value=row.getValue(col);
			if (StringHelper.isEmpty(value))
				continue;
			map.put(identifier,value);
		}
		return map;
	}
	
	public String getHeader(int col)
	{
		return getHeader().getValue(col);
	}
	
	public void addHeader(String name)
	{
		getHeader().add(name);
	}
	
	public List<String> getColnames()
	{
		List<String> colnames=Lists.newArrayList();
		for (Cell cell : header.getCells())
		{
			colnames.add(cell.getStringValue());
		}
		return colnames;
	}
	
	public int getNumColumns()
	{
		return getHeader().getCells().size();
	}

	public DataType guessDataType(int col, String missing)
	{
		Column column=getColumn(col);
		return column.guessDataType(missing);
	}
	
	// find emty columns
	// create a new table skipping these columns
	public CTable condense()
	{
		CTable table=new CTable();
		for (int col=0;col<this.header.getCells().size();col++)
		{
			Column column=getColumn(col);
			if (!column.isEmpty() && !column.isFalse())
				table.add(column);
		}
		return table;
	}
	
	public CTable getPage(int start, int limit)
	{
		StringBuilder buffer=new StringBuilder();
		this.header.toString(buffer);
		for (Row row : getRows(start,limit))
		{
			row.toString(buffer);
		}
		return new CTable(buffer.toString());
	}
	
	public int size()
	{
		return this.rows.size();
	}
	
	public String toHtml(){return getHtml();}
	
	public String getHtml()
	{
		StringBuilder buffer=new StringBuilder();
		buffer.append("<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\">\n");
		this.header.toHtml(buffer);
		for (Row row : this.rows)
		{
			row.toHtml(buffer);
		}
		buffer.append("</table>\n");
		return buffer.toString();
	}
	
	public String getJson()
	{
		StringBuilder buffer=new StringBuilder();
		buffer.append("[\n");
		for (int r=0;r<this.rows.size();r++)
		{
			Row row=this.rows.get(r);
			buffer.append("{");
			for (int c=0;c<this.header.getCells().size();c++)
			{
				String name=this.header.getCell(c).getValue().toString();
				Object value=row.getCell(c).getValue();
				buffer.append("\t").append(name).append(": ");
				if (value instanceof Integer || value instanceof Float || value instanceof Boolean)
					buffer.append(value);
				else buffer.append("\"").append(value).append("\"");
				if (c<this.header.getCells().size()-1)
					buffer.append(",");
				buffer.append("\n");
			}
			buffer.append("}");
			if (r<this.rows.size()-1)
				buffer.append(",");
			buffer.append("\n");
		}
		buffer.append("]\n");
		return buffer.toString();
	}
	
	public boolean isEmpty()
	{
		return (this.header.getCells().isEmpty() && this.rows.isEmpty());
	}
	
	public static boolean hasTabs(String str)
	{
		return (str.indexOf('\t')!=-1);
	}
	
	public String toString()
	{
		if (isEmpty())
			return "";
		StringBuilder buffer=new StringBuilder();
		if (this.showHeader)
			this.header.toString(buffer);
		for (Row row : this.rows)
		{
			row.toString(buffer);
		}
		return buffer.toString();
	}
	
	public static CTable parse(String str)
	{
		return parseTabDelimited(str);
	}
	
	public static CTable parseTabDelimited(String str)
	{
		return parse(str,"\t");
	}
	
	public static CTable parseCsv(String str)
	{
		return parse(str,",");
	}
	
	public static CTable parse(String str, String delimiter)
	{
		CTable table=new CTable();
		parse(str,table,delimiter);
		return table;
	}
	
	private static void parse(String str, CTable table, String delimiter)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new StringReader(str));
			boolean firstline=true;
			for (String line=reader.readLine(); line!=null; line=reader.readLine())
			{
				readLine(line,firstline,table,delimiter);
				firstline=false;
			}
		}
		catch(Exception e)
		{
			throw new CException(e);
		}
	}
	
	public static CTable parseFile(String filename, String delimiter)
	{
		CTable table=new CTable();
		parseFile(filename,table,delimiter);
		return table;		
	}
	
	public static void parseFile(String filename, CTable table, String delimiter)
	{
		String str=FileHelper.readFile(filename);
		parse(str,table,delimiter);
	}
	
	private static void readLine(String line, boolean firstline, CTable table, String delimiter)
	{
		if (StringHelper.isEmpty(line))
			return;
		if (line.charAt(0)=='#')
			return;
		Row row=null;
		if (firstline)
			row=table.getHeader();
		else row=table.addRow();
		for (String value : StringHelper.split(line,delimiter))
		{
			row.add(StringHelper.trim(value));
		}
	}
	
	public List<String> getIdentifiers()
	{
		return getIdentifiers(this.rows,0);
	}
	
	public List<String> getIdentifiers(Collection<Row> rows, String column)
	{
		Integer col=findColumn(column);
		if (col==null)
			throw new CException("cannot find identifier column: "+column);
		System.out.println("index for column "+column+" is "+col);
		return getIdentifiers(rows,col);
	}
	
	public List<String> getIdentifiers(Collection<Row> rows, int col)
	{
		List<String> identifiers=new ArrayList<String>();
		for (CTable.Row row : rows)
		{
			String identifier=row.getValue(col);
			if (StringHelper.hasContent(identifier))
				identifiers.add(identifier);
		}
		return identifiers;
	}
	
	public List<Integer> getNumericIdentifiers(Collection<Row> rows, int col)
	{
		List<Integer> identifiers=new ArrayList<Integer>();
		for (CTable.Row row : rows)
		{
			String value=row.getValue(col);
			Integer identifier=MathHelper.parseInt(value);
			if (identifier!=null)
				identifiers.add(identifier);
		}
		return identifiers;
	}
	
	public List<String> getColumnValues(String name)
	{
		List<String> values=new ArrayList<String>();
		Integer index=findColumn(name);
		if (index==null)
			return values;//throw new CException("can't find column "+name);
		for (Row row : this.rows)
		{
			String value=row.getValue(index);
			if (!values.contains(value))
				values.add(value);
		}
		return values;
	}
	
	/*
	public static List<String> getIdentifiersFromFile(String filename)
	{
		final List<String> identifiers=new ArrayList<String>();
		final AbstractFileReader reader=new AbstractFileReader()
		{
			protected boolean firstline=true;
			
			protected boolean handleLine(String line)
			{
				if (line.charAt(0)=='#')
					return true;
				if (StringHelper.isEmpty(line))
					return true;
				if (this.firstline)
				{
					this.firstline=false;
					return true;
				}
				String identifier=line;
				int index=line.indexOf('\t');
				if (index!=-1)
					identifier=line.substring(0,index);
				identifiers.add(identifier);
				return true;
			}
		};
		reader.readFile(filename);
		return identifiers;
	}
	*/
	
	public void insertRowNumberColumn(String name)
	{
		getHeader().insertAt(0,name);
		int rowID=1;
		for (CTable.Row row : getRows())
		{
			row.insertAt(0,rowID);
			rowID++;
		}
	}
	
	public CTable getFieldLengthReport()
	{
		CTable table=new CTable();
		table.getHeader().add("field");
		table.getHeader().add("maxlength");
		table.getHeader().add("maxvalue");
		for (Cell cell : getHeader().getCells())
		{
			String colname=cell.getStringValue();
			int max=0;
			String maxvalue=null;
			for (String value : getColumnValues(colname))
			{
				if (value==null)
					continue;
				int length=value.length();
				if (length>max)
				{
					max=length;
					maxvalue=value;
				}
			}
			Row row=table.addRow();
			row.add(colname);
			row.add(max);
			row.add(maxvalue);
		}
		return table;
	}
	
	public static CTable createTable(Collection<?> list, List<String> columns)
	{
		BeanHelper beanhelper=new BeanHelper();
		CTable table=new CTable();
		for (String column : columns)
		{
			table.getHeader().add(column);
		}
		
		for (Object obj : list)
		{
			CTable.Row row=table.addRow();
			for (String column : columns)
			{
				Object value=beanhelper.getProperty(obj,column);
				row.add(value);
			}
		}
		return table;
	}

	public class Row
	{
		protected Integer id;
		protected List<Cell> cells=new ArrayList<Cell>();
		protected String style;
		
		public Integer getId(){return this.id;}
		public void setId(final Integer id){this.id=id;}
		
		public List<Cell> getCells(){return this.cells;}
		public void setCells(List<Cell> cells){this.cells=cells;}
		
		public String getStyle(){return this.style;}
		public void setStyle(String style){this.style=style;}
		
		public Row(){}
		
		public Row(int id)
		{
			this.id=id;
		}
		
		public void add(Cell cell)
		{
			cell.setRow(this);
			this.cells.add(cell);
		}
		
		public Cell add(Object obj)
		{
			Cell cell=new Cell(obj);
			add(cell);
			return cell;
		}
		
		public void insertAt(int index, Cell cell)
		{
			cell.setRow(this);
			this.cells.add(index,cell);
		}
		
		public Cell insertAt(int index, Object obj)
		{
			Cell cell=new Cell(obj);
			insertAt(index,cell);
			return cell;
		}

		public void addAll(List<Cell> cells)
		{
			for (Cell cell : cells)
			{
				add(cell.getValue());
			}
		}
		
		public Cell getCell(int index)
		{
			return this.cells.get(index);
		}
		
		public String getValue(Integer index)
		{
			if (index==null)
				return null;
			if (index>=this.cells.size())
				return null;
			Cell cell=this.cells.get(index);
			if (cell==null || cell.getValue()==null)
				return null;
			return cell.getValue().toString();
		}
		
		public String getValue(Integer index, String dflt)
		{
			String value=getValue(index);
			if (value==null)
				return dflt;
			return value;
		}
		
		public List<Object> getValues()
		{
			List<Object> values=Lists.newArrayList();
			for (Cell cell : cells)
			{
				values.add(cell.getValue());
			}
			return values;
		}
		
		public List<String> getStringValues()
		{
			List<String> values=Lists.newArrayList();
			for (Cell cell : cells)
			{
				Object value=cell.getValue();
				if (value==null)
					value="";
				values.add(value.toString());
			}
			return values;
		}
		
		public int size()
		{
			return this.cells.size();
		}
		
		public void toHtml(StringBuilder buffer)
		{
			buffer.append("<tr");
			if (this.style!=null)
				buffer.append(" style=\""+this.style+"\"");
			buffer.append(">");
			for (Cell cell : this.cells)
			{
				cell.toHtml(buffer);
			}
			buffer.append("</tr>\n");
		}
		
		@Override
		public String toString()
		{
			StringBuilder buffer=new StringBuilder();
			toString(buffer);
			return buffer.toString();
		}
		
		public void toString(StringBuilder buffer)
		{
			boolean first=true;
			for (Cell cell : this.cells)
			{
				if (first)
					first=false;
				else buffer.append("\t");
				cell.toString(buffer);
			}
			buffer.append("\n");
		}
	}
		
	public static class Cell
	{
		protected Row row;
		protected Object value;
		protected String align;
		protected String style;
		
		public Row getRow(){return this.row;}
		public void setRow(final Row row){this.row=row;}
		
		public Object getValue(){return this.value;}
		public void setValue(Object value){this.value=value;}
		
		public String getAlign(){return this.align;}
		public void setAlign(String align){this.align=align;}
		
		public String getStyle(){return this.style;}
		public void setStyle(String style){this.style=style;}
		
		public Cell(){}
		
		public Cell(Object value)
		{
			this.value=value;
		}
		
		public boolean isEmpty()
		{
			return !StringHelper.hasContent(this.value);
		}
		
		public String getStringValue()
		{
			return this.value.toString();
		}
		
		public void toHtml(StringBuilder buffer)
		{
			buffer.append("\t<td");
			if (this.align!=null)
				buffer.append(" align=\""+this.align+"\"");
			if (this.style!=null)
				buffer.append(" style=\""+this.style+"\"");
			buffer.append(">");
			if (this.value==null || !StringHelper.hasContent(this.value))
				buffer.append("&nbsp;");
			else buffer.append(this.value);
			buffer.append("</td>\n");
		}
		
		public void toString(StringBuilder buffer)
		{
			if (this.value==null)
				return;
			//System.out.println("value type: "+this.value.getClass().toString());
			if (this.value instanceof Date)
				buffer.append(DateHelper.format((Date)this.value,DateHelper.DATE_PATTERN));
			else if (this.value instanceof String && this.value.toString().indexOf("\n")!=-1)
				buffer.append(StringHelper.replace(this.value.toString(), "\n", "|"));
			else buffer.append(this.value);
		}
	}
	
	public class Column
	{
		protected Row header=new Row();
		protected List<Cell> cells=new ArrayList<Cell>();
		
		public Row getHeader(){return this.header;}
		
		public List<Cell> getCells(){return this.cells;}
		
		public Column(){}
		
		public Column(String name, List<Object> values)
		{
			getHeader().add(name);
			for (Object value : values)
			{
				add(value);
			}
		}
		
		public Column(String name, double[] values)
		{
			getHeader().add(name);
			for (double value : values)
			{
				add(value);
			}
		}
		
		public Column(String name, int[] values)
		{
			getHeader().add(name);
			for (double value : values)
			{
				add(value);
			}
		}
		
		public void add(Cell cell)
		{
			this.cells.add(cell);
		}
		
		public void add(Object obj)
		{
			this.cells.add(new Cell(obj));
		}
		
		public boolean isEmpty()
		{
			for (Cell cell : this.cells)
			{
				if (!cell.isEmpty())
					return false;
			}
			return true;
		}
		
		public boolean isFalse()
		{
			for (Cell cell : this.cells)
			{
				if (!cell.isEmpty())
					return false;
				if ((cell.getValue() instanceof Boolean) && !((Boolean)cell.getValue()).booleanValue())
					return false;
			}
			return true;
		}
		
		public DataType guessDataType(String missing)
		{
			List<Object> values=new ArrayList<Object>();
			for (Cell cell : this.cells)
			{
				if (cell.getValue()!=null && !cell.getStringValue().equals("missing"))
					values.add(cell.getValue());
			}
			return DataType.guessDataType(values);
		}
		
		public List<String> findUniqueValues(String missing)
		{
			List<String> values=new ArrayList<String>();	
			for (Cell cell : this.cells)
			{
				String value=cell.getValue().toString().toLowerCase().trim();
				if (value.equals(missing))
					continue;
				if (!values.contains(value))
					values.add(value);
			}
			return values;
		}
	}
	
	/*
	public static DataType guessDataType(Collection<Object> values, String missing)
	{
		boolean is_integer=true;
		boolean is_float=true;
		boolean is_boolean=true;			
		for (Object obj : values)
		{
			String value=obj.toString().toLowerCase().trim();
			if (value.equals(missing) || StringHelper.isEmpty(value))
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
	*/
	
	public static CTable parseCvsFile(String filename)
	{
		return parseCvsFile(filename,FileHelper.Encoding.UTF8);
	}
	
	public static CTable parseCvsFile(String filename, FileHelper.Encoding encoding)
	{
		BufferedReader filereader=null;
		try		
		{
			CTable table=new CTable();
			filereader=new BufferedReader(new InputStreamReader(new FileInputStream(filename),encoding.toString()));
			CSVReader reader = new CSVReader(filereader);
		    String[] fields;
		    boolean firstline=true;
		    while ((fields = reader.readNext()) != null)
		    {
		    	if (firstline)
		    	{
		    		readHeader(fields,table);
		    		firstline=false;
		    	}
		    	else
		    	{
		    		readLine(fields,table);
		    	}
		    }
		    return table;
		}
		catch(Exception e)
		{
			throw new CException(e);
		}
		finally
		{
			FileHelper.closeReader(filereader);
		}		
	}
	
	private static void readHeader(String[] fields, CTable table)
	{
		for (String field : fields)
		{
			table.getHeader().add(field);
		}
	}
	
	private static void readLine(String[] values, CTable table)
	{
		if (values.length!=table.getHeader().size())
			throw new CException("CTable.readLine: numbers of fields and headings don't match: fields="+values.length+", columns="+table.getHeader().size());
		CTable.Row row=table.addRow();
		for (String value : values)
		{			
			row.add(value);
		}
	}	
}
