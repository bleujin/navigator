package net.ion.navigator.web;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.commons.lang.StringUtils;

import net.ion.craken.node.ReadNode;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteNode;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.tree.impl.PropertyId;
import net.ion.craken.node.crud.tree.impl.PropertyValue;
import net.ion.framework.mte.Engine;
import net.ion.framework.parse.gson.JsonElement;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.parse.gson.JsonParser;
import net.ion.framework.parse.gson.JsonPrimitive;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.StringUtil;
import net.ion.navigator.entry.RepoEntry;
import net.ion.navigator.template.HtmlDecorator;
import net.ion.navigator.web.util.JsonTransformer;
import net.ion.navigator.web.util.JsonpTransformer;
import net.ion.navigator.web.util.Transformer;
import net.ion.nradon.restlet.FileMetaType;
import net.ion.nradon.restlet.Language;
import net.ion.nradon.restlet.MediaType;
import net.ion.nradon.restlet.representation.FileRepresentation;
import net.ion.nradon.restlet.representation.Representation;
import net.ion.nradon.restlet.representation.StringRepresentation;
import net.ion.nradon.restlet.util.IoUtils;
import net.ion.radon.core.ContextParam;

import com.google.common.base.Objects;

@Path("")
public class NavigatorLet {

	private Engine engine ;
	private static File BASE_DIR = new File("./webapp") ;
	private static Map<String, Transformer> transformers = MapUtil.<Transformer> chainKeyMap().put("json", new JsonTransformer()).put("jsonp", new JsonpTransformer()).toMap();
	
	
	private ReadSession session;
	public NavigatorLet(@ContextParam(RepoEntry.EntryID) RepoEntry entry) throws IOException{
		this.session = entry.login() ;
		this.engine = session.workspace().parseEngine() ;
	}

	
    @GET
    @Path("/app/{remain: [A-Za-z0-9-_\\.\\/]*}")
    public FileRepresentation viewFile(@PathParam("remain") String path) {

        String ext = StringUtil.substringAfterLast(path, ".");

        if(StringUtil.isEmpty(path) && StringUtil.isEmpty(ext)) {
            path = "index.html";
            ext = "html";
        }

        return new FileRepresentation(new File(BASE_DIR, path), FileMetaType.mediaType2(path));
    }

	

    @GET
    @Path("/preview/{remain: [A-Za-z0-9-_\\.\\/]*}")
    public StringRepresentation view(@PathParam("remain") String path) throws IOException {

        String serverAddr = String.format("/cview/%s", path);

        String tpl = IOUtil.toStringWithClose(new FileInputStream(new File(BASE_DIR, "preview.html"))) ;
        String html = engine.transform(tpl, MapUtil.chainKeyMap().put("link", serverAddr).toMap());

        return new StringRepresentation(html, MediaType.TEXT_HTML, Language.valueOf("UTF-8"));
    }
    
    
	
    @POST
    @Path("/node/{remain: [A-Za-z0-9-_\\.\\/]*}")
    public String createNode(@PathParam("remain") final String path) {
        session.tran(new TransactionJob<Void>() {
            @Override
            public Void handle(WriteSession wsession) throws Exception {
                wsession.pathBy(path);
                return null;
            }
        });

        return "success";
    }

