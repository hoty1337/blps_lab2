package com.djeno.impl;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.djeno.api.SpotBugsConnection;
import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.LocalTransaction;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

public class SpotBugsManagedConnection implements ManagedConnection {
    private SpotBugsManagedConnectionFactory mcf;
    private PrintWriter logWriter;
    private List<ConnectionEventListener> listeners;
    private Object connection;

    public SpotBugsManagedConnection(SpotBugsManagedConnectionFactory mcf) {
        this.mcf = mcf;
        listeners = new ArrayList<>(1);
    }


    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo)
            throws ResourceException {
        try {
            System.out.println("getConnection() called from " + getClass().getClassLoader());
            SpotBugsConnectionImpl handle = new SpotBugsConnectionImpl(this);
            System.out.println("Handle created ok: " + handle);
            return handle;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new ResourceException("Error in getConnection(): " + e, e);
        }
    }

    @Override
    public void destroy() throws ResourceException {
        this.connection = null;
    }

    @Override
    public void cleanup() throws ResourceException {}

    @Override
    public void associateConnection(Object connection) throws ResourceException {
        this.connection = connection;
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener is null");
        }
        listeners.add(listener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener is null");
        }
        listeners.remove(listener);
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        throw new NotSupportedException("GetXAResource not supported");
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        throw new NotSupportedException("LocalTransaction not supported");
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return new SpotBugsManagedConnectionMetaData();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }

    void closeHandle(SpotBugsConnection handle) {
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
        event.setConnectionHandle(handle);
        for (ConnectionEventListener cel : listeners) {
            cel.connectionClosed(event);
        }
    }
}