package edu.hiro.util;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import ch.ralscha.extdirectspring.bean.ExtDirectStoreReadRequest;
import ch.ralscha.extdirectspring.bean.ExtDirectStoreResponse;
import ch.ralscha.extdirectspring.bean.SortDirection;
import ch.ralscha.extdirectspring.bean.SortInfo;
import ch.ralscha.extdirectspring.filter.DateFilter;
import ch.ralscha.extdirectspring.filter.Filter;
import ch.ralscha.extdirectspring.filter.NumericFilter;
import ch.ralscha.extdirectspring.filter.StringFilter;

import com.google.common.collect.Lists;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.path.PathBuilder;

public class ExtDirectHelper
{
	public static Pageable getPageable(ExtDirectStoreReadRequest request)
	{
		System.out.println("received request: "+request.toString()); 
		List<Sort.Order> sorters=Lists.newArrayList();
		for (SortInfo sortinfo : request.getSorters())
		{
			String field=sortinfo.getProperty();
			Sort.Direction dir=sortinfo.getDirection()==SortDirection.DESCENDING ? Sort.Direction.DESC : Sort.Direction.ASC;
			sorters.add(new Sort.Order(dir, field));
		}
		Sort sort=new Sort(sorters);
		//System.out.println("sorts: "+sort.toString());
		Pageable pageable=new PageRequest(request.getPage()-1,request.getLimit(), sort);
		return pageable;
	}

	//PathBuilder<Patient> path=new PathBuilder<Patient>(Patient.class,"patient");
	public static BooleanBuilder getFilter(ExtDirectStoreReadRequest request, PathBuilder<?> path )
	{
		System.out.println("creating bollean filter from request: "+request.toString());
		BooleanBuilder builder = new BooleanBuilder();
		for (Filter filter : request.getFilters())		
		{
			System.out.println("filter="+filter.toString());
			if (filter instanceof StringFilter)
			{
				StringFilter strfilter=(StringFilter)filter;
				builder.and(path.get(filter.getField()).eq(strfilter.getValue()));
			}
			else if (filter instanceof NumericFilter)
			{
				NumericFilter numfilter=(NumericFilter)filter;
				builder.and(path.get(filter.getField()).eq(numfilter.getValue()));
			}
			else if (filter instanceof DateFilter)
			{
				DateFilter datefilter=(DateFilter)filter;
				builder.and(path.get(filter.getField()).eq(datefilter.getValue()));
			}
		}
		return builder;
	}
	
	public static <T> ExtDirectStoreResponse<T> getResponse(Page<T> page)
	{
		return new ExtDirectStoreResponse<T>((int)page.getTotalElements(),page.getContent());
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
