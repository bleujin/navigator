package net.ion.navigator.web.util;

import net.ion.craken.node.ReadNode;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.StringUtil;
import net.ion.nradon.restlet.MediaType;
import net.ion.nradon.restlet.representation.Representation;
import net.ion.nradon.restlet.representation.StringRepresentation;

public class JsonpTransformer implements Transformer {

    @Override
    public Representation transform(String callback, ReadNode node) {

        if(StringUtil.isEmpty(callback)) {
            throw new IllegalArgumentException("callback is not specified for jsonp request");
        }

        JsonObject transformed = JsonTransformer.jsonTransformer.apply(node);
        StringRepresentation rep = new StringRepresentation(callback + "(" + transformed.toString() + ")");
        rep.setMediaType(MediaType.APPLICATION_JSON);

        return rep;
    }
}
