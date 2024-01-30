/**
 * 
 */
package org.ofbiz.ICE.ZkCodeBehind;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.ofbiz.ICE.ZkServices.ZkCommonServices;
import org.ofbiz.entity.GenericValue;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Window;

/**
 * @author mithun
 * 
 */
public class CB_ReceiptReport extends GenericForwardComposer {

	public Datebox clonedatefrom;
	public Datebox clonedateto;

	// public Datebox winshowdates$datefrom;
	public Listbox lstreceiptreport;
	public Window winreceiptshowdates;

	public ZkCommonServices objCommonServices;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		clonedatefrom = (Datebox) Path
				.getComponent("/winreceiptreport/winreceiptshowdates/datefrom");
		clonedateto = (Datebox) Path
				.getComponent("/winreceiptreport/winreceiptshowdates/dateto");

	}

	public CB_ReceiptReport() {
		super();

		objCommonServices = new ZkCommonServices();

	}

	public void onClick$btnokreceiptreport$winreceiptshowdates$winreceiptreport(
			Event e) {
		
		Date fromdate2 = clonedatefrom.getValue();
		Date todate2 = clonedateto.getValue();

		List<GenericValue> data2 = objCommonServices.getDateComparison(
				"tbl_Receipt_Details", "Receipt_Date", fromdate2, todate2);

		lstreceiptreport.setModel(fillModel2(data2));
		winreceiptshowdates.setVisible(false);
	}

	/*
	 * accepts a list of genericvalues and makes a new model with addditional
	 * fields and uses hashmap
	 */
	protected ListModelList fillModel2(List<GenericValue> param1) {
		ListModelList returnobj = new ListModelList();

		for (int i = 0; i < param1.size(); i++) {

			GenericValue row2 = param1.get(i);
			String custname;

			try {
				String custid = row2.getString("Cust_Id");
				//alert("Inside fillmodel before data2" + custid);

				List<GenericValue> data2 = objCommonServices.ReturnRowGen(
						"tbl_Customer_Master", "Cust_Id",
						custid);

				//alert("After data2 length is " + data2.size());
				GenericValue row3 = data2.get(0);

				custname = row3.getString("Cust_Name");

				
				HashMap<String, String> newrow2 = new HashMap<String, String>();
				newrow2.put("Receipt_Nos", row2.getString("Receipt_Nos"));
				newrow2.put("Cust_Name", custname);
				newrow2.put("Receipt_Date", row2.getString("Receipt_Date"));
				newrow2.put("Receipt_Amt", row2.getString("Receipt_Amt"));
				newrow2.put("Receipt_Paytype", row2
						.getString("Receipt_Paytype"));
				newrow2.put("Receipt_Remarks", row2
						.getString("Receipt_Remarks"));
				newrow2.put("Receipt_Tag", row2.getString("Receipt_Tag"));
				returnobj.add(newrow2);
			} catch (Exception ex) {

			}
		}

		return returnobj;

	}

}
