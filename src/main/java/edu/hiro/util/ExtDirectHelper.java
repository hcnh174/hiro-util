package edu.hiro.util;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BindingResult;

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
import com.google.common.collect.Maps;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.path.DateTimePath;
import com.mysema.query.types.path.EnumPath;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.PathBuilder;
import com.mysema.query.types.path.StringPath;

//BooleanBuilder filter=ExtDirectHelper.getFilter(request, new PathBuilder<Patient>(Patient.class,"patient"), new Patient());
public class ExtDirectHelper
{	
	public static BooleanBuilder getFilter(ExtDirectStoreReadRequest request, Class<?> cls)
	{
		FilterHelper helper=new FilterHelper(cls);
		return helper.process(request);
	}
	
	public static class FilterHelper
	{
		private PathBuilder<?> path;
		private Object obj;
		private BeanWrapper wrapper;
		private BooleanBuilder builder=new BooleanBuilder();
		private String dateFormat=DateHelper.YYYYMMDD_PATTERN;
		
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public FilterHelper(Class<?> cls)//PathBuilder path
		{
			//this.cls=cls;
			this.obj=BeanHelper.newInstance(cls);
			this.wrapper=PropertyAccessorFactory.forBeanPropertyAccess(obj);
			this.path=new PathBuilder(cls,cls.getSimpleName().toLowerCase());
		}
		
		private BooleanBuilder process(ExtDirectStoreReadRequest request)
		{
			System.out.println("************************************************");
			List<Filter> filters=request.getFilters();
			System.out.println("filters.length="+request.getFilters().size());
			for (Filter filter : filters)
			{
				System.out.println("FILTER="+filter.toString()+": "+filter.getClass().getCanonicalName());
				Class<?> type=getType(filter.getField());
				if (filter instanceof StringFilter)
					handleFilter((StringFilter)filter,type);
				else if (filter instanceof NumericFilter)
					handleFilter((NumericFilter)filter,type);
				else if (filter instanceof DateFilter)
					handleFilter((DateFilter)filter,type);
				else if (filter instanceof ListFilter)
					handleFilter((ListFilter)filter,type);			
			}
			return builder;
		}
		
		private void handleFilter(StringFilter filter, Class<?> type)
		{
			System.out.println("StringFilter: "+filter.getField()+"="+filter.getValue());
			if (type.isEnum())
				handleEnumFilter(filter,type);
			else handleStringFilter(filter,type);
		}
		
		private void handleEnumFilter(StringFilter filter, Class<?> type)
		{
			//EnumPath field=new EnumPath(type, filter.getField());
			for (Object cnstnt : Arrays.asList(type.getEnumConstants()))
			{
				Enum constant=(Enum)cnstnt;
				if (filter.getField().equals(constant.name()))
				{
					EnumPath field=path.getEnum(filter.getField(),constant.getDeclaringClass());
					builder.and(field.eq(constant));
				}
			}
		}
		
//		protected <E extends Enum<E>> void  handleEnumFilter(StringFilter filter, Class<E> type)
//		{
//			EnumPath<E> field=path.getEnum(filter.getField(),type);
//			Enum etype=(Enum)type;
//			builder.and(field.eq(filter.getValue()));
//			//return add(new EnumPath<A>(type, forProperty(property)));
//		}
	
		private void handleStringFilter(StringFilter filter, Class<?> type)
		{
			StringPath field=path.getString(filter.getField());
			builder.and(field.like(filter.getValue()));
		}		
		
		private void handleFilter(NumericFilter filter, Class<?> type)
		{
			System.out.println("NumericFilter: "+filter.getField()+"="+filter.getValue());
			String property=filter.getField();			
			System.out.println("NumericFilter type=: "+type);
			if (isInteger(type))
				handleIntegerFilter(filter);
			else if (isDouble(type))
				handleDoubleFilter(filter);
			else throw new CException("number not assignable from Integer or Double: "+property);
		}
		
		private void handleIntegerFilter(NumericFilter filter)
		{	
			System.out.println("treating as integer");
			NumberPath<Integer> field=path.getNumber(filter.getField(),Integer.class);
			Integer value=(Integer)filter.getValue();
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
		
		private void handleDoubleFilter(NumericFilter filter)
		{
			System.out.println("treating as double");
			NumberPath<Double> field=path.getNumber(filter.getField(),Double.class);
			Double value=(Double)filter.getValue();
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
	
		private void handleFilter(DateFilter filter, Class<?> type)
		{			
			DateTimePath<Date> field=path.getDateTime(filter.getField(),Date.class);
			System.out.println("DateFilter: "+filter.getField()+"="+filter.getValue());
			Date value=DateHelper.parse(filter.getValue(),dateFormat);
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
		
		private void handleFilter(ListFilter filter, Class<?> type)
		{
			System.out.println("ListFilter: "+filter.getField()+"="+filter.getValue());
			if (isInteger(type))
				handleIntegerListFilter(filter);
			else handleStringListFilter(filter);
		}
		
		private void handleStringListFilter(ListFilter filter)
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
		
		private void handleIntegerListFilter(ListFilter filter)
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
		
		private boolean isInteger(Class<?> type)
		{
			return type.isAssignableFrom(Integer.class);
		}
		
		private boolean isDouble(Class<?> type)
		{
			return type.isAssignableFrom(Double.class);
		}
		
		
		private Class<?> getType(String property)
		{
			Class<?> type=wrapper.getPropertyType(property);
			if (type==null)
				throw new CException("cannot find property: "+property);
			return type;
		}
	}
	
	/*
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
	*/
	
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
		
		/*
		public ExtTreeNode()
		{
			this(true,"root");
		}
		*/
		
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
		
		public boolean isEmpty()
		{
			return this.children.isEmpty();
		}
		
		public int size()
		{
			return this.children.size();
		}
	}

	public static class ExtLeafNode extends ExtTreeNode
	{		
		public ExtLeafNode(String text)
		{
			super(true,text);
		}
	}
	
	public static class ExtRootNode extends ExtParentNode
	{
		public ExtRootNode()
		{
			super("text");
			this.expanded=true;
		}
	}
	
	public static void getResponse(HttpServletRequest request, HttpServletResponse response)
	{
		ExtDirectResponseBuilder.create(request, response).buildAndWrite();
	}
	
	public static void getResponse(HttpServletRequest request, HttpServletResponse response, BindingResult result)
	{
		ExtDirectResponseBuilder.create(request, response).addErrors(result).buildAndWrite();
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


