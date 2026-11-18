package com.acme.cxf.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.acme.cxf.model.OperationStat;

/**
 * Journal d'invocation en memoire, alimente par un intercepteur CXF.
 * Permet de mesurer les operations SOAP sans instrumenter le code metier.
 */
public final class InvocationJournal {

    private static final Map<String, Compteur> COMPTEURS = new ConcurrentHashMap<>();

    private InvocationJournal() {
    }

    public static void enregistrer(String operation, long dureeMillis) {
        COMPTEURS.computeIfAbsent(operation, cle -> new Compteur()).ajouter(dureeMillis);
    }

    public static OperationStat stat(String operation) {
        Compteur compteur = COMPTEURS.get(operation);
        if (compteur == null) {
            return new OperationStat(operation, 0L, 0L, 0L);
        }
        return new OperationStat(operation, compteur.invocations.get(), compteur.total.get(), compteur.derniere.get());
    }

    public static Map<String, Compteur> instantane() {
        return Map.copyOf(COMPTEURS);
    }

    public static void reinitialiser() {
        COMPTEURS.clear();
    }

    public static final class Compteur {

        private final AtomicLong invocations = new AtomicLong();
        private final AtomicLong total = new AtomicLong();
        private final AtomicLong derniere = new AtomicLong();

        void ajouter(long dureeMillis) {
            invocations.incrementAndGet();
            total.addAndGet(dureeMillis);
            derniere.set(dureeMillis);
        }

        public long getInvocations() {
            return invocations.get();
        }

        public long getTotal() {
            return total.get();
        }

        public long getDerniere() {
            return derniere.get();
        }
    }
}
