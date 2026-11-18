package com.acme.cxf;

import java.util.Map;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;

import com.acme.cxf.impl.ContractServiceImpl;
import com.acme.cxf.impl.HelloServiceImpl;
import com.acme.cxf.metrics.InvocationMetrics;
import com.acme.cxf.security.SecurityInterceptors;

/**
 * Point d'entree du TP 12 : publie simultanement
 *   - le service HelloService en clair           /services/hello
 *   - le meme service protege par WS-Security    /services/hello-secure
 *   - le service d'introspection du contrat      /services/contract
 *
 * Le WSDL de chaque endpoint est disponible sur <adresse>?wsdl.
 *
 * La classe est nommee SoapServer pour ne pas masquer org.apache.cxf.endpoint.Server.
 */
public final class SoapServer {

    public static final int PORT = Integer.parseInt(System.getProperty("soap.port", "8080"));

    private SoapServer() {
    }

    public static void main(String[] args) {
        Endpoints endpoints = demarrer();
        System.out.println("WSDL clair    : " + endpoints.baseUrl() + "/hello?wsdl");
        System.out.println("WSDL securise : " + endpoints.baseUrl() + "/hello-secure?wsdl");
        System.out.println("WSDL contrat  : " + endpoints.baseUrl() + "/contract?wsdl");
        System.out.println("Identifiants  : " + SecurityInterceptors.UTILISATEUR_DEMO
                + " / " + SecurityInterceptors.SECRET_DEMO);
    }

    public static Endpoints demarrer() {
        String base = "http://localhost:" + PORT + "/services";

        Server clair = publier(new HelloServiceImpl(), base + "/hello", null);
        Server securise = publier(new HelloServiceImpl(), base + "/hello-secure",
                SecurityInterceptors.usernameTokenTexte(Map.of(
                        SecurityInterceptors.UTILISATEUR_DEMO, SecurityInterceptors.SECRET_DEMO)));
        Server contrat = publier(new ContractServiceImpl(), base + "/contract", null);

        return new Endpoints(clair, securise, contrat, base);
    }

    private static Server publier(Object implementation, String adresse, WSS4JInInterceptor securite) {
        JaxWsServerFactoryBean fabrique = new JaxWsServerFactoryBean();
        fabrique.setServiceBean(implementation);
        fabrique.setAddress(adresse);
        Server serveur = fabrique.create();

        serveur.getEndpoint().getInInterceptors().add(new InvocationMetrics.InvocationStartInterceptor());
        serveur.getEndpoint().getOutInterceptors().add(new InvocationMetrics.InvocationEndInterceptor());

        if (securite != null) {
            serveur.getEndpoint().getInInterceptors().add(securite);
        }
        return serveur;
    }

    public record Endpoints(Server clair, Server securise, Server contrat, String baseUrl) {

        public void arreter() {
            clair.stop();
            securise.stop();
            contrat.stop();
        }
    }
}