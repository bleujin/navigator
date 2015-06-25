package net.ion.navigator.web.util;

import net.ion.craken.node.ReadNode;
import net.ion.nradon.restlet.representation.Representation;

public interface Transformer {

    public Representation transform(String callback, ReadNode node);

}
