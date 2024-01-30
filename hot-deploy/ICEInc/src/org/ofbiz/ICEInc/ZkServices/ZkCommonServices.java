package org.ofbiz.ICEInc.ZkServices;

import java.lang.annotation.Documented;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import org.apache.jasper.tagplugins.jstl.core.ForEach;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;

public class ZkCommonServices {

	public GenericDelegator delegator ;
	public String strError;
	
	public ZkCommonServices() {
		delegator = GenericDelegator.getGenericDelegator("default");
		emptyerror();
	}
	/*
	 * 
	 */

	/**
	 * @param tblname Name of entity 
	 * @param mptblvalues Parameters to be passed to Update enity.
	 * @return on successfull updateion else check in "strError"
	 */
	public boolean createTable(String tblname,Map mptblvalues)
	{
		emptyerror();
		
		try 
		{		 
			GenericValue strRow=  delegator.makeValidValue(tblname, mptblvalues);
			strRow.create();
			
			return true;
		}
		catch (Exception e) 
		{
			strError=e.getMessage();
			return false;
		}
		
	}
	/* 
	 * Update the table
	 */
	public boolean updateTable(String tblname,Map mptblvalues,Map varId)
	{
		emptyerror();
		
		try 
		{		 
			GenericValue strRow=  delegator.findByPrimaryKey(tblname, varId);
			
			Set ks=mptblvalues.keySet();
			String[] temp2=new String[1];
			
			String []keys2=(String[])ks.toArray(temp2);
			
			for(int i=0;i<keys2.length;i++)
			{
				String strname=(String)keys2[i];
				
				String strvalue=(String)mptblvalues.get(keys2[i]);
				strRow.put(strname, strvalue);				
			}
			strRow.store();
						
			return true;
		}
		catch (Exception e) 
		{
			strError=e.getMessage();
			return false;
		}
		
	}
	//Gets the next sequential ID 
	public long CustSequenceId(String tblname,String fldname)
	{
		emptyerror();
		try 
		{
		List <GenericValue> genericValue = delegator.findByCondition(tblname, null, UtilMisc.toList(fldname) , null);
		long abc=0;
		for (GenericValue genericValue2 : genericValue) {
			
			abc = genericValue2.getLong(fldname);
			
		}
		//BigDecimal MaxVal =  genericValue.get(0).getBigDecimal(fldname);
		abc++;
		
		//int MaxVal = genericValue getInteger(0);
		 return abc;
		}
		catch (Exception e) 
		{
			strError=e.getMessage();
			return 0;
		}
		
	}
	/*
	 * return true if thers is duplicate
	 */
	
	public boolean CustCheckDuplicate(String tblname,String fldname,String fldvalue)
	{
		try 
		{
			emptyerror();
			EntityExpr expr = new EntityExpr(fldname, EntityOperator.EQUALS,
					fldvalue);
			List<GenericValue> retvalue2=null;
			int rowcount=0;
			retvalue2 = delegator.findByCondition(tblname, expr, null, null);
			rowcount= retvalue2.size();
			/*for (GenericValue genericValue : retvalue2) {
			   rowcount ++;
			}*/
			return rowcount>0?true:false;
		}
		catch (Exception e) 
		{
			strError=e.getMessage();
			return true;
		}
	}
	/*
	 * Gets all the rows of that table.
	 * params accepts a tablename  ie entity name.
	 */
	public List<GenericValue> getAllDataTable(String tablename)
	{
		
		List<GenericValue> retvalue2=null;
		try 
		{
			retvalue2=delegator.findAll(tablename);
			
		}
		catch (Exception e) 
		{
			
			strError="getAllDataTable()"+e.getMessage();
			
			
		}
		return retvalue2;
	}
	/*
	 * inorder to empty this string which holds the error that has occured.
	 */
	private void emptyerror()
	{
		
		strError="";
	}
	
}
	
	
	

