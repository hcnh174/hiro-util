package edu.hiro.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.RecordFormatException;

public class ExcelHelper
{	
	protected MessageWriter writer=new MessageWriter();
	
	public ExcelHelper(){}
	
	public ExcelHelper(MessageWriter writer)
	{
		this.writer=writer;
	}
	
	public MessageWriter getWriter(){return this.writer;}
	public void setWriter(final MessageWriter writer){this.writer=writer;}
	
	//////////////////////////////////////////////////////////
	
	// try to create a table based on a cells in the first sheet of a spreadsheet
	// stop columns when first row cell is blank
	// stop rows when first column cell is blank (other cells may be empty)
	public CTable extractTable(String filename)
	{
		Workbook workbook=openSpreadsheet(filename);
		Sheet sheet = workbook.getSheetAt(0);
		CTable table=extractTable(sheet);
		table.setIdentifier(FileHelper.getIdentifierFromFilename(filename));
		return table;
	}
	
	public CTable extractTable(Sheet sheet)
	{
		CTable table=new CTable();
		boolean isHeader=true;
		for (Row row : sheet)
		{
			if (isHeader)
			{
				extractHeaderRow(row,table);
				isHeader=false;
			}
			else extractRow(row,table);
		}
		return table;
	}
	
	private void extractHeaderRow(Row row, CTable table)
	{
		CTable.Row trow=table.getHeader();
		for (Cell cell : row)
		{
			String value=cell.getStringCellValue();
			//System.out.println("header value=["+value+"]");
			if (!hasContent(value))
				return;
			trow.add(value);
		}
	}
	
	private void extractRow(Row row, CTable table)
	{
		// if the first column is empty, skip and return
		if (!hasContent(getCellValue(row.getCell(0))))
		{
			writer.message("first column is empty - skipping");
			return;
		}
		CTable.Row trow=table.addRow();
		// only read as many columns as there are header fields
		for (int colnum=0; colnum<table.getHeader().size();colnum++)
		{
			Cell cell=row.getCell(colnum);
			if (colnum==0)
				trow.add(getIdentifierCellValue(cell));
			else trow.add(getCellValue(cell));
		}
	}
	
	// copied here to remove a dependency on SpringUtils
	private boolean hasContent(Object obj)
	{
		if (obj==null)
			return false;
		String value=obj.toString();
		if (value.length()==0)
			return false;
		value=value.trim();
		if (value.length()==0)
			return false;
		return !"".equals(value);
	}
	
	/////////////////////////////////////////////////
	
	public DataFrame extractDataFrame(Sheet sheet)
	{
		DataFrame dataframe=new DataFrame();
		boolean isHeader=true;
		for (Row row : sheet)
		{
			if (isHeader)
			{
				extractHeaderRow(row,dataframe);
				isHeader=false;
			}
			else extractRow(row,dataframe);
		}
		return dataframe;
	}
	
	private void extractHeaderRow(Row row, DataFrame dataframe)
	{
		//CTable.Row trow=table.getHeader();
		for (Cell cell : row)
		{
			String colname=cell.getStringCellValue();
			if (!StringHelper.hasContent(colname))
				return;
			dataframe.addColumn(colname);
		}
	}
	
	private void extractRow(Row row, DataFrame dataframe)
	{
		// if the first column is empty, skip and return
		if (!StringHelper.hasContent(getCellValue(row.getCell(0))))
		{
			writer.message("first column is empty - skipping");
			return;
		}
		//CTable.Row trow=table.addRow();
		// only read as many columns as there are header fields
		//for (int colnum=0; colnum<table.getHeader().size();colnum++)
		String rowname=getIdentifierCellValue(row.getCell(0));		
		for (int colnum=1; colnum<dataframe.getNumCols(); colnum++)
		{
			Cell cell=row.getCell(colnum);
			String colname=dataframe.getColNames().get(colnum);
			dataframe.setValue(colname, rowname, getCellValue(cell));
		}
	}
	
	//////////////////////////////////////////////////////

	private String getIdentifierCellValue(Cell cell)
	{
		Object value=getCellValue(cell);
		if (value instanceof Double)
			return StringHelper.formatDecimal((Double)value,0);
		else return value.toString();
	}
	
