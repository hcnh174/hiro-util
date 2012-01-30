package edu.hiro.util;

import java.util.List;

import org.neo4j.helpers.collection.IteratorUtil;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import com.google.common.collect.Lists;

public final class Neo4jHelper
{
	private Neo4jHelper(){}
	
	public static <T> List<T> asList(Iterable<T> iter)
	{
		return Lists.newArrayList(IteratorUtil.asCollection(iter));
	}
	
	public static void clearDatabase(Neo4jTemplate neo4jTemplate)
    {
		org.springframework.data.neo4j.support.node.Neo4jHelper.cleanDb(neo4jTemplate);
    }
}
	