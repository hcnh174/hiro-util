package edu.hiro.util;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BindingResult;

import ch.ralscha.extdirectspring.bean.ExtDirectResponse;
import ch.ralscha.extdirectspring.bean.ExtDirectResponseBuilder;
import ch.ralscha.extdirectspring.bean.ExtDirectStoreReadRequest;
import ch.ralscha.extdirectspring.bean.ExtDirectStoreResponse;
import ch.ralscha.extdirectspring.bean.SortDirection;
import ch.ralscha.extdirectspring.bean.SortInfo;
import ch.ralscha.extdirectspring.filter.DateFilter;
import ch.ralscha.extdirectspring.filter.Filter;
import ch.ralscha.extdirectspring.filter.ListFilter;
import ch.ralscha.extdirectspring.filter.NumericFilter;
import ch.ralscha.extdirectspring.filter.StringFilter;

import com.google.common.collect.Lists;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.path.DatePath;
import com.mysema.query.types.path.DateTimePath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.PathBuilder;
import com.mysema.query.types.path.StringPath;

//BooleanBuilder filter=ExtDirectHelper.getFilter(request, new PathBuilder<Patient>(Patient.class,"patient"), new Patient());
public class ExtDirectHelper
{
	public static ExtDirectResponse getResponse(HttpServletRequest request, BindingResult result)
	{
		ExtDirectResponseBuilder builder = new ExtDirectResponseBuilder(request);
		builder.addErrors(result);
		return builder.build();
	}
	
	public static Pageable getPageable(ExtDirectStoreReadRequest request)
	{
		System.out.println("received request: "+request.toString());
		int page=request.getPage()-1;
		int limit=request.getLimit();
		if (request.getSorters().isEmpty())
			return new PageRequest(page,limit);
		else return new PageRequest(page,limit,getSort(request));
	}

	private static Sort getSort(ExtDirectStoreReadRequest request)
	{
		List<Sort.Order> sorters=Lists.newArrayList();
		for (SortInfo sortinfo : request.getSorters())
		{
			String field=sortinfo.getProperty();
			Sort.Direction dir=sortinfo.getDirection()==SortDirection.DESCENDING ? Sort.Direction.DESC : Sort.Direction.ASC;
			sorters.add(new Sort.Order(dir, field));
		}
		Sort sort=new Sort(sorters);
		//System.out.println("sorts: "+sort.toString());
		return sort;
	}

	public static BooleanBuilder getFilter(ExtDirectStoreReadRequest request, PathBuilder<?> path)
	{
		System.out.println("************************************************");
		BooleanBuilder builder = new BooleanBuilder();
		List<Filter> filters=request.getFilters();
		System.out.println("filters.length="+request.getFilters().size());
		for (Filter filter : filters)
		{
			System.out.println("FILTER="+filter.toString()+": "+filter.getClass().getCanonicalName());
			if (filter instanceof StringFilter)
				handleFilter((StringFilter)filter,builder,path);
			else if (filter instanceof NumericFilter)
				handleFilter((NumericFilter)filter,builder,path);
			else if (filter instanceof DateFilter)
				handleFilter((DateFilter)filter,builder,path);
			else if (filter instanceof ListFilter)
				handleFilter((ListFilter)filter,builder,path);			
		}
		return builder;
	}
	
	public static void handleFilter(StringFilter filter, BooleanBuilder builder, PathBuilder<?> path)
	{
		System.out.println("StringFilter: "+filter.getField()+"="+filter.getValue());
		StringPath field=path.getString(filter.getField());
		builder.and(field.like(filter.getValue()));
	}
	
	public static void handleFilter(NumericFilter filter, BooleanBuilder builder, PathBuilder<?> path)
	{
		System.out.println("NumericFilter: "+filter.getField()+"="+filter.getValue());
		Integer value=(Integer)filter.getValue();
		NumberPath<Integer> field=path.getNumber(filter.getField(),Integer.class);
		switch(filter.getComparison())
		{
		case EQUAL:
			builder.and(field.eq(value));
			break;
		case GREATER_THAN:
			builder.and(field.gt(value));
			break;
		case LESS_THAN:
			builder.and(field.lt(value));
			break;
		}
	}

	public static void handleFilter(DateFilter filter, BooleanBuilder builder, PathBuilder<?> path)
	{
		DateTimePath<Date> field=path.getDateTime(filter.getField(),Date.class);
		handleFilter(filter,field,builder);
	}
	
	public static void handleFilter(DateFilter filter, DateTimePath<java.util.Date> field, BooleanBuilder builder)
	{
		System.out.println("DateFilter: "+filter.getField()+"="+filter.getValue());
		Date value=DateHelper.parse(filter.getValue(),DateHelper.YYYYMMDD_PATTERN);
		switch(filter.getComparison())
		{
		case EQUAL:
			builder.and(field.eq(value));
			break;
		case GREATER_THAN:
			builder.and(field.after(value));
			break;
		case LESS_THAN:
			builder.and(field.before(value));
			break;
		}
	}
	