	public Object getCellValue(Cell cell)
	{
		if (cell==null)
			return null;
		if (cell.getCellType()==Cell.CELL_TYPE_BLANK)
			return null;
		//System.out.println("cell type: "+cell.getCellType());
		switch(cell.getCellType())
		{
			case Cell.CELL_TYPE_BLANK:
				return null;//"(blank)"; // hack! temporary
			case Cell.CELL_TYPE_STRING:
				return cell.getRichStringCellValue().getString();
			case Cell.CELL_TYPE_NUMERIC:
				return getNumericValue(cell);
			case Cell.CELL_TYPE_BOOLEAN:
				return cell.getBooleanCellValue();
			case Cell.CELL_TYPE_FORMULA:
				return getFormulaValue(cell);
			case Cell.CELL_TYPE_ERROR:
				return cell.getErrorCellValue();
			default:
				//System.out.println("unhandled cell type: "+cell.getCellType());
				writer.message("unhandled cell type: "+cell.getCellType());
				return null;
		}
	}
		
	private Object getFormulaValue(Cell cell)
	{
		try
		{
			if (cell.getCellType()==Cell.CELL_TYPE_BLANK)
				return null;
			FormulaEvaluator evaluator = getFormulaEvaluator(cell);
			CellValue cellValue = evaluator.evaluate(cell);
			switch(cellValue.getCellType())
			{
				case Cell.CELL_TYPE_BLANK:
					return null;//"(blank)"; // hack! temporary
				case Cell.CELL_TYPE_STRING:
					return cellValue.getStringValue();
				case Cell.CELL_TYPE_NUMERIC:
					return getNumericValue(cell,cellValue);
				case Cell.CELL_TYPE_BOOLEAN:
					return cellValue.getBooleanValue();				
				case Cell.CELL_TYPE_ERROR:
					return cellValue.formatAsString();
				default:
					writer.message("unhandled cellValue type: "+cellValue.getCellType());
					return null;
			}
		}
		catch (NotImplementedException e)
		{
			writer.message("NotImplementedException: ["+"="+cell.getCellFormula().toString()+"]");
			return null;
		}
		catch (Exception e)
		{
			writer.message(e.toString());
			writer.message("="+cell.getCellFormula().toString());
			return null;
		}
	}
	
	private Object getNumericValue(Cell cell)
	{
		if (DateUtil.isCellDateFormatted(cell))
			return cell.getDateCellValue();
		else return cell.getNumericCellValue();
	}
	
	private Object getNumericValue(Cell cell, CellValue cellValue)
	{
		if (DateUtil.isCellDateFormatted(cell))
			return DateUtil.getJavaDate(cellValue.getNumberValue());
		else return cellValue.getNumberValue();
	}

	public Workbook openSpreadsheet(String filename)
	{
		try
		{
			InputStream instream = new FileInputStream(filename);
			return WorkbookFactory.create(instream);
		}
		catch (RecordFormatException e)
		{
			/*
			writer.error("RecordFormatException: failed to open file "+filename);
			writer.error(e);
			return null;
			*/
			throw new CException(e);
		}
		catch (FileNotFoundException e)
		{
			throw new CException(e);
		}
		catch (IOException e)
		{
			throw new CException(e);
		}
		catch (org.apache.poi.openxml4j.exceptions.InvalidFormatException e) {
			throw new CException(e);
		}
	}

	public Object getCellValue(Sheet sheet, int rownum, int colnum)
	{
		Row row=sheet.getRow(rownum);
		if (row==null)
			return null;//throw new CException("row is null for rownum "+rownum+" in sheet "+sheet.getSheetName());
		Cell cell=row.getCell(colnum);
		//if (cell==null)
			//throw new CException("cell is null for rownum "+rownum+" and colnum "+colnum+" in sheet "+sheet.getSheetName());
		return getCellValue(cell);
	}
	
	public Object getCellValue(Sheet sheet, String address)
	{
		CellReference cellReference = new CellReference(address);
		Row row = sheet.getRow(cellReference.getRow());
		Cell cell = row.getCell(cellReference.getCol());
		return getCellValue(cell);
	}
	
	public String getStringCellValue(Sheet sheet, String address)
	{
		Object value=getCellValue(sheet,address);
		if (value==null)
			return "";
		return value.toString();
	}
	
