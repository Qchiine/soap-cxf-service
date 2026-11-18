package com.acme.cxf.security;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.apache.wss4j.dom.handler.WSHandlerConstants;

/**
 * Configure l'envoi d'un UsernameToken par un client JAX-WS.
 * Le secret reste en memoire uniquement, il n'est jamais journalise.
 */
public final class ClientSecurity {

    private ClientSecurity() {
    }

    public static void ajouterUsernameToken(Object port, String utilisateur, String secret, String typeMotDePasse) {
        Map<String, Object> proprietes = new HashMap<>();
        proprietes.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
        proprietes.put(WSHandlerConstants.USER, utilisateur);
        proprietes.put(WSHandlerConstants.PASSWORD_TYPE, typeMotDePasse);
        proprietes.put(WSHandlerConstants.PW_CALLBACK_REF, callback(secret));

        ClientProxy.getClient(port).getOutInterceptors().add(new WSS4JOutInterceptor(proprietes));
    }

    /**
     * PasswordText transmet le secret en clair, PasswordDigest envoie un digest
     * nonce + horodatage. Dans les deux cas le callback fournit le secret a WSS4J.
     */
    private static CallbackHandler callback(String secret) {
        return callbacks -> {
            for (Callback callback : callbacks) {
                if (callback instanceof WSPasswordCallback motDePasse) {
                    motDePasse.setPassword(secret);
                }
            }
        };
    }
}