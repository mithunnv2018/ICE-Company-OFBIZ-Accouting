package org.ofbiz.ICEInc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

public class MainCategoryServices {

	/*
	 * public static Map PopulateMainCategory(DispatchContext dctx,Map context)
	 * { GenericDelegator delegator=dctx.getDelegator(); Map resultMap=null; Map
	 * content=new HashMap();
	 * 
	 * List <GenericValue>abc;
	 * 
	 * try { abc=delegator.findAll("tbl_MainCategory_Master"); for (GenericValue
	 * var2 : abc) { String name2=var2.getString("MaintCat_Name"); String
	 * catid=var2.getString("MaintCat_Id"); content.put(catid, name2);
	 * 
	 * }resultMap=ServiceUtil.returnSuccess(
	 * "Succesfully retrieved MainCategory_Master"); resultMap.put("mainCatMap",
	 * content);
	 * 
	 * 
	 * } catch (GenericEntityException e) { // TODO Auto-generated catch block
	 * e.printStackTrace();
	 * resultMap=ServiceUtil.returnError("Mith Some Error "+e.getMessage()); }
	 * return resultMap; }
	 */
	public HashMap PopulateMainCategory2() {

		GenericDelegator delegator = GenericDelegator
				.getGenericDelegator("default");

		// Map resultMap=null;
		HashMap content = new HashMap();

		List<GenericValue> abc;

		try {
			abc = delegator.findAll("tbl_MainCategory_Master");
			for (GenericValue var2 : abc) {
				String name2 = var2.getString("MainCat_Name");
				String catid = var2.getString("MainCat_Id");
				content.put(catid, name2);

			}
			// resultMap=ServiceUtil.returnSuccess("Succesfully retrieved MainCategory_Master");
			// resultMap.put("mainCatMap", content);

		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// resultMap=ServiceUtil.returnError("Mith Some Error "+e.getMessage());
		}
		return content;
	}

	public List PopulateSubCategory(String name2) 
	{
		GenericDelegator delegator = GenericDelegator
				.getGenericDelegator("default");

		EntityExpr expr = new EntityExpr("MainCat_Id", EntityOperator.EQUALS,
				name2);
		HashMap content = new HashMap();

		List<GenericValue> retvalue2=null;

		try {
			retvalue2 = delegator.findByCondition("tbl_SubCategory_Master",
					expr, null, null);


		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retvalue2;
	}
	/* Get MaintCat_Id  from table tbl_MainCategory_Master
	 * 
	 * */
	public String getMainCatID(String name2) 
	{
		GenericDelegator delegator = GenericDelegator
				.getGenericDelegator("default");

		EntityExpr expr = new EntityExpr("MainCat_Name", EntityOperator.EQUALS,
				name2);
		HashMap content = new HashMap();

		List<GenericValue> retvalue2=null;
		String retvalue3="";
		

		try {
			retvalue2 = delegator.findByCondition("tbl_MainCategory_Master",
					expr, null, null);

			if(retvalue2!=null && retvalue2.size()>0)
			{
				retvalue3=(String)   retvalue2.get(0).get("MainCat_Id");
				
			}

		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retvalue3;
	}
	
	public List<GenericValue> getStockCatTable()
	{
		GenericDelegator delegator = GenericDelegator.getGenericDelegator("default");
		List<GenericValue> retvalue2=null;
		
		try {
			retvalue2= delegator.findAll("tbl_StockCategory_Master");
			
			

		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retvalue2;
	}
	public GenericValue getStockCatGenVal(String stockCatId)
	{
		GenericDelegator delegator = GenericDelegator.getGenericDelegator("default");
		GenericValue retvalue2=null;
		EntityExpr expr = new EntityExpr("StockCat_Id", EntityOperator.EQUALS,
				stockCatId);
		try {
			
			retvalue2 = (GenericValue) delegator.findByCondition("tbl_StockCategory_Master",
					expr, null, null);
			

		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retvalue2;
	}
	
	
}
