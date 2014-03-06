package com.engagepoint.plugins.drools.io;

import org.drools.KnowledgeBase;

import java.io.File;

public interface FlowSerializer {

    KnowledgeBase deserialize(File file) throws Exception;

    KnowledgeBase deserialize(byte[] input) throws Exception;

    File serializeToFile(String filePath, boolean overwrite) throws SerializationException;

    byte[] serializeToByteArray() throws Exception;
}
