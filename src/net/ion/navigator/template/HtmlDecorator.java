package net.ion.navigator.template;

import net.ion.craken.node.ReadNode;
import net.ion.framework.util.StringUtil;

public class HtmlDecorator {

    ReadNode node = null;

    final static String BASIC_TAG_TPL = "<%s craken-path=\"%s.%s\" craken-script=\"ctag.%s(%s)\"%s>%s</%s>";
    final static String IMG_TAG_TPL = "<img src=\"%s\" craken-path=\"%s.%s\" craken-script=\"ctag.img(%s)\"%s>";


    public static HtmlDecorator create(ReadNode node) {
        HtmlDecorator _this = new HtmlDecorator();
        _this.node = node;

        return _this;
    }

    public String span(String name) {
        return spanAttr(name, "");
    }

    public String spanAttr(String name, String extraAttr) {

        String padding = StringUtil.isNotEmpty(extraAttr) ? " " : "";

        return String.format(BASIC_TAG_TPL, "span", node.fqn(), name, "span", name, padding + extraAttr, node.property(name).stringValue(), "span");
    }

    public String div(String name) {
        return divAttr(name, "");
    }

    public String divAttr(String name, String extraAttr) {

        String padding = StringUtil.isNotEmpty(extraAttr) ? " " : "";

        return String.format(BASIC_TAG_TPL, "div", node.fqn(), name, "div", name, padding + extraAttr, node.property(name).stringValue(), "div");
    }

    public String img(String name) {
        return imgAttr(name, "");
    }

    public String imgAttr(String name, String extraAttr) {

        String padding = StringUtil.isNotEmpty(extraAttr) ? " " : "";

        return String.format(IMG_TAG_TPL, node.property(name).stringValue(), node.fqn(), name, name, padding + extraAttr);
    }

    public String val(String name) {
        return node.property(name).stringValue();
    }

}
