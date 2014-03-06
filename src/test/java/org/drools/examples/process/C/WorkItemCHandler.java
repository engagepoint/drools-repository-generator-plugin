package org.drools.examples.process.C;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;

import java.util.HashMap;
import java.util.Map;

public class WorkItemCHandler implements WorkItemHandler {

    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        String msg = (String) workItem.getParameter("msg");
        System.out.println(msg);
        Map<String, Object> results = new HashMap<String, Object>();
        manager.completeWorkItem(workItem.getId(), results);
    }

    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
    }

}
