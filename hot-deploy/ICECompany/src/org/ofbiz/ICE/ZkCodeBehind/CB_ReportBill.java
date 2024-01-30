package org.ofbiz.ICE.ZkCodeBehind;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.ofbiz.ICE.ZkServices.ZkCommonServices;
import org.ofbiz.entity.GenericValue;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Window;

public class CB_ReportBill extends GenericForwardComposer {

	public Datebox clonedatefrom;
	public Datebox clonedateto;
	
	public Datebox winshowdates$datefrom;
	public Listbox lstbillreport;
	public Window winshowdates;
	public Label lbltotal;
	public Label lblqty;
	
	public ZkCommonServices objCommonServices;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		clonedatefrom=(Datebox)Path.getComponent("/winbillreport/winshowdates/datefrom");
		clonedateto=(Datebox)Path.getComponent("/winbillreport/winshowdates/dateto");
		
		
	}
	
	public CB_ReportBill() {
		super();
		objCommonServices=new ZkCommonServices();		
	}

	 

	public void onClick$btnokreport$winshowdates$winbillreport(Event e)
	{
	//	alert("HI");
		
		Date fromdate2=clonedatefrom.getValue();
		Date todate2=clonedateto.getValue();
		
		List<GenericValue> data2= objCommonServices.getDateComparison("VE_Select_tbl_SalesOrder_Customer","veSo_Date",fromdate2,todate2);
		ListModelList model=new ListModelList(data2);
		
			
		ListModelList mymodel=new ListModelList();
		
		double totalamt=0;
		int totalqty=0;
		for(int i=0;i<data2.size();i++)
		{
			
			GenericValue row=data2.get(i);
			
			totalamt = totalamt + row.getDouble("veSo_TotalAmt");	
			totalqty = totalqty+getQTY(row.getString("veSo_Id"));
			
			HashMap mymap = new HashMap();
			mymap.put("veSo_Nos", row.getString("veSo_Nos"));
			mymap.put("veCust_Name", row.getString("veCust_Name"));
			mymap.put("veSo_Qty", String.valueOf(getQTY(row.getString("veSo_Id"))));
			mymap.put("veSo_Date", row.getString("veSo_Date"));
			mymap.put("veSo_Amt", row.getString("veSo_Amt"));
			mymap.put("veSo_TaxPer", row.getString("veSo_TaxPer"));
			mymap.put("veSo_TaxAmt", row.getString("veSo_TaxAmt"));
			mymap.put("veSo_AmtAdjust", row.getString("veSo_AmtAdjust"));
			mymap.put("veSo_TotalAmt", row.getString("veSo_TotalAmt"));
			mymap.put("veSo_Status", row.getString("veSo_Status"));
			
		

			mymodel.add(mymap);
			
			
		}
		//alert("Total: " + totalamt);
		lblqty.setValue(""+totalqty);
		lbltotal.setValue(""+totalamt);
		lstbillreport.setModel(mymodel);
		winshowdates.setVisible(false);
		
		
	}
	
	public int getQTY(String SoId)
	{
		List<GenericValue> data2= objCommonServices.ReturnRowGen("tbl_SOItems_Details", "So_Id", SoId);// ("VE_Select_tbl_SalesOrder_Customer","veSo_Date",fromdate2,todate2);
		//ListModelList model=new ListModelList(data2);
		long qty=0;
		for(int i=0;i<data2.size();i++)
		{
			
			//GenericValue row=data2.get(i);			
			GenericValue row=data2.get(i);
			
			qty = qty + row.getLong("SOItems_Qty");		
		}
		
		return (int)qty;
	}

	 
}
