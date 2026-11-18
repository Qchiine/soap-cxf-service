package com.acme.cxf.security;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.wss4j.common.ext.WSPasswordCallback;

public class UTPasswordCallback implements CallbackHandler {

    private final Map<String, String> utilisateurs;

    public UTPasswordCallback(Map<String, String> utilisateurs) {
        this.utilisateurs = Map.copyOf(utilisateurs);
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof WSPasswordCallback motDePasse) {
                String attendu = utilisateurs.get(motDePasse.getIdentifier());
                if (attendu == null) {
                    throw new UnsupportedCallbackException(callback, "Utilisateur inconnu : " + motDePasse.getIdentifier());
                }
                motDePasse.setPassword(attendu);
                return;
            }
        }
    }
}
