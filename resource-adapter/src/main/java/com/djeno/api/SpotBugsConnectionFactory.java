package com.djeno.api;

import java.io.Serializable;

import jakarta.resource.Referenceable;
import jakarta.resource.ResourceException;

public interface SpotBugsConnectionFactory extends Serializable, Referenceable{
    /**
     * Get connection from factory
     *
     * @return HelloWorldConnection instance
     * @exception ResourceException Thrown if a connection can't be obtained
     */
    SpotBugsConnection getConnection() throws ResourceException;
}