/**
 * 
 */
package org.ofbiz.ICE.ZkCodeBehind;

import java.util.Map;

import org.ofbiz.ICE.ZkServices.ZkCommonServices;
import org.ofbiz.base.util.UtilMisc;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Window;

/**
 * @author mithun
 *
 */
public class CB_MainPage extends GenericForwardComposer {

	public Window winMainPage;
	public Bandbox bdFinancialYear;
	public Listbox lstFinancialYear;
	public Button btOkFinancialYear;
	
	public ZkCommonServices objCommonServices;
	
	public CB_MainPage()
	{
		objCommonServices=new ZkCommonServices();
		
	}
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		lstFinancialYear.setModel(objCommonServices.PopulateListBox("tbl_Financial_Year", "Fy_AcYear", "", ""));
		
		
	}
	
	public void onClick$btOkFinancialYear(Event e) {
		
		//winMainPage.setClosable(true);
		String year2=bdFinancialYear.getText();
		if(year2.trim().length()<=0 || year2.equals("--Select--"))
		{
			alert("You have to Select a Financial Year ");
			return;
		}
		
		objCommonServices.updateAllWithOneValue("tbl_Financial_Year", "Fy_Status", "No");
		String key2=objCommonServices.GetIdofName("tbl_Financial_Year", "Fy_AcYear", year2, "Fy_Id");
		Map StrId = UtilMisc.toMap("Fy_Id",key2);
		Map mptblvalues=UtilMisc.toMap("Fy_Status","Yes");
		
		
		if(objCommonServices.updateTable("tbl_Financial_Year", mptblvalues, StrId)==false)
		{
			alert("Fintable not updated!");
		}
		
		winMainPage.setVisible(false);
		
		
	}

}
