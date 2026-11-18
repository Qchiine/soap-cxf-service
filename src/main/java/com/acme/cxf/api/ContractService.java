package com.acme.cxf.api;

import com.acme.cxf.model.OperationStat;
import com.acme.cxf.model.WsdlAnatomy;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

@WebService(targetNamespace = "http://api.cxf.acme.com/")
public interface ContractService {

    /**
     * Renvoie l'anatomie du contrat WSDL effectivement publie par CXF,
     * section par section, sans avoir a ouvrir le document XML a la main.
     */
    @WebMethod(operationName = "DescribeContract")
    @WebResult(name = "anatomy")
    WsdlAnatomy describeContract(@WebParam(name = "endpoint") String endpoint);

    /**
     * Statistiques d'invocation par operation, mesurees cote serveur.
     */
    @WebMethod(operationName = "OperationStats")
    @WebResult(name = "stat")
    OperationStat operationStats(@WebParam(name = "operationName") String operationName);
}
