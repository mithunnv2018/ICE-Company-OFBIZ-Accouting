package org.ofbiz.ICE.ZkCodeBehind;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.fortuna.ical4j.util.Dates;

import org.ofbiz.ICE.ZkServices.ZkCommonServices;
import org.ofbiz.ICE.zkValidation.zkCommonValidations;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.ext.client.Selectable;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.AbstractListModel;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.SimpleConstraint;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class CB_SalesOrderStatusModify extends GenericForwardComposer{

	public Window SalesOrderModify;
	private ZkCommonServices objCommonServices;
	private zkCommonValidations objCommonValidations;

	public long seqId;
	public String acYear;
	public long billseq;

	 
	public Listbox lstcustomerdetailstatusmodify;

	String variablemodel = "SalesOrderStatusModify";
	private List<GenericValue> data2 = null;
	private long maxSeqVal;

	// Buttons
	public Button btnSave;
	public Button btnCancel;
	  
	  
	 
	 
	 
	 

	// Modify ID
	public String variableId = "0";

	// -------Add new components--------
	public Textbox txtsalesordernoaddnew;
	public Bandbox bdCustNameAddnew;
	public Bandbox bdStatus;
	public Listbox lstStatus;
	 public Listbox lstSalesOrderStatusModify;
	 

	// ---Modify Components
	 
	public Button btnGridRefresh;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		
		comp.setVariable(variablemodel, this, true);
		
		//ListModel sm = new SimpleListModel(getData2());
		acYear = objCommonServices.GetACYear();
 		SetControls();
			try{
 		 setData2(objCommonServices.getAllDataTable("VE_Select_tbl_SalesOrder_Customer"));
				}
				catch(Exception exxx)
				{
					//alert(objCommonServices.strError);
					//txterror.setText(txterror.getText()+ exxx.getMessage());
				}
			 
		
//		}
		// txtMainCategory.setConstraint(objempty);

	}

	public void SetControls()
	{
  		String a[] =new String[]{"VOID","APPROVED" ,"CANCELED"};
		ListModel xyz = new ListModelList(a);
		
		lstStatus.setModel(xyz);

	}
	public void onClick$btnGridRefresh(Event e)
	{
		 
		 ListModelList xas = new ListModelList( objCommonServices.getAllDataTable("VE_Select_tbl_SalesOrder_Customer"));
		 
 		 lstSalesOrderStatusModify.setModel(xas);
 			
	}
	public CB_SalesOrderStatusModify() {
		objCommonServices = new ZkCommonServices();
		objCommonValidations = new zkCommonValidations();
		 
	}
	 
	 
	public void onClick$btnSave(Event e) {
		 Set<Listitem> local2=lstSalesOrderStatusModify.getSelectedItems();
		 Listitem []a=null;

		 a=local2.toArray(new Listitem[0]);
		 for (Listitem listitem : a) {
			List<Listcell> b= listitem.getChildren();
			String id2="";
			
			for (int index=0;index<b.size();index++) {
				if(index==5)
				{
					 id2=String.valueOf(b.get(index).getLabel());
					
				}
//				alert("Hi "+String.valueOf(b.get(index).getLabel()));
			}
			Map  StrId = UtilMisc.toMap("So_Id",String.valueOf(id2));
			Map StrValues =UtilMisc.toMap("So_Status",bdStatus.getText());
			if(UpdateTable(StrValues, StrId)==false)
			{
				alert("Not updated "+id2);
			}
		}

		 alert("Updated");
	}
	 
	 
	 
	
	private boolean UpdateTable(Map StrValues,Map StrId)
	{
		
		if (objCommonServices.updateTable("tbl_SalesOrder_Details", StrValues,StrId) == true)
		{
			return true;
			
		}
		return false;
	}
	 
 
	public void onClick$btnCancel(Event e) {

		//UpdateSOItems();
		clearControlsNew();

	}
 
  
	 
	public void clearControlsNew() {
		 
		bdStatus.setText("--Select--");
		SetControls();
	}

 
	public void setData2(List<GenericValue> data2) {
		this.data2 = data2;
	}

	public List<GenericValue> getData2() {
		return data2;
	}
//	public void onSelect$lstSalesOrderModify(Event e)
//	{
//		Listitem a= lstSalesOrderModify.getSelectedItem();
//		
//		List a2=a.getChildren();
//		for(int i=0;i<a2.size();i++)
//		{
//			Listcell lc=(Listcell) a2.get(i);
//			
//			//alert(lc.getLabel());
//			if(i==0){
//				variableId=lc.getLabel(); 
//				maxSeqVal=Long.valueOf(lc.getLabel());
//			}
//			//get Id
//			//if (i==1){txtMainCatModify.setText(lc.getLabel());}//Set Main Category Name
//		}
//		
//		List<GenericValue> StrRow = objCommonServices.ReturnRowGen("VE_Select_tbl_SalesOrder_Customer", "veSo_Id", variableId);
//		
//		GenericValue genericValue5 = StrRow.get(0);
//		
//		//txtmodifycustomername.setText(genericValue5.getString("Cust_Name"));
//		//---------Populate Form--------------------
//		
//		txtsalesordernoaddnew.setText(genericValue5.getString("veSo_Nos"));
//		bdCustNameAddnew.setText(genericValue5.getString("veCust_Name"));
//		
//		 bdStatus.setText(genericValue5.getString("veSo_Status"));
//				
//		//Have to convert date format----------
//		//dateSalesDateAddnew.setText(genericValue5.getString("veSo_Date"));
//		dateSalesDateAddnew.setValue(genericValue5.getDate("veSo_Date"));
//		//------------------------------------------VE_Select_tbl_SalesOrder_Items
//		
//		ListModel xm = new ListModelList(objCommonServices.ReturnRowGen("VE_Select_tbl_Items_StockIt", "veSo_Id", variableId));
//		lstSoItemsAddnew.setModel(xm);
//	}
}
