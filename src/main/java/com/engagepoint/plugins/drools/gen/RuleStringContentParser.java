package com.engagepoint.plugins.drools.gen;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleStringContentParser {

    private static final String REVISION_REGEXP = "^(.*\\$Revision:\\s*)(\\d+)(\\s*\\$.*)";
    private static final String MODIFY_DATE_REGEXP = "^(.*\\$Date:\\s*)(.*)(\\s*\\$.*)";

    private final Pattern revisionPattern = Pattern.compile(REVISION_REGEXP);
    private final Pattern modifyDatePattern = Pattern.compile(MODIFY_DATE_REGEXP);

    public int getRevisionFromString(String content) {
        String str = getStringByPattern(content, revisionPattern);
        if (str != null) {
            try {
                return Integer.valueOf(str);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return 0;
    }

    public String getModifyDateFromString(String content) {
        String res = getStringByPattern(content, modifyDatePattern);
        if (res.contains("(")) {
            res = res.substring(0, res.indexOf('(')).trim();
        }
        return res;
    }

    public String getStringByPattern(String content, Pattern pattern) {
        String[] lines = content.split(System.getProperty("line.separator", "\n"));
        for (String line : lines) {
            try {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String result = matcher.group(2);
                    if (StringUtils.isNotBlank(result)) {
                        return result;
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return null;
    }
}