    @DELETE
    @Path("/node/{remain: [A-Za-z0-9-_\\.\\/]*}")
    public void deleteNode(@PathParam("remain") final String path) {
    	session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy(path).removeSelf() ;
				return null;
			}
		}) ;
    }
    
    
    
    
    
    @GET
    @Path("/value/{remain: [A-Za-z0-9-_\\.\\/]*}")
    public Representation viewPropertyValue(@PathParam("remain") String path) throws IOException {
        ValueRequest req = ValueRequest.create(path);
        byte[] resource = firstProperty(req.nodePath, req.propertyId);
        return new ByteArrayRepresentation(resource, req.mediaType);
    }

    private byte[] firstProperty(String nodePath, String propertyId) throws IOException {
        ReadNode node = session.pathBy(nodePath);
        if (!node.hasProperty(propertyId)) {
            throw new IllegalArgumentException("Property not exist : " + propertyId);
        }

        PropertyValue pvalue = node.property(propertyId);
        if(pvalue.isBlob()) {
            return IOUtil.toByteArrayWithClose(pvalue.asBlob().toInputStream());
        } else {
            return pvalue.stringValue().getBytes("UTF-8");
        }
    }

    
    
    

	@GET
	@Path("/property/{remain: [A-Za-z0-9-_\\.\\/]*}")
	public StringRepresentation viewProperty(@PathParam("remain") String remain) throws IOException {
		PropertyRequest parsed = PropertyRequest.create(remain);

		PropertyValue property = session.pathBy(parsed.nodePath).property(parsed.propertyId);
		if (property.isBlob()) {
			return new StringRepresentation("Editing BLOB property is not allowed", MediaType.TEXT_HTML);
		}

		String viewTpl =  IOUtil.toStringWithClose(new FileInputStream(new File(BASE_DIR, "property.html"))) ;  
		String transformed = session.workspace().parseEngine().transform(viewTpl, MapUtil.chainKeyMap().put("value", property.stringValue()).toMap());
		return new StringRepresentation(transformed, MediaType.TEXT_HTML, Language.valueOf("UTF-8"));
	}

	@POST
	@Path("/property/{remain: [A-Za-z0-9-_\\.\\/]*}")
	public String update(@PathParam("remain") String remain, @FormParam("_value") String value) throws Exception {

		final PropertyRequest propertyRequest = PropertyRequest.create(remain);
		ReadNode requestedNode = session.pathBy(propertyRequest.nodePath);

		final ReadNode propOwnerNode = findOwner(session, requestedNode, propertyRequest.propertyId);
		final Object castedValue = castToOriginalValueType(propOwnerNode, propertyRequest.propertyId, value);

		session.tranSync(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				wsession.pathBy(propOwnerNode.fqn()).property(propertyRequest.propertyId, castedValue);
				return null;
			}
		});

		return "sucesss";
	}

	private ReadNode findOwner(ReadSession session, ReadNode node, String propName) {

		if (isParentOfRoot(session, node)) {
			return session.pathBy("/__" + propName);
		}

		if (node.hasProperty(propName)) {
			return node;
		} else if (node.hasRef(propName)) {
			return findOwner(session, node.ref(propName), propName);
		} else {
			return findOwner(session, node.parent(), propName);
		}
	}

	private boolean isParentOfRoot(ReadSession session, ReadNode node) {
		return node.equals(session.root()) && node.parent().equals(session.root());
	}

	private Object castToOriginalValueType(ReadNode owner, String propertyId, String value) {
		PropertyValue.VType type = owner.property(propertyId).type();

		if (PropertyValue.VType.BLOB.equals(type)) {
			throw new IllegalArgumentException("Blob property is not modifiable");
		}

		return type.read(new JsonPrimitive(value));
	}

	private static class PropertyRequest {
		String nodePath;
		String propertyId;

		public static PropertyRequest create(String remainingPart) {
			PropertyRequest propertyRequest = new PropertyRequest();
			String url = StringUtil.removeEnd(remainingPart, "?");

			propertyRequest.nodePath = StringUtil.substringBeforeLast(url, ".");
			propertyRequest.propertyId = StringUtil.substringAfterLast(url, ".");
			return propertyRequest;
		}
	}
    
    
	
	
	@GET
    @Path("/cview/{remain: [A-Za-z0-9-_\\.\\/]*}")
    public StringRepresentation transform(@PathParam("remain") String remainPath) throws IOException {

        String nodePath = StringUtil.substringBeforeLast(remainPath, ".");
        String propName = StringUtil.substringAfterLast(remainPath, ".");

        ReadNode node = session.ghostBy(nodePath);
        String template = engineTemplate(session, node, propName);

        Map<String, Object> props = props(node);
        String transformed = session.workspace().parseEngine().transform(template, props);
        return new StringRepresentation(transformed, MediaType.TEXT_HTML, Language.valueOf("UTF-8"));
    }

    private Map<String, Object> props(ReadNode node) {

        Map<String, Object> props = MapUtil.newMap();
        props.put("self", node);
        props.put("ctag", HtmlDecorator.create(node));

        return props;
    }

    private String engineTemplate(ReadSession session, ReadNode node, String propName) {
    	
        if(node.fqn().isRoot()) {
            return    "properties \n"
            		+ "${foreach self.toMap() entry \n } ${entry.getKey().idString()} ${entry.getValue().asObject()} ${end} \n\n<br/><br/>"
            		+ "children \n"
            		+ "${foreach self.children() node \n } ${node} ${end}";
        }

        if(node.hasProperty(propName)) {
            return node.property(propName).stringValue();
        } else if(node.hasRef(propName)) {
            return node.ref(propName).property(propName).stringValue();
        } else {
            return engineTemplate(session, node.parent(), propName);
        }
    }

	
    
    

	@GET
	@Path("/properties/")
	public Representation pathByRoot(@FormParam("callback") String callback) throws IOException{
		return pathBy("", callback) ;
	}
	
	@GET
	@Path("/properties/{remain: [A-Za-z0-9-_\\.\\/]*}")
	public Representation pathBy(@PathParam("remain") String path, @FormParam("callback") String callback) throws IOException {
		String reqType = StringUtil.isEmpty(callback) ? "json" : "jsonp" ;

		ReadNode node = session.pathBy(path);
		return transformers.get(reqType).transform(callback, node);
	}

	@POST
	@Path("/properties/{remain: [A-Za-z0-9-_\\.\\/]*}")
	public String upsert(@FormParam("body") String body, @PathParam("remain") String path) throws Exception {
		upsertNode(path, body);
		return "success";
	}

	private void upsertNode(final String nodePath, String body) {
		final JsonObject json = JsonParser.fromString(body).getAsJsonObject();

		session.tran(new TransactionJob<Void>() {
			@Override
			public Void handle(WriteSession wsession) throws Exception {
				WriteNode node = wsession.pathBy(nodePath);
				updateAll(node, json);
				return null;
			}

			private void updateAll(WriteNode node, JsonObject json) {
				unsetRemovedProps(node, json);
				setProperties(node, json);
			}

			private void unsetRemovedProps(WriteNode node, JsonObject json) {
				Set<PropertyId> keys = node.keys();

				for (PropertyId key : keys) {
					String pid = key.idString();

					if (json.get(pid) == null) {
						node.unset(pid);
					}
				}
			}

			private void setProperties(WriteNode node, JsonObject json) {
				for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
					String key = entry.getKey();
					JsonElement value = entry.getValue();

					if ("blob".equalsIgnoreCase(key)) {
						continue;
					}

					setValue(node, key, value);
				}
			}

			private void setValue(WriteNode node, String key, JsonElement value) {
				if (value.isJsonPrimitive()) {
					node.property(key, value.getAsJsonPrimitive().getValue());
				} else if (value.isJsonArray()) {
					node.unset(key);
					node.property(key, value.getAsJsonArray().toObjectArray());
				} else {
					// If value type is not supposed, just consider it as string
					node.property(key, value.toString());
				}
			}
		});
	}

	private String nodePath(String remainPart) {
		String path = StringUtil.substringBeforeLast(remainPart, "?");

		if (StringUtils.isEmpty(path)) {
			return "/";
		}

		return path;
	}
    
    
}



