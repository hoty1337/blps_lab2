package com.djeno.impl;

import com.djeno.api.SpotBugsConnection;
import com.djeno.model.AnalysisReport;
import com.djeno.service.AnalyzerClient;
import jakarta.resource.ResourceException;

public class SpotBugsConnectionImpl implements SpotBugsConnection {
    private SpotBugsManagedConnection mc;
    private AnalyzerClient client;

    public SpotBugsConnectionImpl(SpotBugsManagedConnection mc) {
        this.mc = mc;
        this.client = new AnalyzerClient();
    }

    @Override
    public AnalysisReport analyze(byte[] data) {// на всякий случай
        return client.analyze(data); // внутри вызовется FindBugs2
    }

    @Override
    public void close() throws ResourceException {
        mc.closeHandle(this);
    }
}