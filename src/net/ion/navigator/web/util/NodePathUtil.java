package net.ion.navigator.web.util;

import net.ion.framework.util.StringUtil;
import org.apache.commons.lang.StringUtils;

public class NodePathUtil {

    public static String getPath(String remainPart) {
        String path = StringUtil.substringBeforeLast(remainPart, "?");

        if (StringUtils.isEmpty(path)) {
            return "/";
        }

        return path;
    }

}
