package com.acme.cxf.impl;

import com.acme.cxf.api.HelloService;
import com.acme.cxf.model.Person;

import jakarta.jws.WebService;

@WebService(
        serviceName = "HelloService",
        portName = "HelloServicePort",
        endpointInterface = "com.acme.cxf.api.HelloService",
        targetNamespace = "http://api.cxf.acme.com/"
)
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
        String cible = (name == null || name.isBlank()) ? "inconnu" : name.trim();
        return "Bonjour, " + cible;
    }

    @Override
    public Person findPersonById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("L'identifiant est obligatoire.");
        }
        String identifiant = id.trim();
        if (identifiant.equals("P-001")) {
            return new Person(identifiant, "Ada Lovelace", 36);
        }
        if (identifiant.equals("P-002")) {
            return new Person(identifiant, "Alan Turing", 41);
        }
        return new Person(identifiant, "Inconnu", 0);
    }
}
