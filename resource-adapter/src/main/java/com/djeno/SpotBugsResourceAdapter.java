package com.djeno;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;

import java.io.Serializable;
import java.util.Objects;
import javax.transaction.xa.XAResource;

@Connector(
        displayName = "SpotBugs Resource Adapter",
        vendorName = "ITMO",
        eisType = "Static Analyzer",
        version = "1.0"
)
public class SpotBugsResourceAdapter implements ResourceAdapter, Serializable {

    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private transient BootstrapContext bootstrapContext;

    @Override
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
        this.bootstrapContext = ctx;
    }

    @Override
    public void stop() {
        // Nothing to clean up
    }

    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec)
            throws ResourceException {
        // Not used for outbound communication
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
        // Not used for outbound communication
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        // No XA transaction support
        return new XAResource[0];
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(bootstrapContext);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SpotBugsResourceAdapter other) {
            return Objects.equals(bootstrapContext, other.bootstrapContext);
        }
        return false;
    }
}