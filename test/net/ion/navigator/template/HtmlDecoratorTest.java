package net.ion.navigator.template;

import net.ion.craken.node.ReadNode;
import net.ion.framework.mte.Engine;
import net.ion.framework.util.Debug;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.MapUtil;
import net.ion.navigator.TestCrakenBase;

public class HtmlDecoratorTest extends TestCrakenBase {

    public void testFirst() throws Exception {
        // given
        addNode("/test", MapUtil.chainKeyMap().put("name", "airkjh").toMap());
        ReadNode node = session.pathBy("/test");

        // when
        HtmlDecorator decorator = HtmlDecorator.create(node);

        // then
        assertEquals("<span craken-path=\"/test.name\" craken-script=\"ctag.span(name)\" >airkjh</span>", decorator.span("name"));
    }

    public void testSpanWithTpl() throws Exception {
        // given
        addNode("/test", MapUtil.chainKeyMap().put("name", "airkjh").toMap());
        ReadNode node = session.pathBy("/test");

        String tpl = "<div>${ctag.span(\"name\")}</div>";
        Engine engine = Engine.createDefaultEngine();

        // when

        HtmlDecorator decorator = HtmlDecorator.create(node);
        String transformed = engine.transform(tpl, MapUtil.chainKeyMap().put("ctag", decorator).toMap());

        // then
        assertEquals("<div><span craken-path=\"/test.name\" craken-script=\"ctag.span(name)\" >airkjh</span></div>", transformed);
    }

//    public void testSpanWithAttr() throws Exception {
//        // given
//        addNode("/test", MapUtil.chainKeyMap().put("name", "airkjh").toMap());
//        ReadNode node = session.pathBy("/test");
//
//        String tpl = "<div>${ctag.span(name,\"class='test'\")}</div>";
//        Engine engine = Engine.createDefaultEngine();
//
//        // when
//
//        String transformed = engine.transform(tpl, MapUtil.chainKeyMap().put("ctag", decorator).toMap());
//        HtmlDecorator decorator = HtmlDecorator.create(node);
//
//        // then
//        assertEquals("<div><span craken-path=\"/test.name\" craken-script=\"ctag.name\" hahaha>airkjh</span></div>", transformed);
//    }

    public void testDiv() throws Exception {
        // given
        addNode("/test", MapUtil.chainKeyMap().put("name", "airkjh").toMap());
        ReadNode node = session.pathBy("/test");
        String tpl = "<div>${ctag.div(\"name\")}</div>";
        Engine engine = Engine.createDefaultEngine();

        // when

        HtmlDecorator decorator = HtmlDecorator.create(node);
        String transformed = engine.transform(tpl, MapUtil.chainKeyMap().put("ctag", decorator).toMap());

        // then
        assertEquals("<div><div craken-path=\"/test.name\" craken-script=\"ctag.div(name)\" >airkjh</div></div>", transformed);
    }

    public void testImg() throws Exception {
        // given
        addNode("/test", MapUtil.chainKeyMap().put("link", "img_link").toMap());
        ReadNode node = session.pathBy("/test");
        String tpl = "<div>${ctag.img(\"link\")}</div>";
        Engine engine = Engine.createDefaultEngine();

        // when

        HtmlDecorator decorator = HtmlDecorator.create(node);
        String transformed = engine.transform(tpl, MapUtil.chainKeyMap().put("ctag", decorator).toMap());

        // then
        assertEquals("<div><img src=\"img_link\" craken-path=\"/test.link\" craken-script=\"ctag.img(link)\" ></div>", transformed);
    }

    public void testJustValue() throws Exception {
        // given
        addNode("/test", MapUtil.chainKeyMap().put("link", "img_link").toMap());
        ReadNode node = session.pathBy("/test");
        String tpl = "<div>${ctag.val(\"link\")}</div>";
        Engine engine = Engine.createDefaultEngine();

        // when

        HtmlDecorator decorator = HtmlDecorator.create(node);
        String transformed = engine.transform(tpl, MapUtil.chainKeyMap().put("ctag", decorator).toMap());

        // then
        assertEquals("<div>img_link</div>", transformed);
    }

    public void testWithRealTemplate() throws Exception {
        // given
        addNode("/narkssos", MapUtil.chainKeyMap().put("description", "한 때 미남").put("name", "narkssos").put("profile_link", "img_link").toMap());
        ReadNode node = session.pathBy("/narkssos");
        String tpl = IOUtil.toStringWithClose(this.getClass().getResourceAsStream("test_template.html"));
        Engine engine = Engine.createDefaultEngine();

        // when

        HtmlDecorator decorator = HtmlDecorator.create(node);
        String transformed = engine.transform(tpl, MapUtil.chainKeyMap().put("self", node).put("ctag", decorator).toMap());

        // then
        Debug.line(transformed);

    }
}