/**
 * 
 */
package org.ofbiz.ICEInc;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * @author Mithun
 * 
 */
public class MyMVC extends GenericForwardComposer {

	private Button btn_test;
	private Textbox txt_MainCategory;
	private Label lbl;
	 
	
	public void onClick$btn_test(Event ev) {
		try {
			txt_MainCategory.setValue("u made it!!");


			lbl.setValue("You ???");
		} catch (Exception e) {
			alert(e.toString());
			e.printStackTrace();
		}


	}
}
