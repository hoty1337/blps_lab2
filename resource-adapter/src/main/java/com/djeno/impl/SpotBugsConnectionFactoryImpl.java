package com.djeno.impl;

import com.djeno.api.SpotBugsConnection;
import com.djeno.api.SpotBugsConnectionFactory;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;

import javax.naming.NamingException;
import javax.naming.Reference;

public class SpotBugsConnectionFactoryImpl implements SpotBugsConnectionFactory {

    private final SpotBugsManagedConnectionFactory mcf;
    private final ConnectionManager cm;
    private Reference mcRef;

    public SpotBugsConnectionFactoryImpl(SpotBugsManagedConnectionFactory mcf, ConnectionManager cm) {
        this.mcf = mcf;
        this.cm = cm;
    }

    public SpotBugsConnection getConnection() throws ResourceException {
        return (SpotBugsConnection) cm.allocateConnection(mcf, null);
    }

    @Override
    public void setReference(Reference reference) {
        mcRef = reference;
    }

    @Override
    public Reference getReference() throws NamingException {
        return null;
    }
}