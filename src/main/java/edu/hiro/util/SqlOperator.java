package edu.hiro.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public enum SqlOperator
{
	SUBQUERY
	{
		public String getSql(String field, String value)
		{
			return field+" in ("+value+")";
		}
	},
	NOT_SUBQUERY
	{
		public String getSql(String field, String value)
		{
			return field+" not in ("+value+")";
		}
	},
	STRING_EQUALS
	{
		public String getSql(String field, String value)
		{
			if (value==null)
				return field+IS_NULL;
			else return field+"='"+StringHelper.escapeSql(value)+"'";
		}
	},
	STRING_NOT_EQUALS(true)
	{
		public String getSql(String field, String value)
		{
			if (value==null)
				return field+IS_NOT_NULL;//" is not null";
			else return field+"<>'"+StringHelper.escapeSql(value)+"'";
		}
	},
	STRING_CONTAINS
	{
		public String getSql(String field, String value)
		{
			if (value==null)
				return ALL;
			else return field+" like '"+StringHelper.escapeSql(value)+"'";
			//else return field+" like '"+value+"%'";
		}
	},
	STRING_DOESNT_CONTAIN
	{
		public String getSql(String field, String value)
		{
			if (value==null)
				return ALL;
			else return field+" not like '"+StringHelper.escapeSql(value)+"'"; //%
		}
	},
	STRING_STARTS_WITH
	{
		public String getSql(String field, String value)
		{
			if (value==null)
				return ALL;
			else return field+" like '"+StringHelper.escapeSql(value)+"%'";
		}
	},
	NOT_STRING_STARTS_WITH
	{
		public String getSql(String field, String value)
		{
			if (value==null)
				return ALL;
			else return field+" not like '"+StringHelper.escapeSql(value)+"%'";
		}
	},
	STRING_ENDS_WITH
	{
		public String getSql(String field, String value)
		{
			if (value==null)
				return ALL;
			else return field+" like '%"+StringHelper.escapeSql(value)+"'";
		}
	},
	STRING_LIKE(true)// custom - assumes a wild card character
	{
		public String getSql(String field, String value)
		{
			if (value==null)
				return ALL;
			else return field+" ilike '"+StringHelper.escapeSql(value)+"'";
		}
	},
	STRING_NOT_LIKE // custom - assumes a wild card character
	{
		public String getSql(String field, String value)
		{
			if (value==null)
				return ALL;
			else return field+" not ilike '"+StringHelper.escapeSql(value)+"'";
		}
	},
	STRING_LIST
	{
		public String getSql(String field, String value)
		{
			List<String> values=getList(value);
			return field+" in ('"+StringHelper.join(values,"','")+"')";
		}
	},
	STRING_NOT_IN_LIST
	{
		public String getSql(String field, String value)
		{
			List<String> values=getList(value);
			return field+" not in ('"+StringHelper.join(values,"','")+"')";
		}
	},
	REGEX
	{
		public String getSql(String field, String value)
		{
			if (value==null)
				return ALL;
			else return field+" ~* '"+value+"'";
		}
	},
	NUMBER_EQUAL
	{
		public String getSql(String field, String val)
		{
			Float value=getFloat(val);
			if (value==null)
				return field+IS_NULL;
			else return field+"="+value;
		}
	},
	NUMBER_NOT_EQUAL(true)
	{
		public String getSql(String field, String val)
		{
			Float value=getFloat(val);
			if (value==null)
				return field+IS_NOT_NULL;//" is not null";
			else return field+"<>"+value;
		}
	},
	NUMBER_GREATER
	{
		public String getSql(String field, String val)
		{
			Float value=getFloat(val);
			if (value==null)
				return field+IS_NULL;
			else return field+">"+value;
		}
	},
	NUMBER_GREATER_OR_EQUAL
	{
		public String getSql(String field, String val)
		{
			Float value=getFloat(val);
			if (value==null)
				return field+IS_NULL;
			else return field+">="+value;
		}
	},
	NUMBER_LESS
	{
		public String getSql(String field, String val)
		{
			Float value=getFloat(val);
			if (value==null)
				return field+IS_NULL;
			else return field+"<"+value;
		}
	},
	NUMBER_LESS_OR_EQUAL
	{
		public String getSql(String field, String val)
		{
			Float value=getFloat(val);
			if (value==null)
				return field+IS_NULL;
			else return field+"<="+value;
		}
	},
	NUMBER_RANGE
	{
		public String getSql(String field, String value)
		{
			List<String> list=getList(value);
			if (list.size()!=2) // if doesn't have 2 numbers, always return false
				return NONE;
			Float from=getFloat(list.get(0));
			Float to=getFloat(list.get(1));
			return "("+field+">="+from+" and "+field+"<="+to+")";
		}
	},
	DATE_EQUAL
	{
		public String getSql(String field, String value)
		{
			Date date=getDate(value);
			if (date==null)
				return field+IS_NULL;
			else return field+"="+getSql(date);
		}
	},
	DATE_GREATER
	{
		public String getSql(String field, String value)
		{
			Date date=getDate(value);
			if (date==null)
				return field+IS_NULL;
			else return field+">"+getSql(date);
		}
	},
	DATE_GREATER_OR_EQUAL
	{
		public String getSql(String field, String value)
		{
			Date date=getDate(value);
			if (date==null)
				return field+IS_NULL;
			else return field+">="+getSql(date);
		}
	},
	DATE_LESS
	{
		public String getSql(String field, String value)
		{
			Date date=getDate(value);
			if (date==null)
				return field+IS_NULL;
			else return field+"<"+getSql(date);
		}
	},
	DATE_LESS_OR_EQUAL
	{
		public String getSql(String field, String value)
		{
			Date date=getDate(value);
			if (date==null)
				return field+IS_NULL;
			else return field+"<="+getSql(date);
		}
	},
	DATE_RANGE
	{
		public String getSql(String field, String value)
		{
			List<String> dates=getList(value);
			if (dates.size()!=2) // if doesn't have 2 dates, always return false
				return NONE;
			Date date1=DateHelper.parse(dates.get(0),INPUT_DATE_PATTERN);
			Date date2=DateHelper.parse(dates.get(1),INPUT_DATE_PATTERN);
			return field+">="+getSql(date1)+" and "+field+"<="+getSql(date2);
		}
	};
	
	private boolean m_useAnd=false;
	
	SqlOperator(){}
	
	SqlOperator(boolean useAnd)
	{
		m_useAnd=useAnd;
	}
	
	protected static final String INPUT_DATE_PATTERN="yyyyMMdd";
	protected static final String OUTPUT_DATE_PATTERN="yyyy-MM-dd HH:mm:ss";
	protected static final String POSTGRES_OUTPUT_DATE_PATTERN="YYYY-MM-DD HH24:MI:SS";
	protected static final String IS_NULL=" is null";
	protected static final String IS_NOT_NULL=" is not null";
	protected static final String ALL="1=1";
	protected static final String NONE="1<>1";
	
	public boolean useAnd(){return m_useAnd;}
	
	public abstract String getSql(String field, String value);

	protected static List<String> getList(String value)
	{
		List<String> values=new ArrayList<String>();
		if (value==null)
			return values;
		StringHelper.escapeSql(value);
		return StringHelper.splitAsList(value,","); 
	}
	
	protected static Float getFloat(String value)
	{
		return MathHelper.parseFloat(value);
	}
	
	protected static Date getDate(String str)
	{
		return DateHelper.parse(str,INPUT_DATE_PATTERN);
	}
	
	protected static String getSql(Date date)
	{
		return "to_date('"+DateHelper.format(date,OUTPUT_DATE_PATTERN)+"','"+POSTGRES_OUTPUT_DATE_PATTERN+"')";
	}
}