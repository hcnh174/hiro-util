package edu.hiro.util;

import java.util.ArrayList;
import java.util.List;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;

public class RserveHelper
{
	private RserveHelper() {}
	
	/*
	public static abstract class RTemplate
	{
		protected Object response=null;
		
		public void execute()
		{
			RConnection connection=null;
			try
			{
				connection=new RConnection();
				execute(connection);
				System.out.println("response: "+response);
			}
			catch(Exception e)
			{
				throw new CException(e);
			}
			finally
			{
				if (connection!=null)
				{
					try
					{
						connection.close();
					}
					catch(Exception e)
					{
						System.out.println("problem closing RConnection");
						e.printStackTrace();
					}
				}
			}
		}
		
		public Object getResponse(){return response;}
		
		public abstract void execute(RConnection connection) throws Exception;
		
		protected String prepareCommands(String str)
		{
			return prepareCommands(StringHelper.splitAsList(str,"\n"));
		}
		
		protected String prepareCommands(List<String> list)
		{
			List<String> commands=new ArrayList<String>();
			for (String command : list)
			{
				command=command.trim();
				commands.add(command);
			}
			String str=StringHelper.join(commands,"; ");
			System.out.println("commands="+str);
			return str;
		}
		
		protected void voidEval(RConnection con, String command) throws Exception
		{
			System.out.println(command);
			con.voidEval(command);
		}
		
		protected REXP eval(RConnection con, String command) throws Exception
		{
			System.out.println(command);
			return con.eval(command);
		}
		
		protected String[][] asDataFrame(REXP expr) throws REXPMismatchException
		{
			RList list=expr.asList();
			//DataFrame dataframe=new DataFrame();
			int cols = list.size();
			int rows = list.at(0).length();
			String[][] s = new String[cols][];
			for (int i=0; i<cols; i++)
			{
				s[i]=list.at(i).asStrings();
			}
			System.out.println(StringHelper.toString(s));
			return s;
		}
	}
	*/
	
	/*
	public String eval(final String str)
	{
		RTemplate template=new RTemplate()
		{
			public void execute(RConnection connection) throws Exception
			{
				String commands=prepareCommands(str);
				REXP x=connection.eval(commands);
				m_response=x.asString();
			}
		};
		template.execute();
		return (String)template.getResponse();
	}
	
	public void eval(final List<String> str)
	{
		RTemplate template=new RTemplate()
		{
			public void execute(RConnection connection) throws Exception
			{
				String commands=prepareCommands(str);
				connection.eval(commands);
			}
		};
		template.execute();
	}
	
	public void voidEval(final String str)
	{
		RTemplate template=new RTemplate()
		{
			public void execute(RConnection connection) throws Exception
			{
				String commands=prepareCommands(str);
				connection.eval(commands);
			}
		};
		template.execute();
	}
	
	@SuppressWarnings("unchecked")
	public List<Double> evalList(final String str)
	{
		RTemplate template=new RTemplate()
		{
			public void execute(RConnection connection) throws Exception
			{
				String commands=prepareCommands(str);
				double[] values=connection.eval(commands).asDoubles();
				ArrayList response=new ArrayList<Double>();
				for (double value : values)
				{
					response.add(value);
				}
				m_response=response;
			}
		};
		template.execute();
		return (List<Double>)template.getResponse();
	}

//	public DataFrame evalTable(final String str)
//	{
//		RTemplate template=new RTemplate()
//		{
//			public void execute(RConnection connection) throws Exception
//			{
//				String commands=prepareCommands(str);
//				RList list=connection.eval(commands).asList();
//				DataFrame table=new DataFrame();
//				for (Object key : list.keySet())
//				{
//					System.out.println("key="+key);
//					REXP exp=(REXP)list.get(key);
//					//System.out.println("exp="+exp.asDoubles());
//					table.add(table.new Column((String)key,exp.asDoubles()));
//				}
//				m_response=table;
//			}
//		};
//		template.execute();
//		return (DataFrame)template.getResponse();
//	}
	*/	
}