	private static void handleFilter(ListFilter filter, BooleanBuilder builder, PathBuilder<?> path)
	{
		System.out.println("ListFilter: "+filter.getField()+"="+filter.getValue());
		if (isInteger(filter))
			handleIntegerFilter(filter,builder,path);
		else handleStringFilter(filter,builder,path);
	}
	
	private static void handleStringFilter(ListFilter filter, BooleanBuilder builder, PathBuilder<?> path)
	{
		System.out.println("  StringListFilter");
		StringPath field=path.getString(filter.getField());
		BooleanBuilder listitems = new BooleanBuilder();
		for (String value : filter.getValue())
		{
			listitems.or(field.eq(value));
		}
		builder.and(listitems);
	}
	
	private static void handleIntegerFilter(ListFilter filter, BooleanBuilder builder, PathBuilder<?> path)
	{
		System.out.println("  IntegerListFilter");
		NumberPath<Integer> field=path.getNumber(filter.getField(), Integer.class);
		BooleanBuilder listitems = new BooleanBuilder();
		for (Object value : filter.getValue())
		{
			System.out.println("value="+value.toString());
			listitems.or(field.eq(Integer.valueOf(value.toString())));	
		}
		builder.and(listitems);
	}
	
	private static boolean isInteger(ListFilter filter)
	{
		for (Object value : filter.getValue())
		{
			if (!MathHelper.isInteger(value.toString()))
				return false;
		}
		return true;
	}
	
	/*
	private static List<Filter> removeDuplicates(List<Filter> list)
	{
		List<Filter> filters=Lists.newArrayList();
		Set<String> fields=Sets.newHashSet();
		// go backwards and only count each field once
		for (int index=list.size()-1; index>=0; index--)
		{
			Filter filter=list.get(index);
			if (fields.contains(filter.getField()))
			{
				System.out.println("skipping field "+filter.getField());
				continue;
			}
			else
			{
				filters.add(filter);
				fields.add(filter.getField());
			}
		}
		return filters;
	}
	*/
	
	public static <T> ExtDirectStoreResponse<T> getResponse(Page<T> page)
	{
		return new ExtDirectStoreResponse<T>((int)page.getTotalElements(),page.getContent());
	}
	
	public static class ExtTreeNode
	{	
		@JsonProperty protected final boolean leaf;
		@JsonProperty protected String text;		
		@JsonProperty protected String iconCls;
		
		public ExtTreeNode()
		{
			this(true,"root");
		}
		
		public ExtTreeNode(boolean leaf)
		{
			this.leaf=leaf;
		}
		
		public ExtTreeNode(boolean leaf, String text)
		{
			this(leaf);
			this.text=text;
		}
		
		public boolean getLeaf(){return this.leaf;}

		public String getText(){return this.text;}
		public void setText(final String text){this.text=text;}

		public String getIconCls(){return this.iconCls;}
		public void setIconCls(final String iconCls){this.iconCls=iconCls;}
	}
	
	public static class ExtParentNode extends ExtTreeNode
	{
		@JsonProperty protected boolean expanded=true;
		@JsonProperty protected List<ExtTreeNode> children = Lists.newArrayList();
		
//		public ExtParentNode()
//		{
//			super(false);
//		}
//		
		public ExtParentNode(String text)
		{
			super(false,text);
		}
		
		public boolean getExpanded(){return this.expanded;}
		public void setExpanded(final boolean expanded){this.expanded=expanded;}

		public List<ExtTreeNode> getChildren(){return this.children;}
		public void setChildren(final List<ExtTreeNode> children){this.children=children;}
		
		public void add(ExtTreeNode node)
		{
			this.children.add(node);
		}
	}

	public static class ExtLeafNode extends ExtTreeNode
	{		
//		public ExtLeafNode()
//		{
//			super(true);
//		}
//		
		public ExtLeafNode(String text)
		{
			super(true,text);
		}
	}
}

/*
List<Filter> filters=Lists.newArrayList();
filters.add(new StringFilter("patientName","佐藤  花子"));
//filters.add(new DateFilter("birthdate","1975-03-09",Comparison.EQUAL));
filters.add(new StringFilter("sex","F"));
filters.add(new NumericFilter("mainHospitalNo",32725,Comparison.EQUAL));

PathBuilder<Patient> path=new PathBuilder<Patient>(Patient.class,"patient");
ExtDirectStoreReadRequest request=new ExtDirectStoreReadRequest();
request.setFilters(filters);
BooleanBuilder builder=ExtDirectHelper.getFilter(request,path);
Page<Patient> page=patientService.getPatients(builder,new PageRequest(1,10));
for (Patient patient : page.getContent())
{
	System.out.println("patient="+patient.toString());
}
*/
