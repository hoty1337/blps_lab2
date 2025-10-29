package com.djeno.api;

import com.djeno.model.AnalysisReport;
import jakarta.resource.ResourceException;

public interface SpotBugsConnection extends AutoCloseable {

    AnalysisReport analyze(byte[] data);

    @Override
    void close() throws ResourceException;
}