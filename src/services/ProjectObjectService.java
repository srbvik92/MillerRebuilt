package services;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.TransactionException;
import org.hibernate.criterion.Order;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import objects.HibernateUtil;
import projectObjects.ChangeOrder;
import projectObjects.Inspections;
import projectObjects.NewEquipment;
import projectObjects.Permits;
import projectObjects.ProjectObject;


/**
 * This class performs actions on the database, such as adding,
 * updating, and deleting objects in the database.

 *
 */
public class ProjectObjectService 
{	
	/**
	 * This class returns all objects of a certain type from the database.
	 * @param domain the type of object to be returned.
	 * @return a list of all objects of a specific type in the database.
	 */
	public static List<Object> getAll(String domain)
	{
		//Begin transaction
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		Transaction tx = session.beginTransaction();
		
		//Get all objects of type "domain"
        Query q = session.createQuery("from "+domain);
        @SuppressWarnings("unchecked")
		List<Object> list = q.list();
        tx.commit();
        
        return list;
	}
	
	/**
	 * This method returns all objects of a certain type from the database,
	 * and formats them as a JSON array. This method is to be used to send information
	 * to the front-end of this application.
	 * 
	 * @param domain the type of object to be returned.
	 * @return A JSON array string representing all of the objects.
	 */
	public static String getAllAsJsonString(String domain) throws NonUniqueObjectException
	{
        Gson gson = new GsonBuilder().setDateFormat("MM/dd/yyyy").create();
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try
		{
		tx = session.beginTransaction();
		}
		catch(TransactionException ex)
		{
			
			tx.commit();
			return "ERROR";
		}
		Class<?> c;
		
		try 
		{
			c = Class.forName("projectObjects."+domain);
			
			Criteria criteria = session.createCriteria(c);
			if (!domain.equals("Warehouse") && !domain.equals("Project") && !domain.equals("Inspections") && !domain.equals("Permits")  && !domain.equals("Equipment")&& !domain.equals("EquipmentStatus") && !domain.equals("Proposal"))
				criteria.addOrder(Order.asc("name"));
			else if(domain.equals("Inspections"))
			{
				criteria.addOrder(Order.asc("id"));
			}
			else if(domain.equals("Proposal"))
			{
				criteria.addOrder(Order.asc("id"));
			}
			else if(domain.equals("Permits"))
			{
				criteria.addOrder(Order.asc("id"));
			}
			
			else if(domain.equals("Equipment"))
			{
				criteria.addOrder(Order.asc("id"));
			}
			
			else if(domain.equals("EquipmentStatus"))
			{
				criteria.addOrder(Order.asc("id"));
			}
			
			else if (domain.equals("Warehouse"))
			{
				criteria.createAlias("city", "c");
				criteria.addOrder(Order.asc("c.name"));
			}
			
	        List<?> list = criteria.list();
	        
	        tx.commit();

	        return gson.toJson(list);
		} 
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	public static String getID(String domain) throws NonUniqueObjectException
	{
        Gson gson = new GsonBuilder().setDateFormat("MM/dd/yyyy").create();
		Session session = HibernateUtil.getSession();
		Transaction tx = session.beginTransaction();
		Class<?> c;
		try 
		{
			c = Class.forName("projectObjects."+domain);
			
			Criteria criteria = session.createCriteria(c);
			if (!domain.equals("Warehouse") && !domain.equals("Project") && !domain.equals("Inspections") && !domain.equals("Permits") && !domain.equals("Equipment") && !domain.equals("EquipmentStatus"))
				criteria.addOrder(Order.asc("name"));
			else if(domain.equals("Inspections"))
			{
				criteria.addOrder(Order.asc("id"));
			}
			else if(domain.equals("Equipment"))
			{
				criteria.addOrder(Order.asc("id"));
			}
			else if(domain.equals("EquipmentStatus"))
			{
				criteria.addOrder(Order.asc("id"));
			}
			

	        List<?> list = criteria.list();
	        tx.commit();

	        return gson.toJson(list);
		} 
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return "";
	}	

	
	
	
	/**
	 * This method returns a single object of a certain type
	 * @param id the id of the object
	 * @param domain the type of object
	 * @return an object in the database
	 * @throws ClassNotFoundException
	 */
	public static Object get(Long id, String domain) throws ClassNotFoundException
	{
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		
		Transaction tx = session.beginTransaction();
		
		//Get the Class from parsing the "domain" string.
		Class<?> c = Class.forName("projectObjects."+domain);
		
		//Get object from database that matches the id
		Object o = session.get(c, id);
		tx.commit();
		return o;
	}
	
	/**
	 * This method gets a particular object of a certain type from the database,
	 * and then formats it as a JSON string.
	 * @param id the id of the object
	 * @param domain the type of the object.
	 * @return A JSON string representing the object.
	 * @throws ClassNotFoundException
	 */
	public static String getAsJSON(Long id, String domain) throws ClassNotFoundException
	{
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		Transaction tx = session.beginTransaction();

		Object o;	
		Gson gson= new GsonBuilder().setDateFormat("MM/dd/yyyy").create();
		Class<?> c = Class.forName("projectObjects."+domain);
		
		//When convering dates to JSON format, don't use units smaller than days
		o = session.get(c, id);
		if(o instanceof projectObjects.Project)
			checkNullRows((projectObjects.Project)o);

		tx.commit();
	
		return gson.toJson(o);
	}
	
	/**
	 * this method is vital for any new fields that are added to the database
	 * This method will find any objects within a project that are null (weren't initialized when created)
	 * and then initialize them which gives them a ID which is needed to access any linked object's fields. 
	 * @param o the Project which needs to be checked
	 */
	private static void checkNullRows(projectObjects.Project o) {
		if(o.getPermits() == null)
		{
			o.setPermits(new Permits());
		}	
		if(o.getInspections() == null)
		{
			o.setInspections(new Inspections());
		}
		if(o.getChangeOrders() == null)
		{
			o.setChangeOrders(new HashSet<ChangeOrder>());
		}
		if(o.getProjEquipment() == null)
		{
			o.setProjEquipment(new HashSet<NewEquipment>());
		}
	}

	/**
	 * This method deletes an object from the database.
	 * @param id the id of the object to be deleted
	 * @param domain the type of object
	 * @return a confirmation that the object has been deleted.
	 * @throws ClassNotFoundException
	 */
	public static String delete(Long id, String domain) throws ClassNotFoundException
	{
		String success = "NOT_DELETED";
		Session session = HibernateUtil.getSession();
		Transaction tx = session.beginTransaction();
		Class<?> c = Class.forName("projectObjects."+domain);
		
		//Attempt to delete the object
		try
		{
			ProjectObject oldObject2 = (ProjectObject) session.get(c, id);

		   // Object o = session.get(c, id); 
		    session.delete(oldObject2); 
		    System.out.println("hi");
		    tx.commit();
		    System.out.println("hello");
		    success = "PROJECT_DELETED";
		}
		catch (Exception e) 
		{
			if (tx!=null) tx.rollback();
			System.out.println("NOTHING WENT WRONG - kind of");
		}
		
		return success;
	}
	
	/**
	 * This method adds an object to the database.
	 * @param domain the type of object that is to be added
	 * @param o the object to be added
	 * @return the id of the transaction
	 */
	public static long addObject(String domain, Object o)
	{
		System.out.println("add Project");
		Session session = HibernateUtil.getSession();
		Transaction tx = session.beginTransaction();
		//session.save(o);
		
		Long txID = (Long)session.save(o);
        System.out.println("ID OF TRANSACTION: " + txID);
        tx.commit();
        //return (long) txID;
	    return txID ;
	}
	
	
	/**
	 * TODO: I'm pretty sure this is awful garbage code that we don't need. See ProjectService.editExistingProject()
	 * 
	 * This method replaces an object in the database with a new object. This simpler and more efficient than
	 * getting an object from a database, changing specific fields, and updating the database.
	 * 
	 * @param domain the type of object that is going to be edited
	 * @param id the id of the object to be edited
	 * @param newObject the object that is to replace the object in the database.
	 * @throws ClassNotFoundException
	 */	
	public static long editObject(String domain, Long id, ProjectObject newObject,  int i2) throws ClassNotFoundException, NonUniqueObjectException
	{
		
		//Get session and start transaction
		Session session = HibernateUtil.getSession();
		Transaction tx = session.beginTransaction();
				
		//Get object from database
		Class<?> c = Class.forName("projectObjects."+domain);
		
		ProjectObject oldObject2 = (ProjectObject) session.get(c, id);
		
		//Copy fields of new object into old object, but keep the id
		newObject.setId(id);
		System.out.println(newObject + " " + oldObject2);
		copyFieldByField(newObject, oldObject2, i2);
				
		session.saveOrUpdate(oldObject2);
		tx.commit();

		return id;
	}
	
	/**
	 * TODO: This is a really hacky way to fix a problem with sets in projects
	 * @param session
	 */
	public static void deleteNullSetObjects() 
	{
		Session session = HibernateUtil.getSession();
		Transaction tx = session.beginTransaction();

		Query deleteChangeOrders = session.createSQLQuery("delete from changeorder where changeOrders_id is NULL");
		deleteChangeOrders.executeUpdate();
		Query deleteEquipment = session.createSQLQuery("delete from newequipment where projEquipment_id is NULL");
		deleteEquipment.executeUpdate();
		
		tx.commit();
		
	}

	/*public static void editSetFromProject(String domain, Long id, ProjectObject o) throws ClassNotFoundException, NonUniqueObjectException
	{
		
		System.out.println("add to set");
		Session session = HibernateUtil.getSession();
		Transaction tx = session.beginTransaction();
		
		session.saveOrUpdate(o);
		//long txID = Long.parseLong((String)session.save(o));
       // System.out.println("ID OF TRANSACTION: " + txID);
        tx.commit();
        //return (long) txID;
	}*/
	
	/**
	 * This method copies the fields of the src object into the fields of the dest object,
	 * no matter what kind of objects they are (They must be the same kind of object, though).
	 * This method is a wrapper method so that the public does not need to make the
	 * src.getClass call to give the ACTUAL copyFields a third argument
	 * 
	 * Source: http://vyazelenko.com/2013/10/29/copy-object-in-java-performance-comparison/
	 * 
	 * @param src The object we are copying fields from
	 * @param dest The object we are copying fields to.
	 */
	public static void copyFieldByField(Object src, Object dest, int i2) 
	{
		copyFields(src, dest, src.getClass(), i2);
	}
		
	/**
	 * This method copies the fields of the src object into the fields of the dest object,
	 * no matter what kind of objects they are (They must be the same kind of object, though).
	 * 
	 * Source: http://vyazelenko.com/2013/10/29/copy-object-in-java-performance-comparison/
	 * 
	 * @param src The object we are copying fields from
	 * @param dest The object we are copying fields to.
	 * @param klass The Class object from the src object
	 */
	private static void copyFields(Object src, Object dest, Class<?> klass, int i2) 
	{
		//Copy the fields from one object to another
		Field[] fields = klass.getDeclaredFields();
		for (Field f : fields) 
		{
			/*
			 * I Don't know what this does, but it works...
			 */
			if((i2!=1 || !f.toString().contains("Inspections")) && 
			   (i2!=1 || !f.toString().contains("Closeout")) && 
			   (i2!=1 || !f.toString().contains("Permits")))
			//&& (i2!=1 || !f.toString().contains("Equipment")))
			{
				if(i2!=2 || !f.toString().contains("SalvageV"))
				{
					System.out.println("class: " + " " + src.getClass() +" Field: "  + f);
					f.setAccessible(true);
					copyFieldValue(src, dest, f);
				}
			}
		}
		 
		//Copy any fields that are also in the super class.
		klass = klass.getSuperclass();
		if (klass != null) 
		{
			copyFields(src, dest, klass, i2);
		}
	}
		 
	/**
	 * This method copies data from one field of object "src" to the same field of object "dest"
	 * 
	 * Source: http://vyazelenko.com/2013/10/29/copy-object-in-java-performance-comparison/
	 * 
	 * @param src the object we are copying the data from 
	 * @param dest the object we are copying the data to
	 * @param f the field of the object we are copying data from and to
	 */
	private static void copyFieldValue(Object src, Object dest, Field f) 
	{
		try 
		{
			//if src is of the Inspection object slip
			Object value = f.get(src);
			System.out.println(f);
			f.set(dest, value);
			
				
		} 
		catch (ReflectiveOperationException e) 
		{
			throw new RuntimeException(e);
		}
	}
}
