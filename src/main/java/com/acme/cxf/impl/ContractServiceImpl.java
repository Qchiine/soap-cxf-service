package com.acme.cxf.impl;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.acme.cxf.api.ContractService;
import com.acme.cxf.metrics.InvocationJournal;
import com.acme.cxf.model.OperationStat;
import com.acme.cxf.model.WsdlAnatomy;

import jakarta.jws.WebService;

@WebService(
        serviceName = "ContractService",
        portName = "ContractServicePort",
        endpointInterface = "com.acme.cxf.api.ContractService",
        targetNamespace = "http://api.cxf.acme.com/"
)
public class ContractServiceImpl implements ContractService {

    private static final String NS_WSDL = "http://schemas.xmlsoap.org/wsdl/";

    @Override
    public WsdlAnatomy describeContract(String endpoint) {
        String base = (endpoint == null || endpoint.isBlank())
                ? "http://localhost:8080/services/hello"
                : endpoint.trim();
        String url = base.endsWith("?wsdl") ? base : base + "?wsdl";

        WsdlAnatomy anatomie = new WsdlAnatomy();
        anatomie.setWsdlUrl(url);

        Document document = charger(url);
        Element racine = document.getDocumentElement();
        anatomie.setTargetNamespace(racine.getAttribute("targetNamespace"));

        NodeList types = racine.getElementsByTagNameNS(NS_WSDL, "types");
        if (types.getLength() > 0) {
            NodeList schemas = ((Element) types.item(0)).getElementsByTagNameNS("*", "schema");
            for (int i = 0; i < schemas.getLength(); i++) {
                NodeList elements = ((Element) schemas.item(i)).getElementsByTagNameNS("*", "element");
                for (int j = 0; j < elements.getLength(); j++) {
                    Element element = (Element) elements.item(j);
                    anatomie.getTypes().add(element.getAttribute("name"));
                }
            }
        }

        NodeList messages = racine.getElementsByTagNameNS(NS_WSDL, "message");
        for (int i = 0; i < messages.getLength(); i++) {
            anatomie.getMessages().add(((Element) messages.item(i)).getAttribute("name"));
        }

        NodeList portTypes = racine.getElementsByTagNameNS(NS_WSDL, "portType");
        for (int i = 0; i < portTypes.getLength(); i++) {
            Element portType = (Element) portTypes.item(i);
            NodeList operations = portType.getElementsByTagNameNS(NS_WSDL, "operation");
            for (int j = 0; j < operations.getLength(); j++) {
                anatomie.getPortTypeOperations().add(((Element) operations.item(j)).getAttribute("name"));
            }
        }

        NodeList bindings = racine.getElementsByTagNameNS(NS_WSDL, "binding");
        for (int i = 0; i < bindings.getLength(); i++) {
            anatomie.getBindings().add(((Element) bindings.item(i)).getAttribute("name"));
        }

        NodeList ports = racine.getElementsByTagNameNS(NS_WSDL, "port");
        for (int i = 0; i < ports.getLength(); i++) {
            Element port = (Element) ports.item(i);
            NodeList adresses = port.getElementsByTagNameNS("*", "address");
            String adresse = adresses.getLength() > 0
                    ? ((Element) adresses.item(0)).getAttribute("location")
                    : "(adresse absente)";
            anatomie.getServiceEndpoints().add(port.getAttribute("name") + " -> " + adresse);
        }

        return anatomie;
    }

    @Override
    public OperationStat operationStats(String operationName) {
        return InvocationJournal.stat(operationName == null ? "SayHello" : operationName.trim());
    }

    private Document charger(String url) {
        try {
            DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
            fabrique.setNamespaceAware(true);
            fabrique.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder constructeur = fabrique.newDocumentBuilder();
            return constructeur.parse(new InputSource(new StringReader(lire(url))));
        } catch (Exception exception) {
            throw new IllegalStateException("WSDL illisible : " + url, exception);
        }
    }

    private String lire(String url) throws Exception {
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpRequest requete = java.net.http.HttpRequest.newBuilder(java.net.URI.create(url)).GET().build();
        java.net.http.HttpResponse<String> reponse = client.send(requete, java.net.http.HttpResponse.BodyHandlers.ofString());
        if (reponse.statusCode() != 200) {
            throw new IllegalStateException("Le WSDL a repondu " + reponse.statusCode());
        }
        return reponse.body();
    }

}
