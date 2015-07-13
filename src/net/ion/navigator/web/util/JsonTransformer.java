package net.ion.navigator.web.util;

import java.util.Iterator;

import net.ion.craken.node.IteratorList;
import net.ion.craken.node.ReadNode;
import net.ion.craken.node.crud.tree.impl.PropertyId;
import net.ion.craken.node.crud.tree.impl.PropertyValue;
import net.ion.framework.parse.gson.JsonArray;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.StringUtil;
import net.ion.nradon.restlet.representation.JsonObjectRepresentation;
import net.ion.nradon.restlet.representation.Representation;

import com.google.common.base.Function;

public class JsonTransformer implements Transformer {

    public static Function<ReadNode, JsonObject> jsonTransformer = new Function<ReadNode, JsonObject>() {

        @Override
        public JsonObject apply(ReadNode node) {
            return transformToJson(node);
        }

        private JsonObject transformToJson(ReadNode node) {
            JsonObject json = new JsonObject();

            meta(node, json);
            properties(node, json);
            children(node, json);

            return json;
        }

        private void meta(ReadNode node, JsonObject json) {
            String nodeId = "/".equals(node.id().toString()) ? "root" : node.id().toString();
            String nodeName = "/".equals(node.id().toString()) ? "/" : StringUtil.substringAfterLast(node.id().toString(), "/");

            json.put("id", nodeId);
            json.put("name", nodeName);
            json.put("path", node.id().toString());
            json.put("properties", new JsonObject());
            json.put("childCount", node.childrenNames().size());
            json.put("children", new JsonArray());
        }

        private JsonObject properties(ReadNode node, JsonObject json) {
            Iterator<PropertyId> iterator = node.keys().iterator();
            JsonObject properties = json.getAsJsonObject("properties");

            while (iterator.hasNext()) {
                PropertyId propertyId = iterator.next();
                PropertyValue pValue = node.propertyId(propertyId);

                put(node, properties, propertyId.idString(), pValue);
            }

            return properties;
        }

        private void put(ReadNode node, JsonObject json, String propertyId, PropertyValue propertyValue) {
            // Concern: Should I handle references here?

            if (propertyValue.isBlob()) {
                json.put(propertyId, "BlOB Property");
            } else if (propertyValue.asSet().size() > 1) {
                json.put(propertyId, propertyValue.asJsonArray());
            } else {
                json.put(propertyId, propertyValue.value());
            }
        }

        private void children(ReadNode node, JsonObject json) {
            IteratorList<ReadNode> iter = node.children().iterator();
            JsonArray children = json.getAsJsonArray("children");

            while (iter.hasNext()) {
                ReadNode child = iter.next();
                if (node.fqn().equals(child.fqn())) continue ;
                
                JsonObject childJson = transformToJson(child);

//                childJson.put("relName", "child");

                children.add(childJson);
            }
        }
    };

    @Override
    public Representation transform(String callback, ReadNode node) {
        return new JsonObjectRepresentation(jsonTransformer.apply(node));
    }
}
