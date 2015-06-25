package net.ion.navigator;

import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;
import net.ion.craken.node.ReadSession;
import net.ion.craken.node.TransactionJob;
import net.ion.craken.node.WriteNode;
import net.ion.craken.node.WriteSession;
import net.ion.craken.node.crud.Craken;

public class TestCrakenBase extends TestCase {

    protected Craken r;
    protected ReadSession session;

    protected final String WS_NAME = "test";

    @Override
    public void setUp() throws Exception {
        this.r = Craken.inmemoryCreateWithTest() ; // pre define "test" ;

        r.start();
        this.session = r.login(WS_NAME);
    }


    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        r.shutdown();
    }

    protected void addNode(final String path, final Map<String, Object> properties) throws Exception {
        session.tranSync(new TransactionJob<Void>() {
            @Override
            public Void handle(WriteSession wsession) throws Exception {
                WriteNode wnode = wsession.pathBy(path);

                Iterator<String> iterator = properties.keySet().iterator();
                while(iterator.hasNext()) {
                    String key = iterator.next();
                    Object value = properties.get(key);

                    wnode.property(key, value);
                }

                return null;
            }
        });
    }


}
