package com.djeno.impl;

import java.io.PrintWriter;
import java.util.Set;
import java.util.UUID;

import com.djeno.api.SpotBugsConnection;
import com.djeno.api.SpotBugsConnectionFactory;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionDefinition;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterAssociation;
import javax.security.auth.Subject;

@ConnectionDefinition(connectionFactory = SpotBugsConnectionFactory.class,
        connectionFactoryImpl = SpotBugsConnectionFactoryImpl.class,
        connection = SpotBugsConnection.class, connectionImpl = SpotBugsConnectionImpl.class)
public class SpotBugsManagedConnectionFactory
        implements ManagedConnectionFactory, ResourceAdapterAssociation {
    private static final long serialVersionUID = 1L;

    private final UUID id;

    private ResourceAdapter ra;
    private PrintWriter logwriter;

    public SpotBugsManagedConnectionFactory() {
        id = UUID.randomUUID();
    }

    @Override
    public ResourceAdapter getResourceAdapter() {
        return ra;
    }

    @Override
    public void setResourceAdapter(ResourceAdapter ra) {
        this.ra = ra;
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        throw new ResourceException(
                "This resource adapter doesn't support non-managed environments");
    }

    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        return new SpotBugsConnectionFactoryImpl(this, cxManager);
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject,
                                                     ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        return new SpotBugsManagedConnection(this);
    }

    @Override
    public ManagedConnection matchManagedConnections(
            @SuppressWarnings("rawtypes") Set connectionSet, Subject subject,
            ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        @SuppressWarnings("unchecked")
        Set<ManagedConnection> managedConnectionSet = connectionSet;
        for (ManagedConnection mc : managedConnectionSet) {
            if (mc instanceof SpotBugsManagedConnection) {
                return mc;
            }
        }
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        logwriter = out;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return logwriter;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SpotBugsManagedConnectionFactory mcf) {
            return id.equals(mcf.id);
        }
        return false;
    }
}