class ValueRequest {

    String nodePath = null;
    MediaType mediaType = null;
    String propertyId = null;

    static ValueRequest create(String remainPath) {

        String requestPath = removeJSONPParams(remainPath);


        ValueRequest req = new ValueRequest();

        req.nodePath = StringUtil.substringBeforeLast(requestPath, "/");

        MediaType mediaType = FileMetaType.mediaType2(requestPath);

        if(mediaType != null) {
            req.mediaType = mediaType;
        } else {
            req.mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        int start = StringUtil.lastIndexOf(requestPath, "/") + 1;
        int end = StringUtil.lastIndexOf(requestPath, ".");

        req.propertyId = StringUtil.substring(requestPath, start, end);

        return req;
    }

    private static String removeJSONPParams(String remainPath) {
        return StringUtil.substringBeforeLast(remainPath, "?");
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("nodePath", nodePath)
                .add("mediaType", mediaType)
                .add("propertyId", propertyId).toString();
    }
}

class ByteArrayRepresentation extends Representation{

	private ByteArrayInputStream input;
	public ByteArrayRepresentation(byte[] bytes, MediaType mtype){
		super(mtype) ;
		this.input = new ByteArrayInputStream(bytes) ;
	}

	@Override
	public java.nio.channels.ReadableByteChannel getChannel() throws IOException {
		return IoUtils.getChannel(getStream());
	}

	@Override
	public Reader getReader() throws IOException {
		return IoUtils.getReader(getStream(), getCharacterSet());
	}

	@Override
	public void write(java.io.Writer writer) throws IOException {
		IOUtil.copy(getReader(), writer) ;
		writer.flush();
	}

	@Override
	public InputStream getStream() throws IOException {
		return input;
	}

	@Override
	public void write(OutputStream output) throws IOException {
		IOUtil.copy(input, output);
		IOUtil.close(input);
	}
	
}
