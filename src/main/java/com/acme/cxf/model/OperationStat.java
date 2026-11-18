package com.acme.cxf.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "OperationStat", namespace = "http://api.cxf.acme.com/")
@XmlType(namespace = "http://api.cxf.acme.com/")
public class OperationStat {

    private String operation;
    private long invocations;
    private long totalMillis;
    private long lastMillis;
    private long averageMillis;

    public OperationStat() {
    }

    public OperationStat(String operation, long invocations, long totalMillis, long lastMillis) {
        this.operation = operation;
        this.invocations = invocations;
        this.totalMillis = totalMillis;
        this.lastMillis = lastMillis;
        this.averageMillis = invocations == 0 ? 0 : totalMillis / invocations;
    }

    @XmlElement
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @XmlElement
    public long getInvocations() {
        return invocations;
    }

    public void setInvocations(long invocations) {
        this.invocations = invocations;
    }

    @XmlElement
    public long getTotalMillis() {
        return totalMillis;
    }

    public void setTotalMillis(long totalMillis) {
        this.totalMillis = totalMillis;
    }

    @XmlElement
    public long getLastMillis() {
        return lastMillis;
    }

    public void setLastMillis(long lastMillis) {
        this.lastMillis = lastMillis;
    }

    /**
     * Moyenne calculee cote serveur et transmise dans le message SOAP.
     * JAXB exige un setter des qu'un getter est serialise, meme si la valeur
     * est derivee des autres champs.
     */
    @XmlElement
    public long getAverageMillis() {
        return invocations == 0 ? 0 : totalMillis / invocations;
    }

    public void setAverageMillis(long averageMillis) {
        this.averageMillis = averageMillis;
    }
}