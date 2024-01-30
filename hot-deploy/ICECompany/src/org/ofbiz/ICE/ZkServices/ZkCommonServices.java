package org.ofbiz.ICE.ZkServices;

import java.lang.annotation.Documented;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ofbiz.ICE.zkValidation.*;
//import org.apache.jasper.tagplugins.jstl.core.ForEach;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;

import org.zkoss.zul.ListModel;
import org.zkoss.zul.SimpleListModel;
import bussinessapi.*;


public class ZkCommonServices {

	public GenericDelegator delegator ;
	public String strError;
	
	public ZkCommonServices() {
		delegator = GenericDelegator.getGenericDelegator("default");
		emptyerror();
	}
	public boolean verifydate(Date givendate,String sq_Ref)
	{		
		EntityExpr xxp = new EntityExpr("Fy_AcYear",
				 EntityOperator.EQUALS,GetACYear());
		
		EntityExpr xxp2 = new EntityExpr("Sq_Ref",
				 EntityOperator.EQUALS,sq_Ref);
				
		 List a= UtilMisc.toList(xxp,xxp2);
		 
		 EntityConditionList condList = new EntityConditionList(a,EntityOperator.AND);
		 
		List<GenericValue> StrRow = ReturnMultipleCondition("tbl_Sequence_AcYear", condList);
		GenericValue genericValue5 = StrRow.get(0);
		
		zkCommonValidations objCommVal = new zkCommonValidations();
		
		return objCommVal.validateToDateFromDate(genericValue5.getDate("todate"), genericValue5.getDate("fromdate"), givendate);
	}
	
	/**
	 * @param tblname Name of entity 
	 * @param mptblvalues Parameters to be passed to Update enity.
	 * @return on successfull updateion else check in "strError"
	 */
	
	public List<GenericValue> ReturnMultipleCondition(String tblname,EntityConditionList exp)
	{
		List<GenericValue> abc=null;
		try{
		GenericDelegator delegator = GenericDelegator.getGenericDelegator("default");
		
		//delegator.findByCondition(entityName, whereEntityCondition, havingEntityCondition, fieldsToSelect, orderBy, findOptions)
		//EntityExpr expr = new EntityExpr(CondfldName, EntityOperator.EQUALS,CondfldValue);
		abc = delegator.findByCondition(tblname,exp, null, null);
		return abc;
		}
		catch (Exception e) 
		{
			strError=e.getMessage();
			return abc;
		}
	}
	public List<GenericValue> ReturnMultipleCondition(String tblname,EntityConditionList exp,String OrderBy)
	{
		List<GenericValue> abc=null;
		try{
		GenericDelegator delegator = GenericDelegator.getGenericDelegator("default");
		
		//delegator.findByCondition(entityName, whereEntityCondition, havingEntityCondition, fieldsToSelect, orderBy, findOptions)
		//EntityExpr expr = new EntityExpr(CondfldName, EntityOperator.EQUALS,CondfldValue);
		abc = delegator.findByCondition(tblname,exp, null, UtilMisc.toList(OrderBy));
		return abc;
		}
		catch (Exception e) 
		{
			strError=e.getMessage();
			return abc;
		}
	}
	public List<GenericValue> ReturnSingleCondition(String tblname,EntityExpr exp)
	{
		List<GenericValue> abc=null;
		try{
		GenericDelegator delegator = GenericDelegator.getGenericDelegator("default");
		
		//EntityCondition kk=EntityCondition.makeConditionWhere("SumPayment>0");
		//EntityExpr expr = new EntityExpr(CondfldName, EntityOperator.EQUALS,CondfldValue);
		abc = delegator.findByCondition(tblname,exp,null,null);
		return abc;
		}
		catch (Exception e) 
		{
			strError=e.getMessage();
			return abc;
		}
	}
	
