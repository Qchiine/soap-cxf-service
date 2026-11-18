package com.acme.cxf.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

/**
 * Anatomie du contrat WSDL exposee par le service d'introspection.
 * Chaque champ correspond a une section normalisee du document WSDL.
 */
@XmlRootElement(name = "WsdlAnatomy", namespace = "http://api.cxf.acme.com/")
@XmlType(namespace = "http://api.cxf.acme.com/")
public class WsdlAnatomy {

    private String wsdlUrl;
    private String targetNamespace;
    private List<String> types = new ArrayList<>();
    private List<String> messages = new ArrayList<>();
    private List<String> portTypeOperations = new ArrayList<>();
    private List<String> bindings = new ArrayList<>();
    private List<String> serviceEndpoints = new ArrayList<>();

    public WsdlAnatomy() {
    }

    @XmlElement
    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    @XmlElement
    public String getTargetNamespace() {
        return targetNamespace;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    @XmlElement(name = "type")
    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    @XmlElement(name = "message")
    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    @XmlElement(name = "operation")
    public List<String> getPortTypeOperations() {
        return portTypeOperations;
    }

    public void setPortTypeOperations(List<String> portTypeOperations) {
        this.portTypeOperations = portTypeOperations;
    }

    @XmlElement(name = "binding")
    public List<String> getBindings() {
        return bindings;
    }

    public void setBindings(List<String> bindings) {
        this.bindings = bindings;
    }

    @XmlElement(name = "endpoint")
    public List<String> getServiceEndpoints() {
        return serviceEndpoints;
    }

    public void setServiceEndpoints(List<String> serviceEndpoints) {
        this.serviceEndpoints = serviceEndpoints;
    }
}
