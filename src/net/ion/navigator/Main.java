package net.ion.navigator;

import net.ion.craken.node.crud.Craken;
import net.ion.craken.node.crud.store.WorkspaceConfigBuilder;
import net.ion.navigator.entry.RepoEntry;
import net.ion.navigator.web.HeaderHandler;
import net.ion.navigator.web.NavigatorLet;
import net.ion.nradon.HttpControl;
import net.ion.nradon.HttpRequest;
import net.ion.nradon.HttpResponse;
import net.ion.nradon.Radon;
import net.ion.nradon.config.RadonConfiguration;
import net.ion.nradon.config.RadonConfigurationBuilder;
import net.ion.nradon.handler.AbstractHttpHandler;
import net.ion.nradon.handler.logging.LoggingHandler;
import net.ion.nradon.handler.logging.SimpleLogSink;
import net.ion.radon.core.let.PathHandler;

public class Main {


	public static void main(String[] args) throws Exception {

		RadonConfigurationBuilder rconfig = RadonConfiguration.newBuilder(9000)
				.add(new LoggingHandler(new SimpleLogSink()))
				.add(new HeaderHandler())
				.add(new PathHandler(NavigatorLet.class))
				.add(new AbstractHttpHandler() {
					@Override
					public void handleHttpRequest(HttpRequest req, HttpResponse res, HttpControl arg2) throws Exception {
						res.status(404).content("not found path : " + req.uri()).end() ;
					}
				});

		Craken craken = Craken.local();
		craken.createWorkspace("test", WorkspaceConfigBuilder.gridDir("./resource/store"));
		craken.start();
		rconfig.context(RepoEntry.EntryID, new RepoEntry(craken, "test"));

		Radon radon = rconfig.start().get();

	}

}
