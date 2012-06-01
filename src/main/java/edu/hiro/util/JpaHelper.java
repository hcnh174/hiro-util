package edu.hiro.util;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.repository.JpaRepository;

public final class JpaHelper
{	
	private static final String[] ignore={"id","createdBy","createdDate","lastModifiedBy","lastModifiedDate"};

	public static <T,PK extends Serializable> T add(JpaRepository<T,PK> repository, T item)
	{
		return repository.save(item);
	}
	
	public static <T extends AbstractPersistable<PK>,PK extends Serializable> T save(JpaRepository<T,PK> repository, T item)
	{
		PK id=(PK)((AbstractPersistable<PK>)item).getId();
		System.out.println("save: id="+id+" class="+item.getClass().getName());
		if (id==null)
			return repository.save(item);
		T entity=repository.findOne(id);
		if (entity==null)
			throw new CException("cannot find entity with id "+id);
		BeanHelper beanhelper=new BeanHelper();
		beanhelper.copyProperties(entity,item,ignore);
		return repository.save(entity);
	}
	
	public static <T,PK extends Serializable> List<T> findAll(JpaRepository<T,PK> repository)
	{
		return repository.findAll();
	}
	
	public static <T extends AbstractPersistable<PK>,PK extends Serializable> List<T> save(JpaRepository<T,PK> repository, List<T> items)
	{
		BeanHelper beanhelper=new BeanHelper();
		for (T item : items)
		{
			PK id=(PK)((AbstractPersistable<PK>)item).getId();
			T entity=repository.findOne(id);
			if (entity==null)
				throw new CException("cannot find entity with id "+id);
			beanhelper.copyProperties(entity,item,ignore);
			repository.save(entity);
		}
		return items;
	}
	
	public static <T extends AbstractPersistable<PK>,PK extends Serializable> void delete(JpaRepository<T,PK> repository, List<T> items)
	{
		for (T item : items)
		{
			repository.delete(item);
		}
	}
//	
//	public static <T,PK extends Serializable> List<PK> delete(JpaRepository<T,PK> repository, List<PK> ids)
//	{
//		for (PK id : ids)
//		{
//			repository.delete(id);
//		}
//		return ids;
//	}
}
