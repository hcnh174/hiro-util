package edu.hiro.util;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

public class AntlrHelper
{
	public static ANTLRStringStream getStringStream(String str)
	{
		return new ANTLRStringStream(str);
	}
	
	public static CommonTokenStream getTokenStream(Lexer lexer)
	{
		return new CommonTokenStream(lexer);
	}
	
	///////////////////////////////////////////////////////////
	
	public static boolean showTokens(Lexer lexer)
	{
		try
		{
			while(true)
			{
				Token token=lexer.nextToken();
				if (token.getType()==Token.EOF)
					break;
				System.out.println("Token: "+token.getText()+", type="+token.getType());
			}			
		}
		catch(Exception e)
		{
			System.err.println(e.getClass().getName()+": "+e.toString());
		}
		return true;
	}
	
	/////////////////////////////////////////////////
	
	public static boolean showTree(CommonTree tree)
	{
		System.out.println(tree.toStringTree());
		showNode(tree);
		return true;
	}
	
	private static void showNode(CommonTree node)
	{
		if (node==null)
			return;
		System.out.println("node text="+node.getText()+", type="+node.getType());
		if (node.getChildCount()==0)
			return;
		//for (Object child : node.getgetChildren())
		for (int index=0; index<node.getChildCount(); index++)
		{
			Tree child=node.getChild(index);
			showNode((CommonTree)child);
		}		
	}
	
	//////////////////////////////////////////////////////////
	
	public static CommonTree getChild(CommonTree node, int index)
	{
		return (CommonTree)node.getChild(index);
	}
	
	public static String getChildText(CommonTree node, int index)
	{
		return getText(getChild(node,index));
	}
	
	public static String getText(CommonTree node)
	{
		return node.getText();
	}
}