	/////////////////////////////////////////////////////////////
	
	private FormulaEvaluator getFormulaEvaluator(Cell cell)
	{
		Sheet sheet=cell.getSheet();
		Workbook workbook=sheet.getWorkbook();
		return workbook.getCreationHelper().createFormulaEvaluator();
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	
	private CellStyle headerStyle=null;
	
	public Workbook createWorkbook()
	{
		return new HSSFWorkbook();
	}
	
	public Workbook createWorkbook(String filename)
	{
		Workbook workbook = createWorkbook();
		writeWorkbook(workbook,filename);
		return workbook;
	}
	
	public void writeWorkbook(Workbook workbook, String filename)
	{
		FileOutputStream out=null;
		try
		{
			out = new FileOutputStream(filename);
			workbook.write(out);
		}
		catch(Exception e)
		{
			throw new CException(e);
		}
		finally
		{
			FileHelper.closeStream(out);
		}
	}
	
	public Sheet createWorksheet(Workbook workbook, DataFrame dataframe, String sheetname)
	{
		CTable table=dataframe.getTable();
		return createWorksheet(workbook,table,sheetname);
	}
	
	public Sheet createWorksheet(Workbook workbook, CTable table, String sheetname)
	{		
		Sheet sheet=workbook.createSheet(sheetname);
		int r=0;
		int c=0;
		
		for (CTable.Cell cell : table.getHeader().getCells())
		{
			setHeaderCell(sheet,c++,r,cell.getStringValue());
		}

		for (CTable.Row tablerow : table.getRows())
		{
			c=0;
			r++;
			for (CTable.Cell cell : tablerow.getCells())
			{
				setCell(sheet,c++,r,cell.getValue());
			}			
		}
		return sheet;
	}	
	
	public void setHeaderCell(Sheet sheet, int c, int r, String value)
	{
		if (value==null)
			return;
		Cell cell=getCell(sheet,c,r);
		cell.setCellValue(value);
		cell.setCellStyle(getHeaderCellStyle(sheet.getWorkbook()));
	}
	
	public void setCell(Sheet sheet, int c, int r, String value)
	{
		if (value==null)
			return;
		//System.out.println("adding cell at c="+c+", r="+r+", value="+value);
		Cell cell=getCell(sheet,c,r);
		cell.setCellValue(value);
	}
	
	public void setCell(Sheet sheet, int c, int r, Integer value)
	{
		if (value==null)
			return;
		Cell cell=getCell(sheet,c,r);
		cell.setCellValue(value);
	}
	
	public void setCell(Sheet sheet, int c, int r, Double value)
	{
		if (value==null)
			return;
		Cell cell=getCell(sheet,c,r);
		cell.setCellValue(value);
	}
	
	public void setCell(Sheet sheet, int c, int r, Double value, CellStyle style)
	{
		if (value==null)
			return;
		Cell cell=getCell(sheet,c,r);
		cell.setCellValue(value);
		cell.setCellStyle(style);
	}
	
	public void setCell(Sheet sheet, int c, int r, Float value)
	{
		if (value==null)
			return;
		Cell cell=getCell(sheet,c,r);
		cell.setCellValue(value);
	}
	
	public void setCell(Sheet sheet, int c, int r, Float value, CellStyle style)
	{
		if (value==null)
			return;
		Cell cell=getCell(sheet,c,r);
		cell.setCellValue(value);
		cell.setCellStyle(style);
	}
	
	public Cell setCell(Sheet sheet, String address, Object value, CellStyle style)
	{
		Cell cell=setCell(sheet,address,value);
		cell.setCellStyle(style);
		return cell;
	}
	
	public Cell setCell(Sheet sheet, String address, Object value)
	{
		if (address.contains(":"))
			return addRange(sheet,address,value);
		CellReference cellref = new CellReference(address);
		return setCell(sheet,cellref.getCol(),cellref.getRow(),value);
	}
	
	public Cell addRange(Sheet sheet, String range, Object value)
	{
		String cell1=range.split(":")[0];
		String cell2=range.split(":")[1];
		CellReference cellref1 = new CellReference(cell1);
		//System.out.println("range="+range+", cellref1="+cellref1.formatAsString());
		Cell cell=getCell(sheet,cellref1.getCol(),cellref1.getRow());
		//Row row = sheet.getRow(cellref1.getRow());
		//Cell cell = row.getCell(cellref1.getCol());
	    setCellValue(cell,value);

	    CellReference cellref2 = new CellReference(cell2);
	    sheet.addMergedRegion(new CellRangeAddress(
	    		cellref1.getRow(),//first row (0-based)
	    		cellref2.getRow(), //last row  (0-based)
	    		cellref1.getCol(), //first column (0-based)
	    		cellref2.getCol()  //last column  (0-based)
	    ));
	    return cell;
	}
	
	public Cell setCell(Sheet sheet, int c, int r, Object value, CellStyle style)
	{
		Cell cell=setCell(sheet,c,r,value);
		cell.setCellStyle(style);
		return cell;
	}
	
	public Cell setCell(Sheet sheet, int c, int r, Object value)
	{
		if (value==null)
			return null;
		Cell cell=getCell(sheet,c,r);
		setCellValue(cell,value);
		return cell;
	}
	
	public void setCellValue(Cell cell, Object value)
	{
		DataType type=DataType.guessDataType(value);
		switch(type)
		{
		case BOOLEAN:
			cell.setCellValue(Boolean.valueOf(value.toString()));
			return;
		case DATE:
			cell.setCellValue(DateHelper.parse(value.toString(),DateHelper.DATE_PATTERN));
			return;
		case INTEGER:
			cell.setCellValue(Integer.valueOf(value.toString().trim()));
			return;
		case FLOAT:
			cell.setCellValue(Float.valueOf(value.toString().trim()));
			return;
		default:
			cell.setCellValue(value.toString());
			return;
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////
	
	private Cell getCell(Sheet sheet, int c, int r)
	{
		//System.out.println("creating cell at c="+c+", r="+r);
		Row row=sheet.getRow(r);
		if (row==null)
			row=sheet.createRow(r);
		Cell cell=row.getCell(c);
		if (cell==null)
			cell=row.createCell(c);
		return cell;
	}
	
	/*
	@SuppressWarnings("unused")
	private CellStyle createDecimalFormat(int dps)
	{
		String pattern="#."+StringHelper.repeatString("#",dps);
		return createNumberFormat(pattern);
	}
	
	private CellStyle createNumberFormat(String pattern)
	{
		return new WritableCellFormat(new NumberFormat(pattern));
	}
	*/
	
	private CellStyle getHeaderCellStyle(Workbook workbook)
	{
		if (headerStyle==null)
		{
			headerStyle=workbook.createCellStyle();
			int fontSize=10;
			Font font=workbook.createFont();
			font.setFontHeightInPoints((short)fontSize);
			font.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerStyle.setFont(font);
			headerStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
		}
		return headerStyle;
	}
	
	public CellStyle createBorderedCellStyle(Workbook workbook)
	{
		CellStyle style=workbook.createCellStyle();
		style.setBorderTop(CellStyle.BORDER_MEDIUM);
		style.setBorderBottom(CellStyle.BORDER_MEDIUM);
		style.setBorderLeft(CellStyle.BORDER_MEDIUM);
		style.setBorderRight(CellStyle.BORDER_MEDIUM);
		return style;
	}
	
	public void setCellComment(Cell cell, String text)
	{
		CreationHelper factory = cell.getSheet().getWorkbook().getCreationHelper();	    
	    Drawing drawing = cell.getSheet().createDrawingPatriarch();
	    ClientAnchor anchor = factory.createClientAnchor();
	    Comment comment = drawing.createCellComment(anchor);
	    RichTextString str = factory.createRichTextString(text);
	    comment.setString(str);
	    comment.setAuthor("Chayama");
	    //assign the comment to the cell
	    cell.setCellComment(comment);
	}
	
	public String getAddress(Cell cell)
	{
		CellReference cellref = new CellReference(cell);
		return cellref.formatAsString();
	}
	
	public boolean hasContent(Cell cell)
	{
		Object value=getCellValue(cell);
		//if (value!=null && value.toString().equals("1"))
		//	System.out.println("has content ("+value+"): "+cell.getCellType());
		return (value!=null && !value.toString().trim().equals(""));
	}
	
	// copy sheets
	//http://www.coderanch.com/t/420958/open-source/Copying-sheet-excel-file-another
}
