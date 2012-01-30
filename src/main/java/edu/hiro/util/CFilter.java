package edu.hiro.util;


public abstract class CFilter
{	
	public interface Filter
	{
		String getText();
	}
	
	public static class ElementaryFilter implements Filter
	{
		private String field;
		private FieldOperator operator;
		private String value;
		
		public String getField(){return this.field;}
		public void setField(String field){this.field=field;}

		public FieldOperator getOperator(){return this.operator;}
		public void setOperator(FieldOperator operator){this.operator=operator;}

		public String getValue(){return this.value;}
		public void setValue(String value){this.value=value;}

		public ElementaryFilter(String field, FieldOperator operator, String value)
		{
			this.field=field;
			this.operator=operator;
			this.value=value;
		}
		
		public String getText()
		{
			return this.field+this.operator.getText()+this.value;
		}
		
		public SqlOperator getSqlOperator(){return this.operator.getSqlOperator();}
	}
	
	public static class CompositeFilter implements Filter
	{
		private Filter left;
		private LogicalOperator operator;
		private Filter right;
		
		public Filter getLeft(){return this.left;}
		public void setLeft(Filter left){this.left=left;}

		public LogicalOperator getOperator(){return this.operator;}
		public void setOperator(LogicalOperator operator){this.operator=operator;}

		public Filter getRight(){return this.right;}
		public void setRight(Filter right){this.right=right;}
		
		public CompositeFilter(Filter left, LogicalOperator operator, Filter right)
		{
			this.left=left;
			this.operator=operator;
			this.right=right;
		}
		
		public String getText()
		{
			return this.left.getText()+" "+this.operator.getText()+" ("+this.right.getText()+")";
		}
	}
	
	public enum LogicalOperator
	{
		AND,
		OR,
		NOT;
		
		public String getText()
		{
			return name();
		}
	}
	
	public enum FieldOperator
	{
		EQUALS("=",false,SqlOperator.STRING_EQUALS),
		NOT_EQUALS("!=",false,SqlOperator.STRING_NOT_EQUALS),
		GREATER_THAN(">",true,SqlOperator.NUMBER_GREATER),
		LESS_THAN("<",true,SqlOperator.NUMBER_LESS),
		GREATER_THAN_OR_EQUAL(">=",true,SqlOperator.NUMBER_GREATER_OR_EQUAL),
		LESS_THAN_OR_EQUAL("<=",true,SqlOperator.NUMBER_LESS_OR_EQUAL);
		
		private String text;
		private boolean numeric;
		private SqlOperator operator;
		
		FieldOperator(String text, boolean numeric, SqlOperator operator)
		{
			this.text=text;
			this.numeric=numeric;
			this.operator=operator;
		}
		
		public String getText(){return this.text;}
		
		public boolean isNumeric(){return this.numeric;}
		
		public SqlOperator getSqlOperator(){return this.operator;}
		
		public static FieldOperator find(String token)
		{
			for (FieldOperator operator : values())
			{
				if (operator.getText().equals(token))
					return operator;
			}
			throw new CException("cannot find field operator for token: ["+token+"]");
		}
	}
}
