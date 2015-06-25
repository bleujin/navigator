package net.ion.navigator.web;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.spi.HttpResponse;

import net.ion.framework.util.StringUtil;
import net.ion.nradon.HttpControl;
import net.ion.nradon.HttpRequest;
import net.ion.nradon.handler.AbstractHttpHandler;
import net.ion.nradon.restlet.Header;
import net.ion.nradon.restlet.MediaType;
import net.ion.nradon.restlet.Method;
import net.ion.nradon.restlet.Series;
import net.ion.radon.core.IService;

public class HeaderHandler extends AbstractHttpHandler{

	@Override
	public void handleHttpRequest(HttpRequest req, net.ion.nradon.HttpResponse res, HttpControl control) throws Exception {
		if (req.uri().startsWith("/app")) {
			control.nextHandler();
			return ;
		}
		
		String callback = req.queryParam("callback") ;
		req.data("requestType", StringUtil.isEmpty(callback) ? "json" : "jsonp");
		
		res.header("Allow", "GET, PUT, POST, DELETE");
        res.header("Access-Control-Allow-Origin", "*");
        res.header("Access-Control-Allow-Headers", MediaType.APPLICATION_JSON.getName());
        res.header("Cache-Control", "private, no-store, no-cache, must-revalidate");
        res.header("Pragma", "no-cache");
        control.nextHandler();
	}
}
