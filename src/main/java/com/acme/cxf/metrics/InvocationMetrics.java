package com.acme.cxf.metrics;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

/**
 * Deux intercepteurs complementaires mesurent une invocation SOAP :
 *   - InvocationStartInterceptor (phase RECEIVE) pose l'horodatage de depart
 *   - InvocationEndInterceptor (phase SEND) calcule la duree et l'enregistre
 *
 * Passer par deux phases evite de dependre du moment ou CXF resout le nom
 * de l'operation, qui n'est pas encore connu a la reception du message.
 */
public final class InvocationMetrics {

    private static final String CLE_DEBUT = InvocationMetrics.class.getName() + ".debut";

    private InvocationMetrics() {
    }

    public static final class InvocationStartInterceptor extends AbstractPhaseInterceptor<Message> {

        public InvocationStartInterceptor() {
            super(Phase.RECEIVE);
        }

        @Override
        public void handleMessage(Message message) {
            message.put(CLE_DEBUT, System.nanoTime());
        }
    }

    public static final class InvocationEndInterceptor extends AbstractPhaseInterceptor<Message> {

        public InvocationEndInterceptor() {
            super(Phase.SEND);
        }

        @Override
        public void handleMessage(Message message) {
            Exchange exchange = message.getExchange();
            Message entree = exchange == null ? message : exchange.getInMessage();
            if (entree == null) {
                return;
            }
            Object debut = entree.get(CLE_DEBUT);
            if (!(debut instanceof Long debutNano)) {
                return;
            }
            entree.remove(CLE_DEBUT);

            long dureeMillis = Math.max(0L, Math.round((System.nanoTime() - debutNano) / 1_000_000.0));
            InvocationJournal.enregistrer(operationDe(entree), dureeMillis);
        }

        @Override
        public void handleFault(Message message) {
            Message entree = message.getExchange() == null ? null : message.getExchange().getInMessage();
            if (entree != null) {
                entree.remove(CLE_DEBUT);
            }
        }

        /**
         * CXF expose l'operation sous forme de QName "{namespace}SayHello".
         * Seule la partie locale est conservee pour que les statistiques restent
         * interrogeables avec le nom simple declare dans @WebMethod.
         */
        private String operationDe(Message message) {
            Object operation = message.get("org.apache.cxf.resource.operation.name");
            if (operation != null) {
                return normaliser(operation.toString());
            }
            Object qname = message.get(Message.WSDL_OPERATION);
            return qname == null ? "inconnue" : normaliser(qname.toString());
        }

        private String normaliser(String nom) {
            int accolade = nom.lastIndexOf('}');
            return accolade >= 0 && accolade < nom.length() - 1 ? nom.substring(accolade + 1) : nom;
        }
    }
}