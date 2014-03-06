package com.engagepoint.plugins.drools.compilers;

public interface DroolsCompiler {

    void compile(DroolsCompilerConfiguration inputParameters) throws DroolsCompilerException;
}
