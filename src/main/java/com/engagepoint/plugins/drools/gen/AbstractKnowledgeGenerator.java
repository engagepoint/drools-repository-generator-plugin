package com.engagepoint.plugins.drools.gen;

import org.apache.commons.io.IOUtils;
import org.drools.io.Resource;
import org.drools.repository.AssetItem;
import org.drools.repository.PackageItem;

abstract class AbstractKnowledgeGenerator {

    protected void addAsset(PackageItem packageItem, String name, String description, String format, Resource content,
            String comment) throws Exception {
        addAsset(packageItem, name, description, format, IOUtils.toString(content.getInputStream(), "UTF-8"), comment);
    }

    protected void addAsset(PackageItem packageItem, String name, String description, String format, String content,
            String comment) throws Exception {
        name = name.replaceAll("\\.", "_");
        AssetItem assetItem = packageItem.addAsset(name, description);
        assetItem.updateFormat(format);
        assetItem.updateContent(content);
        assetItem.checkin(comment);
    }
}
