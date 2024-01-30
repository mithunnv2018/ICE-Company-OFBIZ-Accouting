/**
 * 
 */
package org.ofbiz.ICEInc;

import java.util.ArrayList;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Textbox;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.text.TextBox;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

/**
 * @author Mithun
 *
 */
public class Stock_Category extends GenericForwardComposer {

	public ArrayList mainCategory;
	public ListModel cat1;
	public String[] a1={"good ","bad"};
	private ListModel cat2;
	public MainCategoryServices objMainCat;
	public ArrayList  abc;
	public org.zkoss.zul.Listbox lk_MainCatStockCat;
	public Listbox lk_SubCatStockCat;
	public Textbox txtStockCatName;
	public ArrayList subCategory;
	/* 
	 * MODIFY2 is used to set the flag if to show the grid for update
	 */
	public   boolean MODIFY2=false;
	public Textbox txtStockCatModifyId;
	public Textbox txtStockCatModifyName;
	public Grid UpdateStockCat;
	
	public Stock_Category() {
	
		mainCategory=new ArrayList();
		setSubCategory(new ArrayList());
		
		objMainCat=new MainCategoryServices();
		
		HashMap aa = objMainCat.PopulateMainCategory2();
		
		 
		

		Set ks=aa.keySet();
		String[] temp2=new String[1];
		
		String []keys2=(String[])ks.toArray(temp2);
		
		for(int i=0;i<keys2.length;i++)
		{
			String name=(String)aa.get(keys2[i]);
			String value=(String)keys2[i];
			mainCategory.add(name);
			
		}
		
		
	} 
	public List<GenericValue> getStockCat2()
	{
		MainCategoryServices a=new MainCategoryServices();
		return a.getStockCatTable();
		
	}
	public void setMainCategory(ArrayList mainCategory) {
		this.mainCategory = mainCategory;
	}

	public ArrayList getMainCategory() {
		
		mainCategory=new ArrayList();
//		objMainCat=new MainCategoryServices();
		
		HashMap aa = objMainCat.PopulateMainCategory2();
		
		 
		

		Set ks=aa.keySet();
		String[] temp2=new String[1];
		
		String []keys2=(String[])ks.toArray(temp2);
		
		for(int i=0;i<keys2.length;i++)
		{
			String name=(String)aa.get(keys2[i]);
			String value=(String)keys2[i];
			mainCategory.add(name);
			
		}
		
		
		return mainCategory;
	}
	//lk_MainCatStockCat
	public void onSelect$lk_MainCatStockCat(Event e)
	{
		
		String item2=lk_MainCatStockCat.getSelectedItem().getValue().toString();
		List xyz= objMainCat.PopulateSubCategory(objMainCat.getMainCatID(item2));
//		String item2=lk_MainCatStockCat.getSelectedItem().getLabel();
		txtStockCatName.setText(objMainCat.getMainCatID(item2));
		
		String temp3[]=new String[xyz.size()];
		
		for(int i=0;i<xyz.size();i++)
		{
			GenericValue temp2=(GenericValue) xyz.get(i);
			temp3[i]=temp2.getString("SubCat_Name");
			
			//abc.add(xyz.get(i));
			//subCategory.add(temp3);
			//txtStockCatName.setText(txtStockCatName.getText());
			
			
			
		}
		
		ListModel model=new SimpleListModel(temp3);
		lk_SubCatStockCat.setModel(model);
		//lk_SubCatStockCat.
		 
		//alert(objMainCat.getMainCatID(item2));
		
	}
	public void setSubCategory(ArrayList subCategory) {
		this.subCategory = subCategory;
	}
	public ArrayList getSubCategory() {
		return subCategory;
	}
	 
	public void AlertBox2(String a)
	{
		
//		alert("My value is"+a);
		/*GenericValue StrRow = objMainCat.getStockCatGenVal(a);
		txtStockCatModifyId.setText(StrRow.getString("StockCat_Id"));
		txtStockCatModifyName.setText(StrRow.getString("StockCat_Name"));*/
		
		this.MODIFY2=true;
	}
}
/*<grid id="ShowStockCategory">
<rows>
	<row>
		Select the person you wish to edit
		<listbox mold="select">
			<listitem value="--- SELECT BELOW ---" />
			<listitem forEach="${main2.stockCat2}"
				label="${each.StockCat_Name} ${each.SubCat_Id}" value="${each}" />
		</listbox>
	</row>
	<row id="editRow" visible="false">
		<textbox id="firstName" />
		<label id="lastName" />
	</row>
</rows>
</grid>
*/