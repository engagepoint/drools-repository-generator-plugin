package com.engagepoint.plugins.drools.gen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KnowledgeProcessingException extends RuntimeException {

    private List<String> errors = new ArrayList<String>();

    public KnowledgeProcessingException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
