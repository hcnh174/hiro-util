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

import com.google.common.collect.Lists;

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
	 
	 public static <T> ExtDirectStoreResponse<T> getResponse(Page<T> page)
	 {
		 return new ExtDirectStoreResponse<T>((int)page.getTotalElements(),page.getContent());
	 }
}