package com.engagepoint.plugins.drools.io;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.definition.KnowledgePackage;

import java.io.*;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultFlowSerializer implements FlowSerializer {

    private static final Logger LOGGER = Logger.getLogger(FlowSerializer.class.getSimpleName());

    private KnowledgeBase knowledgeBase;

    public DefaultFlowSerializer(KnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    private void serialize(OutputStream outputStream) throws Exception {
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(knowledgeBase.getKnowledgePackages());
        oos.close();
    }

    public File serializeToFile(String filePath, boolean overwrite) throws SerializationException {
        File f = new File(filePath);

        if (f.exists() && !overwrite) {
            LOGGER.log(Level.WARNING, "Target file exists and will not be overwritten " + filePath);
            return f;
        }

        if (f.exists() && !f.delete()) {
            throw new SerializationException("Unable to overwrite target file " + filePath);
        }

        try {
            f.createNewFile();
            FileOutputStream stream = new FileOutputStream(f);
            serialize(stream);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
        return f;
    }

    public byte[] serializeToByteArray() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serialize(baos);
        return baos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    private KnowledgeBase deserialize(InputStream inputStream) throws Exception {
        KnowledgeBase kBase = KnowledgeBaseFactory.newKnowledgeBase();
        ObjectInputStream ois = new ObjectInputStream(inputStream);
        Collection<KnowledgePackage> packages = (Collection<KnowledgePackage>) ois.readObject();
        ois.close();
        kBase.addKnowledgePackages(packages);
        return kBase;
    }

    public KnowledgeBase deserialize(File file) throws Exception {
        knowledgeBase = deserialize(new FileInputStream(file));
        return knowledgeBase;
    }

    public KnowledgeBase deserialize(byte[] input) throws Exception {
        knowledgeBase = deserialize(new ByteArrayInputStream(input));
        return knowledgeBase;
    }
}
