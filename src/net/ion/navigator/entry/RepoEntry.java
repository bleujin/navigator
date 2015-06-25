package net.ion.navigator.entry;

import java.io.IOException;

import net.ion.craken.node.ReadSession;
import net.ion.craken.node.crud.Craken;

public class RepoEntry {

	public final static String EntryID = "RepoEntry" ;
	
	private Craken craken;
	private String wsName;

	public RepoEntry(Craken craken, String wsName){
		this.craken = craken ;
		this.wsName = wsName ;
	}
	
	public ReadSession login() throws IOException{
		return craken.login(wsName) ;
	}
	
}
