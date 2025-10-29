package com.djeno.impl;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnectionMetaData;

/**
 * Метаданные соединения SpotBugs адаптера.
 * Возвращаются контейнеру через ManagedConnection.getMetaData().
 */
public class SpotBugsManagedConnectionMetaData implements ManagedConnectionMetaData {

    @Override
    public String getEISProductName() throws ResourceException {
        return "SpotBugs Analyzer Adapter";
    }

    @Override
    public String getEISProductVersion() throws ResourceException {
        return "1.0";
    }

    @Override
    public int getMaxConnections() throws ResourceException {
        return 0;
    }

    @Override
    public String getUserName() throws ResourceException {
        return "system";
    }
}