	public List<GenericValue> ReturnRowGen(String tblname,String CondfldName,String CondfldValue)
	{
		List<GenericValue> abc=null;
		try{
		GenericDelegator delegator = GenericDelegator.getGenericDelegator("default");
		
		
		EntityExpr expr = new EntityExpr(CondfldName, EntityOperator.EQUALS,CondfldValue);
		abc = delegator.findByCondition(tblname,expr, null, null);
		return abc;
		}
		catch (Exception e) 
		{
			strError=e.getMessage();
			return abc;
		}
	}
	public List<GenericValue> ReturnRowGen(String tblname,String CondfldName,String CondfldValue,String OrderBy)
	{
		List<GenericValue> abc=null;
		try{
		GenericDelegator delegator = GenericDelegator.getGenericDelegator("default");
		
		
		EntityExpr expr = new EntityExpr(CondfldName, EntityOperator.EQUALS,CondfldValue);
		abc = delegator.findByCondition(tblname,expr, null, UtilMisc.toList(OrderBy));
		return abc;
		}
		catch (Exception e) 
		{
			strError=e.getMessage();
			return abc;
		}
	}
	public boolean DeleteConditionalRecords(String tblname,String condFldname,String conFldValue)
	{
		GenericDelegator delegator = GenericDelegator.getGenericDelegator("default");
		try{
						                                    
			EntityExpr erpr = new EntityExpr(condFldname, EntityOperator.EQUALS  , conFldValue);
			List <GenericValue> StrDel = delegator.findByCondition(tblname, erpr, null, null); //.findByPrimaryKey(tblname,StrId);
			int size2 = StrDel.size();
			for(int i =0 ; i<size2;i++ )
			{
				GenericValue d =  StrDel.get(i);
				d.remove();
			}			
				
			/*for (GenericValue genericValue6 : StrDel) {
				genericValue6.remove();
			}*/
			//StrDel.remove();
			return true;
		}
		catch (Exception e) 
		{
			strError=e.getMessage();
			return false;
		}
	}
	public ListModel PopulateListBox(String tblname,String fldname, String Confldname,String Confldvalue)
	{
		ListModel model=null;
		try 
		{
			GenericDelegator delegator = GenericDelegator.getGenericDelegator("default");
			List<GenericValue> abc;
			
			HashMap content = new HashMap();
			emptyerror();
			if(Confldname.equals("")||Confldvalue.equals(""))
			{
				abc = delegator.findAll(tblname);
				
			}
			else
			{
				EntityExpr expr = new EntityExpr(Confldname, EntityOperator.EQUALS,Confldvalue);
				abc = delegator.findByCondition(tblname,expr, null, null);				
			}
			String temp3[]=new String[abc.size()+1];
			
			temp3[0]="--Select--";
			for(int i=1;i<abc.size()+1;i++)
			{
				GenericValue temp2=(GenericValue) abc.get(i-1);
				temp3[i]=temp2.getString(fldname);				
			}
			
			model=new SimpleListModel(temp3);
			return model;
		}
		catch (Exception e) 
		{
			strError=e.getMessage();
			return model;
		}
	}
	/*
	 * GET ID FROM THE NAME FIELD FROM A SPECIFIED ENTITY
	 * tblname = Table from which value to be extracted
	 * fldname = Name of the field
	 * fldvalue = value of name field
	 * fldId = Id field name that we want
	 */
	public String GetIdofName(String tblname,String fldname,String fldvalue,String fldId)
	{
		GenericDelegator delegator = GenericDelegator.getGenericDelegator("default");
		List<GenericValue> abc;
		
		HashMap content = new HashMap();
		String temp3[]=new String[1];
		try 
		{
			EntityExpr expr = new EntityExpr(fldname, EntityOperator.EQUALS,
					fldvalue);
			abc = delegator.findByCondition(tblname,
					expr, null, null);		
			GenericValue temp2=(GenericValue) abc.get(0);
			temp3[0]=temp2.getString(fldId);
			return temp3[0];
		}
		catch (Exception e) 
		{
			strError=e.getMessage();
			return "";
		}
		
	}
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
			String[] temp2=new String[ks.size()];
			
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
		long abc=0;
		try 
		{
		List <GenericValue> genericValue = delegator.findByCondition(tblname, null, UtilMisc.toList(fldname) , null);
		
		/*String xyz="";
		int size=genericValue.size();
		if (size>0)
		{
		  xyz = genericValue.get(0).toString();
		}*/
		/*if (genericValue.size()>0){
		for (GenericValue genericValue2 : genericValue) {
			
			abc = genericValue2.getLong(fldname);
			
		}
		}*/
		//BigDecimal MaxVal =  genericValue.get(0).getBigDecimal(fldname);
		
		for (GenericValue genericValue2 : genericValue) {
			
			abc = genericValue2.getLong(fldname);
			
		}
		
		//abc = Long.parseLong(xyz);
		abc++;
		
		//int MaxVal = genericValue getInteger(0);
		 return abc;
		}
		catch (Exception e) 
		{
			strError=e.getMessage();
			abc++;
			return abc;
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
	public List<GenericValue> getAllDataTable(String tablename,String OrderBy)
	{
		
		List<GenericValue> retvalue2=null;
		try 
		{
			retvalue2=delegator.findAll(tablename, UtilMisc.toList(OrderBy));
		}
		catch (Exception e) 
		{
			
			strError="getAllDataTable()"+e.getMessage();
			
			
		}
		return retvalue2;
	}
	public String GetACYear()
	{
		GenericDelegator delegator = GenericDelegator.getGenericDelegator("default");
		List<GenericValue> abc;
		
		HashMap content = new HashMap();
		String temp3[]=new String[1];
		try 
		{
			EntityExpr expr = new EntityExpr("Fy_Status", EntityOperator.EQUALS,
					"Yes");
			abc = delegator.findByCondition("tbl_Financial_Year",
					expr, null, null);		
			GenericValue temp2=(GenericValue) abc.get(0);
			temp3[0]=temp2.getString("Fy_AcYear");
			return temp3[0];
		}
		catch (Exception e) 
		{
			strError=e.getMessage();
			return "";
		}
		
	}
	
	
	public ArrayList GetBillSequence(String acYear)
	{
		GenericDelegator delegator = GenericDelegator.getGenericDelegator("default");
		List<GenericValue> abc;
		
		ArrayList content = new ArrayList(2);
		long temp3=0;
		try 
		{
			EntityExpr expr = new EntityExpr("Fy_AcYear", EntityOperator.EQUALS,
					acYear);
			EntityExpr expr2 = new EntityExpr("Sq_Ref", EntityOperator.EQUALS,
					"BILL");
			
			
			EntityConditionList t2=new EntityConditionList( UtilMisc.toList(expr,expr2),EntityOperator.AND);
			
			abc = delegator.findByCondition("tbl_Sequence_AcYear",
					t2, null, null);		
			GenericValue temp2=(GenericValue) abc.get(0);
			
			content.add(0,temp2.getLong("Sq_Id"));
			content.add(1,temp2.getLong("Sequence_Id"));
			return content;
		}
		catch (Exception e) 
		{
			strError=e.getMessage();
			return null;
		}
		
	}
	public Date GetFromDate(String acYear)
	{
		GenericDelegator delegator = GenericDelegator.getGenericDelegator("default");
		List<GenericValue> abc;
		
		ArrayList content = new ArrayList(2);
		long temp3=0;
		try 
		{
			EntityExpr expr = new EntityExpr("Fy_AcYear", EntityOperator.EQUALS,
					acYear);
			EntityExpr expr2 = new EntityExpr("Sq_Ref", EntityOperator.EQUALS,
					"BILL");
			
			
			EntityConditionList t2=new EntityConditionList( UtilMisc.toList(expr,expr2),EntityOperator.AND);
			
			abc = delegator.findByCondition("tbl_Sequence_AcYear",
					t2, null, null);		
			GenericValue temp2=(GenericValue) abc.get(0);
						
			return temp2.getDate("fromdate");
		}
		catch (Exception e) 
		{
			strError=e.getMessage();
			return null;
		}
		
	}
	
	public ArrayList GetReceiptSequence(String acYear)
	{
		GenericDelegator delegator = GenericDelegator.getGenericDelegator("default");
		List<GenericValue> abc;
		
		ArrayList content = new ArrayList(2);
		long temp3=0;
		try 
		{
			EntityExpr expr = new EntityExpr("Fy_AcYear", EntityOperator.EQUALS,
					acYear);
			EntityExpr expr2 = new EntityExpr("Sq_Ref", EntityOperator.EQUALS,
					"RECEIPT");
			
			
			EntityConditionList t2=new EntityConditionList( UtilMisc.toList(expr,expr2),EntityOperator.AND);
			
			abc = delegator.findByCondition("tbl_Sequence_AcYear",
					t2, null, null);		
			GenericValue temp2=(GenericValue) abc.get(0);
			
			content.add(0,temp2.getLong("Sq_Id"));
			content.add(1,temp2.getLong("Sequence_Id"));
			return content;
		}
		catch (Exception e) 
		{
			strError=e.getMessage();
			return null;
		}
		
	}
	
	/*
	 * 
	 * inorder to empty this string which holds the error that has occured.
	 */
	private void emptyerror()
	{
		
		strError="";
	}
	public boolean updateAllWithOneValue(String tablename,String fieldname,String onevalue)
	 {
		 List<GenericValue> allrecords=this.getAllDataTable(tablename);
		 for(int i=0;i<allrecords.size();i++)
		 {
			 GenericValue val=allrecords.get(i);
			 val.put(fieldname, onevalue);
			 try {
				val.store();
				
			} catch (GenericEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		 }
		 return true;
	 }
	/*
	 * Compares the date between two dates from database- For Reports
	 * 
	 */
	public List<GenericValue> getDateComparison(String tblname,String fieldname,Date fromdate, Date todate) {
		GenericDelegator delegator = GenericDelegator
				.getGenericDelegator("default");
		List<GenericValue> abc;

		try {
				EntityExpr  ex=new EntityExpr(fieldname, EntityOperator.BETWEEN,UtilMisc.toList(fromdate,todate));
				return delegator.findByCondition(tblname, ex, null, null);
				
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;

	}
	
	public String convertDoubleToWords(double currencyamt)
	{
		bussinessapi.AmountByWord b=new AmountByWord();
		b.setCurrency("IN");
        b.setLanguage("en");
        try {
            String moneyAmountByWord = b.getMoneyAmountByWord(currencyamt);
//            alert(moneyAmountByWord);
            //txtoutput2.setText(moneyAmountByWord);
            String [] buf1=moneyAmountByWord.split("Indian Rupees");
            String realoutput=buf1[0];
            String [] buf2=buf1[1].split("Paises");
            double db=Double.parseDouble(buf2[0]);
            String paises=b.getMoneyAmountByWord(db);
            paises=paises.split("Indian Rupees")[0];
            realoutput+=" and "+paises+" paise only";
            
            //moneyAmountByWord= "Rs."+moneyAmountByWord.replaceAll("Indian Rupees", "");

//            if(currencyamt>=100000 && currencyamt<1000000)
//            {
//            	String[] buf3=realoutput.split("hundred");
//            	realoutput=buf3[0]+" lakh "+buf3[1];
//            }
            return realoutput;
//            txtoutput.setText(realoutput);

        } catch (Exception ex) {
//            Logger.getLogger(ConvertToIndian.class.getName()).log(Level.SEVERE, null, ex);
          strError+= ex.getMessage();
        }		return "";
        
        
	}
}
	
	
	

