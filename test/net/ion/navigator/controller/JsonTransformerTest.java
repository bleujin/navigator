package net.ion.navigator.controller;

import java.util.Map;

import net.ion.craken.node.ReadNode;
import net.ion.framework.parse.gson.JsonArray;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.MapUtil;
import net.ion.navigator.TestCrakenBase;
import net.ion.navigator.web.util.JsonTransformer;
import net.ion.nradon.restlet.representation.JsonObjectRepresentation;

public class JsonTransformerTest extends TestCrakenBase {

    private JsonTransformer jsonTransformer = new JsonTransformer();

    public void testFirst() throws Exception {
        // given
        Map<String, Object> props = MapUtil.chainKeyMap().put("age", 30).put("isMe", true).toMap();
        addNode("/airkjh", props);

        ReadNode node = session.pathBy("/airkjh");
        JsonObject result = ((JsonObjectRepresentation)jsonTransformer.transform(null, node)).getJsonObject();

        assertEquals("/airkjh", result.get("id").getAsString());
        assertEquals("airkjh", result.get("name").getAsString());

        JsonObject properties = result.get("properties").getAsJsonObject();
        assertEquals(30, properties.get("age").getAsInt());
        assertEquals(true, properties.get("isMe").getAsBoolean());

        assertEquals(0, result.get("children").getAsJsonArray().size());
    }

    public void testChildren() throws Exception {
        // given
        Map<String, Object> props = MapUtil.EMPTY;
        addNode("/airkjh", props);
        addNode("/airkjh/child1", props);
        addNode("/airkjh/child2", props);

        // when
        ReadNode node = session.pathBy("/airkjh");
        JsonObject json = ((JsonObjectRepresentation)jsonTransformer.transform(null, node)).getJsonObject();

        // then
        JsonArray children = json.get("children").getAsJsonArray();
        assertEquals(2, children.size());

        JsonObject child1 = children.get(0).getAsJsonObject();
        JsonObject child2 = children.get(1).getAsJsonObject();

        assertEquals("/airkjh/child1", child1.get("id").getAsString());
        assertEquals("/airkjh/child2", child2.get("id").getAsString());

        assertEquals("child", child1.get("relName").getAsString());
        assertEquals("child", child2.get("relName").getAsString());
    }

//    public void testRefs() throws Exception {
//        // given
//        addNode("/airkjh", MapUtil.EMPTY);
//        addNode("/airkjh/child1", MapUtil.EMPTY);
//        addNode("/bleujin", MapUtil.EMPTY);
//
//        ref("/airkjh", "/bleujin", "boss");
//
//        // when
//        JsonObject result = session.pathBy("/airkjh").transformer(jsonTransformer);
//
//        // then
//        System.out.println(result.toString());
//    }

//    private void ref(final String src, final String dest, final String refName) throws Exception {
//        session.tranSync(new TransactionJob<Void>() {
//
//            @Override
//            public Void handle(WriteSession wsession) throws Exception {
//                wsession.pathBy(src).refTo(refName, dest);
//                return null;
//            }
//        });
//    }

}
