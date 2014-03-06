package com.engagepoint.plugins.drools.compilers;

public class DroolsCompilerException extends Exception {

    public DroolsCompilerException() {
    }

    public DroolsCompilerException(String message) {
        super(message);
    }

    public DroolsCompilerException(String message, Throwable cause) {
        super(message, cause);
    }

    public DroolsCompilerException(Throwable cause) {
        super(cause);
    }
}
