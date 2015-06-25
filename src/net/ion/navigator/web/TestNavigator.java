package net.ion.navigator.web;

import java.util.Map.Entry;

import junit.framework.TestCase;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.crud.Craken;
import net.ion.craken.node.crud.tree.impl.PropertyId;
import net.ion.craken.node.crud.tree.impl.PropertyValue;
import net.ion.craken.node.crud.util.TransactionJobs;
import net.ion.framework.mte.Engine;
import net.ion.framework.util.Debug;
import net.ion.framework.util.MapUtil;

public class TestNavigator extends TestCase{

	public void testEngine() throws Exception {
		Craken craken = Craken.inmemoryCreateWithTest() ;
		Engine engine = Engine.createDefaultEngine() ;
		
		ReadSession session = craken.login("test") ;
		session.tran(TransactionJobs.HelloBleujin) ;
		
		String template= "properties \n"
		+ "${foreach self.toMap() entry \n } ${entry.getKey().idString()} ${entry.getValue().asObject()} ${end}\n"
		+ "children \n"
		+ "${foreach self.parent().children() node \n } ${node} ${end}";
		
//		ReadNode found = session.pathBy("bleujin");
//		for (Entry<PropertyId, PropertyValue> entry : found.toMap().entrySet()) {
//			Debug.line(entry.getKey().idString(), entry.getValue().asObject()) ;
//		}
		
		String result = engine.transform(template, MapUtil.<String, Object>create("self", session.pathBy("bleujin"))) ;
		Debug.line(result);
	}
}
