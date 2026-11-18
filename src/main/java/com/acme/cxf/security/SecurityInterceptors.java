package com.acme.cxf.security;

import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;

import java.util.HashMap;
import java.util.Map;

/**
 * Fabrique l'intercepteur WS-Security entrant en mode UsernameToken.
 *
 * Deux modes sont exposes :
 *   - texte  : PasswordText, lisible sur le reseau, reserve a la demonstration locale
 *   - digest : PasswordDigest, mot de passe non transmis en clair, recommande
 *
 * Un mot de passe non vide peut etre impose en plus du callback, ce qui permet
 * de refuser un utilisateur connu mais avec un mauvais secret.
 */
public final class SecurityInterceptors {

    public static final String UTILISATEUR_DEMO = "student";
    public static final String SECRET_DEMO = "secret123";

    private SecurityInterceptors() {
    }

    public static WSS4JInInterceptor usernameTokenTexte(Map<String, String> utilisateurs) {
        return construire(utilisateurs, "PasswordText");
    }

    public static WSS4JInInterceptor usernameTokenDigest(Map<String, String> utilisateurs) {
        return construire(utilisateurs, "PasswordDigest");
    }

    private static WSS4JInInterceptor construire(Map<String, String> utilisateurs, String typeMotDePasse) {
        Map<String, Object> proprietes = new HashMap<>();
        proprietes.put("action", "UsernameToken");
        proprietes.put("passwordType", typeMotDePasse);
        proprietes.put("passwordCallbackRef", new UTPasswordCallback(utilisateurs));
        return new WSS4JInInterceptor(proprietes);
    }
}
