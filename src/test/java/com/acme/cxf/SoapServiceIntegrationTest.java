package com.acme.cxf;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.acme.cxf.api.ContractService;
import com.acme.cxf.api.HelloService;
import com.acme.cxf.metrics.InvocationJournal;
import com.acme.cxf.model.OperationStat;
import com.acme.cxf.model.Person;
import com.acme.cxf.model.WsdlAnatomy;
import com.acme.cxf.security.ClientSecurity;
import com.acme.cxf.security.SecurityInterceptors;

import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.SOAPFaultException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test d'integration complet du TP 12 : demarre les trois endpoints CXF,
 * appelle les operations metier, verifie la serialisation JAXB, controle que
 * l'endpoint securise refuse un appel sans token puis l'accepte avec le bon
 * couple identifiant/secret.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SoapServiceIntegrationTest {

    private static final String NS = "http://api.cxf.acme.com/";

    private static SoapServer.Endpoints endpoints;
    private static HelloService hello;
    private static HelloService helloSecure;
    private static ContractService contrat;

    @BeforeAll
    static void demarrer() throws Exception {
        System.setProperty("soap.port", "18080");
        endpoints = SoapServer.demarrer();
        InvocationJournal.reinitialiser();

        hello = portHello("hello");
        helloSecure = portHello("hello-secure");
        contrat = portContrat("contract");
    }

    @AfterAll
    static void arreter() {
        if (endpoints != null) {
            endpoints.arreter();
        }
    }

    private static HelloService portHello(String chemin) throws Exception {
        return service(chemin, "HelloService").getPort(HelloService.class);
    }

    private static ContractService portContrat(String chemin) throws Exception {
        return service(chemin, "ContractService").getPort(ContractService.class);
    }

    private static Service service(String chemin, String nomService) throws Exception {
        URL wsdl = new URL(endpoints.baseUrl() + "/" + chemin + "?wsdl");
        return Service.create(wsdl, new QName(NS, nomService));
    }

    @Test
    @Order(1)
    void leWsdlEstExposeEtAnalysable() {
        WsdlAnatomy anatomie = contrat.describeContract(endpoints.baseUrl() + "/hello");

        assertEquals(NS, anatomie.getTargetNamespace());
        assertTrue(anatomie.getPortTypeOperations().containsAll(List.of("SayHello", "FindPerson")),
                "operations attendues absentes : " + anatomie.getPortTypeOperations());
        assertTrue(anatomie.getTypes().contains("Person"),
                "type Person absent : " + anatomie.getTypes());
        assertTrue(anatomie.getServiceEndpoints().stream().anyMatch(entree -> entree.contains("/services/hello")),
                "endpoint absent : " + anatomie.getServiceEndpoints());
    }

    @Test
    @Order(2)
    void sayHelloRenvoieLaSalutationAttendue() {
        assertEquals("Bonjour, Karim", hello.sayHello("Karim"));
    }

    @Test
    @Order(3)
    void sayHelloSansNomRenvoieInconnu() {
        assertEquals("Bonjour, inconnu", hello.sayHello(null));
        assertEquals("Bonjour, inconnu", hello.sayHello("   "));
    }

    @Test
    @Order(4)
    void findPersonSerialiseObjetJaxb() {
        Person personne = hello.findPersonById("P-001");

        assertNotNull(personne);
        assertEquals("P-001", personne.getId());
        assertEquals("Ada Lovelace", personne.getName());
        assertEquals(36, personne.getAge());
    }

    @Test
    @Order(5)
    void findPersonRetourneUnInconnuPourUnIdAbsent() {
        Person personne = hello.findPersonById("P-999");

        assertEquals("P-999", personne.getId());
        assertEquals("Inconnu", personne.getName());
    }

    @Test
    @Order(6)
    void endpointSecuriseRefuseSansToken() {
        assertThrows(SOAPFaultException.class, () -> helloSecure.sayHello("SansToken"));
    }

    @Test
    @Order(7)
    void endpointSecuriseAccepteAvecLeBonToken() {
        ClientSecurity.ajouterUsernameToken(
                helloSecure,
                SecurityInterceptors.UTILISATEUR_DEMO,
                SecurityInterceptors.SECRET_DEMO,
                "PasswordText");

        assertEquals("Bonjour, AvecToken", helloSecure.sayHello("AvecToken"));
    }

    @Test
    @Order(8)
    void lesStatistiquesComptentLesInvocationsParOperation() {
        hello.sayHello("Stat-1");
        hello.sayHello("Stat-2");
        hello.findPersonById("P-002");

        OperationStat statHello = contrat.operationStats("SayHello");
        OperationStat statFind = contrat.operationStats("FindPerson");

        assertTrue(statHello.getInvocations() >= 2, "SayHello devrait compter au moins 2 appels");
        assertTrue(statFind.getInvocations() >= 1, "FindPerson devrait compter au moins 1 appel");
        assertEquals("SayHello", statHello.getOperation());
    }

    @Test
    @Order(9)
    void uneOperationInconnueRenvoieDesStatistiquesVides() {
        OperationStat stat = contrat.operationStats("OperationQuiNExistePas");

        assertEquals(0L, stat.getInvocations());
        assertEquals(0L, stat.getTotalMillis());
    }

    @Test
    @Order(10)
    void lesIdentifiantsParDefautSontCeuxDuProtocole() {
        assertEquals("student", SecurityInterceptors.UTILISATEUR_DEMO);
        assertEquals("secret123", SecurityInterceptors.SECRET_DEMO);
    }
}