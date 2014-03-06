package com.engagepoint.plugins;

import com.engagepoint.plugins.drools.io.DefaultFlowSerializer;
import com.engagepoint.plugins.drools.io.FlowSerializer;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.io.ResourceFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FlowSerializerTest {

    private FlowSerializer flowSerializer;

    @Before
    public void before() {
        KnowledgeBuilder knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        knowledgeBuilder.add(ResourceFactory.newClassPathResource("org/drools/examples/process/A/ProcessA.bpmn"),
                ResourceType.BPMN2);
        knowledgeBuilder.add(ResourceFactory.newClassPathResource("org/drools/examples/process/B/ProcessB.bpmn"),
                ResourceType.BPMN2);
        knowledgeBuilder.add(ResourceFactory.newClassPathResource("com/engagepoint/plugins/sample/drools/ruleflow.rf"),
                ResourceType.DRF);
        knowledgeBuilder
                .add(ResourceFactory.newClassPathResource("com/engagepoint/plugins/sample/drools/p1/ruleflow1.rf"),
                        ResourceType.DRF);
        knowledgeBuilder
                .add(ResourceFactory.newClassPathResource("com/engagepoint/plugins/sample/drools/p1/p2/ruleflow2.rf"),
                        ResourceType.DRF);
        knowledgeBuilder.add(ResourceFactory.newClassPathResource("com/engagepoint/plugins/sample/drools/sample.drl"),
                ResourceType.DRL);
        knowledgeBuilder
                .add(ResourceFactory.newClassPathResource("com/engagepoint/plugins/sample/drools/p1/sample1.drl"),
                        ResourceType.DRL);
        knowledgeBuilder
                .add(ResourceFactory.newClassPathResource("com/engagepoint/plugins/sample/drools/p1/p2/sample2.drl"),
                        ResourceType.DRL);

        Collection<KnowledgePackage> packages = knowledgeBuilder.getKnowledgePackages();
        KnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
        knowledgeBase.addKnowledgePackages(packages);

        flowSerializer = new DefaultFlowSerializer(knowledgeBase);
    }

    @Test
    public void test_Bytes() throws Exception {
        byte[] buf = flowSerializer.serializeToByteArray();

        assertNotNull(buf);
        assertTrue(buf.length > 0);

        KnowledgeBase knowledgeBase = flowSerializer.deserialize(buf);
        assertKnowledgeBase(knowledgeBase);
    }

    @Test
    public void test_File() throws Exception {
        File file = flowSerializer.serializeToFile(new File(".").getAbsolutePath() + "/target/output.repository", true);

        assertTrue(file.exists());
        assertTrue(file.length() > 0);

        KnowledgeBase knowledgeBase = flowSerializer.deserialize(file);
        assertKnowledgeBase(knowledgeBase);
    }

    private void assertKnowledgeBase(KnowledgeBase knowledgeBase) {
        Collection<KnowledgePackage> packages = knowledgeBase.getKnowledgePackages();
        assertEquals(4, packages.size());

        assertNotNull(knowledgeBase.getProcess(TestConstants.PROCESS_1));
        assertNotNull(knowledgeBase.getProcess(TestConstants.PROCESS_2));
        assertNotNull(knowledgeBase.getProcess(TestConstants.PROCESS_3));
        assertNotNull(knowledgeBase.getProcess(TestConstants.PROCESS_4));

        assertNotNull(knowledgeBase.getKnowledgePackage(TestConstants.PACKAGE_1));
        assertNotNull(knowledgeBase.getKnowledgePackage(TestConstants.PACKAGE_2));
        assertNotNull(knowledgeBase.getKnowledgePackage(TestConstants.PACKAGE_3));
    }
}
