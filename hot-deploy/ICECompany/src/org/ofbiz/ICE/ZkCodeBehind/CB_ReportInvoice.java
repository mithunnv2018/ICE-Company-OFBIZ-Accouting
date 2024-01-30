/**
 * 
 */
package org.ofbiz.ICE.ZkCodeBehind;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ofbiz.ICE.ZkServices.ZkCommonServices;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Window;

/**
 * @author mithun
 * 
 */
public class CB_ReportInvoice extends GenericForwardComposer {

	public Window wintaxinvoice;
	public Window winquickinfo;
 
//	public Label bye;
//	public Listbox winquickinfo$my;
	public ZkCommonServices objCommonServices;

	public Listbox clonelstCustNameReport;
	public Listbox clonelstSalesOrderReport;
	public Bandbox clonebdCustNameReport;
	public Button clonebtnokreport;
	public Bandbox clonebdSalesOrderReport;
	
	//all report components
	public Label lblbillno;
	public Label lblbilldate;
	public Label lblsalestax;
	public Label lblname;
	public Label lbladdress;
	public Label lblphone;
	public Listbox lstitems;
	public Label lbltotal;
	public Label lbltaxpercentage;
	public Label lbltaxamt;
	public Label lblinctax;
	public Label lbladjdiscount;
	public Label lbltotalamt;
	public Label lblTotalOS;
	public Label lblOtherOS;
	
	
	private String message="";
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
 		winquickinfo.setMaximizable(true);
 		message=Path.getComponent("/wintaxinvoice/winquickinfo/lstCustNameReport").toString();
 		clonelstCustNameReport=(Listbox)Path.getComponent("/wintaxinvoice/winquickinfo/lstCustNameReport");
 		clonelstSalesOrderReport=(Listbox)Path.getComponent("/wintaxinvoice/winquickinfo/lstSalesOrderReport");
 		clonebdCustNameReport=(Bandbox)Path.getComponent("/wintaxinvoice/winquickinfo/bdCustNameReport");
 		clonebtnokreport=(Button)Path.getComponent("/wintaxinvoice/winquickinfo/btnokreport");
 		clonebdSalesOrderReport=(Bandbox)Path.getComponent("/wintaxinvoice/winquickinfo/bdSalesOrderReport");
 		
