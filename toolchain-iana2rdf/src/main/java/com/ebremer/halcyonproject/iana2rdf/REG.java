package com.ebremer.halcyonproject.iana2rdf;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class REG {

    public static final String NS = "https://www.w3.org/ns/assignments/reg#";
    
    public static final Property name = ResourceFactory.createProperty(NS, "name");
    public static final Property xref = ResourceFactory.createProperty(NS, "xref");
    public static final Property spec = ResourceFactory.createProperty(NS, "spec");
    public static final Property expert = ResourceFactory.createProperty(NS, "expert");
    public static final Property id = ResourceFactory.createProperty(NS, "id");
    public static final Property record = ResourceFactory.createProperty(NS, "record");
    
}
