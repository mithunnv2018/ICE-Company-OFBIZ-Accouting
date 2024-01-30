/**
 * 
 */
package org.ofbiz.ICE.ZkCodeBehind;

import java.util.ArrayList;
import java.util.List;

import org.ofbiz.ICE.ZkServices.ZkCommonServices;
import org.ofbiz.ICE.zkValidation.zkCommonValidations;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.widget.screen.ZkViewHandler;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Window;
/**
 * @author mithun
 *
 */
public class CB_BillOnly extends GenericForwardComposer {

	public Window winquickreceiptinfo;
	public Listbox clonelstCustNameReceipt;
	public Listbox clonelstReceiptNo;
	public Bandbox clonebdCustNameReceipt;
	public Bandbox clonebdReceiptNo;
	
	
	public Label lblreceiptno;
	public Label lblreceiptdate;
	public Label lblcustomername;
	public Label lblamount;
	public Label lblpaymenttype;
	public Label lblamountinwords;
	
	
	
	public ZkCommonServices objCommonServices;
	public zkCommonValidations objCommonValidations;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		
		clonelstCustNameReceipt=(Listbox)Path.getComponent("/winreceiptonly/winquickreceiptinfo/lstCustNameReceipt");
		clonelstReceiptNo=(Listbox)Path.getComponent("/winreceiptonly/winquickreceiptinfo/lstReceiptNo");
	    clonebdCustNameReceipt=(Bandbox)Path.getComponent("/winreceiptonly/winquickreceiptinfo/bdCustNameReceipt");
	    clonebdReceiptNo=(Bandbox)Path.getComponent("/winreceiptonly/winquickreceiptinfo/bdReceiptNo");
	    
		clonelstCustNameReceipt.setModel(objCommonServices.PopulateListBox("VE_Select_tbl_Customer_Master", "veCust_Name", "", ""));
//		clonelstReceiptNo.setModel(objCommonServices.PopulateListBox("tbl_Receipt_Details", "Receipt_Nos", "", ""));
		
		clonebdCustNameReceipt.setText("--Select--");
		clonebdReceiptNo.setText("--Select--");
	}
	public CB_BillOnly() {
		super();
		objCommonServices=new ZkCommonServices();
		objCommonValidations=new zkCommonValidations();
	}

	public void onSelect$lstCustNameReceipt$winquickreceiptinfo$winreceiptonly(Event e)
	{
		
		String custname2=clonebdCustNameReceipt.getText();
		String key2=objCommonServices.GetIdofName("tbl_Customer_Master", "Cust_Name", custname2, "Cust_Id");
//		List<GenericValue>  val2=objCommonServices.ReturnRowGen("tbl_SalesOrder_Details", "Cust_Id", key2);
		
		List<GenericValue> val2= objCommonServices.ReturnRowGen("tbl_Receipt_Details", "Cust_Id", key2);
		ArrayList data2=new ArrayList();
		data2.add("--Select--");
		
		for (GenericValue item : val2) {
			data2.add(item.getString("Receipt_Nos"));
		}
		
		ListModelList model=new ListModelList(data2);
		clonelstReceiptNo.setModel(model);
		clonebdReceiptNo.setText("");
		
	}
	public void onClick$btnokreceipt$winquickreceiptinfo$winreceiptonly(Event e)
	{
		if(!validateControls()){
			return;
		}
		
		String custname=clonebdCustNameReceipt.getValue();
		String receiptno=clonebdReceiptNo.getValue();
	
		lblcustomername.setValue(custname);
		List<GenericValue>data2= objCommonServices.ReturnRowGen("tbl_Receipt_Details", "Receipt_Nos", receiptno);
		
		GenericValue row2=data2.get(0);
		lblreceiptno.setValue(row2.getString("Receipt_Nos"));
		lblreceiptdate.setValue(row2.getString("Receipt_Date"));
		lblamount.setValue(row2.getString("Receipt_Amt"));
		lblpaymenttype.setValue(row2.getString("Receipt_Paytype"));
//		lblamountinwords.setValue(objCommonServices.convertDoubleToWords(100000));
		if(objCommonValidations.TextBoxValidate(row2.getString("Receipt_Amt")))
		{
			lblamountinwords.setValue(objCommonServices.convertDoubleToWords(Double.parseDouble(row2.getString("Receipt_Amt"))));
		}
//		lblamountinwords.setValue(objCommonServices.convertDoubleToWords(Double.parseDouble(row2.getString("Receipt_Amt"))));
		winquickreceiptinfo.setVisible(false);
		
		
		
	}
	
	// Vaidate Add New Components
	/*
	 * DONE uppdate on 24/02/2011
	 */
	public boolean validateControls() {
		String Error = "";
		Error += objCommonValidations.validateForSelect(clonebdCustNameReceipt.getText().trim()) ? "" : "Select a valid customer name. \n";
		Error += objCommonValidations.validateForSelect(clonebdReceiptNo.getText().trim()) ? "" : "Select a valid receipt no. \n";
		
		if (!Error.equals("")) {
			alert(Error);
			return false;
		} else {
			return true;
		}

	}
 	
}