 		clonelstCustNameReport.setModel(objCommonServices.PopulateListBox("tbl_Customer_Master","Cust_Name", "",""));
 		
 		
 		
 		
	}

	public CB_ReportInvoice() {
		objCommonServices = new ZkCommonServices();

	}

	
	public void onSelect$lstCustNameReport$winquickinfo$wintaxinvoice(Event e) {
		//alert("HI"+message);
		String custname2=clonebdCustNameReport.getText();
		String key2=objCommonServices.GetIdofName("tbl_Customer_Master", "Cust_Name", custname2, "Cust_Id");
		List<GenericValue>  val2=objCommonServices.ReturnRowGen("tbl_SalesOrder_Details", "Cust_Id", key2);
		ArrayList data2=new ArrayList();
		for (GenericValue item : val2) {
			
			 data2.add(item.getString("So_Nos"));
			 
			 
		}
		ListModelList model=new ListModelList(data2);
		clonelstSalesOrderReport.setModel(model);
		
		
//		winquickinfo$my.setWidth("100");
	}
	
	public void onClick$btnokreport$winquickinfo(Event e)
	{
//		alert("HI");
		String customername=clonebdCustNameReport.getValue();
		String salesorderno=clonebdSalesOrderReport.getValue();
		fillData(customername, salesorderno);
		
		
		
		
		winquickinfo.setVisible(false);
		
	}
	protected void fillData(String customername,String salesorderno)
	{
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		List<GenericValue> data2=objCommonServices.ReturnRowGen("VE_Select_tbl_SalesOrder_Customer","veSo_Nos", salesorderno);
		
		GenericValue row2=data2.get(0);
		
		
		Execution ex=Executions.getCurrent();		
		ex.setAttribute("SalesOrderNo", salesorderno);
		this.session.setAttribute("SalesOrderNo", salesorderno);
		ex.sendRedirect("billprinting");
		
		lblbillno.setValue(row2.getString("veSo_Nos"));
		lblbilldate.setValue(row2.getString("veSo_Date"));
		
		lblname.setValue(row2.getString("veCust_Name"));
		lbladdress.setValue(row2.getString("veCust_Address"));
		lblphone.setValue(row2.getString("veCust_PhNo"));
		lbltotal.setValue(row2.getString("veSo_Amt"));
		lbltaxpercentage.setValue(row2.getString("veSo_TaxPer"));
		lbltaxamt.setValue(row2.getString("veSo_TaxAmt"));
		
		lblinctax.setValue(String.valueOf(twoDForm.format(row2.getDouble("veSo_Amt")+row2.getDouble("veSo_TaxAmt"))));
		lbladjdiscount.setValue(row2.getString("veSo_AmtAdjust"));
		lbltotalamt.setValue(String.valueOf(twoDForm.format(row2.getDouble("veSo_TotalAmt"))));
		
		List<GenericValue> data3=objCommonServices.ReturnRowGen("VE_Select_tbl_Items_StockIt", "veSo_Id", row2.getString("veSo_Id"));
		
//		ListModelList model2=new ListModelList(data3);
		lstitems.setModel(fillModel(data3));
		
		try
		{		
		EntityExpr expr = new EntityExpr("Cust_Id", EntityOperator.EQUALS, objCommonServices.GetIdofName("tbl_Customer_Master", "Cust_Name", customername, "Cust_Id"));
		
		EntityExpr expr2 = new EntityExpr("So_Status", EntityOperator.EQUALS, "APPROVED");
		
		
		List a= UtilMisc.toList(expr,expr2);
		 
		EntityConditionList condList = new EntityConditionList(a,EntityOperator.AND);
		 		
		
		List<GenericValue> strRow= objCommonServices.ReturnMultipleCondition("VE_Sum_SOPayment_ByCustomer", condList); 
		
		GenericValue row = strRow.get(0);	
		
		lblTotalOS.setValue(String.valueOf(twoDForm.format(row.getDouble("SoPay_TotalAmt"))));
		lblOtherOS.setValue(String.valueOf(Double.valueOf( twoDForm.format( Double.valueOf(row.getString("SoPay_TotalAmt"))-row2.getDouble("veSo_TotalAmt")))));
		
		//Session and Redirecting Code
		
		
		}
		catch(Exception e)
		{
			lblTotalOS.setValue(String.valueOf(twoDForm.format(row2.getDouble("veSo_TotalAmt"))));
			lblOtherOS.setValue("0.00");
			
		}
		//return Double.valueOf(twoDForm.format(d));

		
		
	}
	
	/*accepts a list of genericvalues and makes a new model with
	 * addditional fields and uses hashmap
	 * 
	 */
	protected ListModelList fillModel(List<GenericValue> param1)
	{
		ListModelList returnobj=new ListModelList();
	
		for(int i=0;i<param1.size();i++)
		{
			GenericValue row2=param1.get(i);
			String srno,particulars,qty,unitamt,amt;
			
			srno=String.valueOf(i+1);
			particulars=row2.getString("veStockIt_Name");
			long qty2=row2.getLong("veSOItems_Qty");
			double unit2=row2.getDouble("veSOItems_Amt");
			double amt2=qty2*unit2;
			
			qty=String.valueOf(qty2);
			unitamt=String.valueOf(unit2);
			amt=String.valueOf(amt2);
			
			HashMap newrow2=new HashMap();
			newrow2.put("srno", srno);
			newrow2.put("particulars",particulars);
			newrow2.put("qty",qty);
			newrow2.put("unitamt",unitamt);
			newrow2.put("amt",amt);
			
			returnobj.add(newrow2);
			
		}
		
		return returnobj;
		
	}
	
	//
	//	
}
