package com.ebremer.halcyonproject.iana2rdf;

import static com.ebremer.halcyonproject.iana2rdf.iana2rdf.BASE;
import static com.ebremer.halcyonproject.iana2rdf.iana2rdf.PEOPLENS;
import java.util.logging.Logger;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.XSD;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDFS;

public class Protocol {
    
    public static String Version = "1.0.0";
    private static final Logger LOGGER = Logger.getLogger(iana2rdf.class.getName());    
    
    public static void IRIGeneration(Model m) {
        UpdateRequest request = UpdateFactory.create();
        ParameterizedSparqlString pss = new ParameterizedSparqlString(
            """
            #
            # generate URIs for records
            #
            
            delete {
                ?registry :record ?record .
                ?record :name ?id; ?p3 ?o3
            }
            insert {
                ?registry :record ?piri .
                ?piri :name ?id; ?p3 ?o3; rdfs:label ?rah
            }
            where {
                ?registrar :id ?registrarid; :registry ?registry .
                ?registry :id ?registryid; :record ?record .
                ?record :name ?id; ?p3 ?o3; :file/:value ?rah
                bind(iri(concat(?BASE,?registrarid,"/",?rah,"#it")) as ?piri)
            };

            #
            # generate URIs for registries
            #
            
            delete {
                ?registrar :registry ?registry .
                ?registry :id ?registryid; :record ?record ; ?p2 ?o2 .
            }
            insert {
                ?registrar :registry ?registrypiri .
                ?registrypiri :id ?registryid; :record ?piri ; ?p2 ?o2 .
            }
            where {
                ?registrar :id ?registrarid; :registry ?registry .
                ?registry :id ?registryid; :record ?record ; ?p2 ?o2 .
                bind(iri(concat(?BASE,?registrarid,"/",?registryid,"#it")) as ?registrypiri)
            };                        

            #
            # generate URIs for registrar            
            #
            
            delete {
                ?registrar :id ?registrarid; :registry ?registry ; ?p ?o .
            }
            insert {
                ?registrarpiri :id ?registrarid; :registry ?registrypiri ; ?p ?o .
            }
            where {
                ?registrar :id ?registrarid; :registry ?registry ; ?p ?o .
                bind(iri(concat(?BASE,?registrarid,"#it")) as ?registrarpiri)
            };
            
            #
            # generate URIs for people and organizations                        
            #
            
            delete {
                ?people :person ?person .
                ?person :id ?id; ?p ?o
            }
            insert {
                ?people :person ?piri .
                ?piri :id ?id; ?p ?o
            }
            where {
                ?people :person ?person .
                ?person :id ?id; ?p ?o
                bind(iri(concat(?peopleNS,?id)) as ?piri)
            };
            
            #
            # remove empty example registry
            #
            
            delete {
                ?a ?b ?piri .                
                ?piri ?p ?o .
            }
            where {
                ?a ?b ?s .                
                ?s ?p ?o .                
                filter (?s=?piri)
                bind(iri(concat(?BASE,"example#it")) as ?piri)
            };    
            
            delete {
                ?a :registry ?registry .
                ?registry :id "example"; ?p ?o .
            }
            where {
                ?a :registry ?registry .
                ?registry :id "example"; ?p ?o .
            };             
            """
        );
        pss.setLiteral("BASE", BASE);
        pss.setNsPrefix("", REG.NS);
        pss.setNsPrefix("rdfs", RDFS.uri);
        pss.setLiteral("peopleNS", PEOPLENS);
        request.add(pss.toString()); 
        UpdateAction.execute(request,m);
    } 
    
    public static void General_Mods(Model m) {
        UpdateRequest request = UpdateFactory.create();
        ParameterizedSparqlString pss = new ParameterizedSparqlString(
            """
            #
            # Change date strings to xsd:date                        
            #
            
            delete { ?s :updated ?date }
            insert { ?s :updated ?newdate }
            where {
                ?s :updated ?date
                bind(STRDT(?date, xsd:date) as ?newdate)
            };
                       
            delete { ?s :date ?date }
            insert { ?s :date ?newdate }
            where {
                ?s :date ?date
                bind(STRDT(?date, xsd:date) as ?newdate)
            };
            
            #           
            # change reg:updated to dct:modified                       
            #
            
            #delete { ?s :updated ?o }
            #insert { ?s dct:modified ?o }
            #where { ?s :updated ?o };
                        
            # change reg:date to dct:date            
            #delete { ?s :date ?o }
            insert { ?s dct:date ?o }
            where { ?s :date ?o; :file ?file };
            
            #            
            # create RFC links            
            #
            
            delete {
                ?s :xref ?bnode .
                ?bnode :data ?data; :type ?type; ?p ?o
            }
            insert {
                ?s :xref ?iri
            }
            where {
                ?s :xref ?bnode .
                ?bnode :data ?data; :type ?type
                filter(?type in ("rfc","draft" ))
                bind(iri(concat(?html,?data)) as ?iri)
            };
           
            delete {
                ?s :xref ?bnode .
                ?bnode :data ?data; :type "rfc-errata"
            }
            insert {
                ?s :xref ?iri
            }
            where {
                ?s :xref ?bnode .
                ?bnode :data ?data; :type "rfc-errata"
                bind(iri(concat(?errataBase,?data)) as ?iri)
            };                             

            delete {
                ?s :xref ?bnode .
                ?bnode :data ?data; :type "note"
            }
            insert {
                ?s :xref ?iri
            }
            where {
                ?s :xref ?bnode .
                ?bnode :data ?data; :type "note"
                bind(iri(concat(?noteBase,?data)) as ?iri)
            };
                                                
            #delete {
            #    ?s :xref ?bnode .
            #    ?bnode :data ?data; :type "registry"
            #}
            insert {
                ?s rdfs:seeAlso ?iri
            }
            where {
                ?s :xref ?bnode .
                ?bnode :data ?data; :type "registry"
                bind(iri(concat(?registryBase,?data)) as ?iri)
            };  
            
            #
            # create people/org links                        
            #
            
            #delete {
            #    ?s :xref ?bnode .
            #    ?bnode :data ?data; :type "person"
            #}
            insert {
                ?s prov:wasAttributedTo ?iri
            }
            where {
                ?s :xref ?bnode .
                ?bnode :data ?data; :type "person"
                bind(iri(concat(?peopleNS,?data)) as ?iri)
                optional { ?bnode :value ?value }            
            };

            #
            # create uri links for http links            
            #
            
            #delete {
            #    ?s :xref ?bnode .
            #    ?bnode :data ?data; :type "uri";  ?p ?o .
            #}
            insert { ?s :xref ?data }
            where {
                ?s :xref ?bnode .
                ?bnode :data ?data; :type "uri"; ?p ?o
                optional { ?bnode :value ?value }            
            };

            #delete {
            #    ?s :file ?bnode .
            #    ?bnode :value ?value; :type "template"
            #}
            insert {
                ?s :file ?iri
            }
            where {
                ?s :file ?bnode .
                ?bnode :value ?value; :type "template"
                bind(iri(concat(?templateiri,?value)) as ?iri)
                optional { ?bnode :value ?value }            
            };

            #
            # maps reg:name to foaf:name
            #
                        
            delete { ?s :name ?o }
            insert { ?s foaf:name ?o }
            where {
                ?s :name ?o; :uri ?uri
                filter(strstarts(?uri,"mailto:"))
            };
            
            #
            # generate links for mailto uris
            #
            
            #delete { ?s :uri ?o }
            insert { ?s foaf:mbox ?o }
            where {
                ?s :uri ?o
                filter(strstarts(?o,"mailto:"))
            };
            
            #
            # remove reg:id now they are are part of uris
            #
            
            #delete { ?s :id ?o }
            #where { ?s :id ?o };
             
            #  
            # remove reg:name as rdfs:label was generated in iri-fication step
            #
            
            #delete { ?s :name ?o }
            #where { ?s :name ?o };
            
            #
            # map ref:file to rdfs:seeAlso
            #
            
            #delete { ?s :file ?o }
            #insert { ?s rdfs:seeAlso ?o }
            #where { ?s :file ?o filter (!isBlank(?o)) };
                        
            #delete { ?s :uri ?uri }
            insert { ?s foaf:homepage ?uri }
            where { ?s :uri ?uri filter(strstarts(?uri,"http"))};
            
            #
            # add subclasses
            #
            
            #delete { ?s :record ?record }
            insert {
                ?record rdfs:subClassOf ?s .
            }
            where { ?s :record ?record };
            
            #
            # add seeAlso
            #
            
            #delete { ?s :xref ?o }
            #insert { ?s rdfs:seeAlso ?o }
            #where { ?s :xref ?o; rdfs:label ?label filter (!isBlank(?o))};
                                                         
            insert { ?s a foaf:Organization }
            where { ?s :org ?o  };
            
            insert { ?s a foaf:Agent }
            where {
                ?s ?p ?o
                filter(strstarts(str(?s),?peopleNS)) 
            };
            
            delete { ?s :expert ?o }
            insert { ?s prov:wasAssociatedWith ?o }
            where { ?s :expert ?o };

            delete { ?s :category ?o }
            insert { ?s dct:subject ?o }
            where { ?s :category ?o };

            #delete { ?s :title ?o }
            insert { ?s dct:title ?o }
            where { ?s :title ?o };

            #
            # remove some misc data
            #
                                    
            delete { ?s :registration_rule ?bnode . ?bnode ?p ?o }
            where { ?s :registration_rule ?bnode . ?bnode ?p ?o };
            
            delete { ?s :footnote ?bnode . ?bnode ?p ?o }
            where { ?s :footnote ?bnode . ?bnode ?p ?o };

            delete { ?s :note ?bnode . ?bnode ?p ?o }
            where { ?s :note ?bnode . ?bnode ?p ?o };
            
            delete { ?s :note ?o }
            where { ?s :note ?o };
            
            delete { ?s :hide ?o }
            where { ?s :hide ?o };
            
            #
            # remove containing "people"
            #
            
            delete { ?s :people ?list . ?list :person ?o }
            insert { ?s :person ?o }           
            where { ?s :people ?list . ?list :person ?o };   
            
            #
            # add registry subclasses
            #
            
            #delete { ?s :registry ?o }
            insert { ?o rdfs:subClassOf ?s }
            where { ?s :registry ?o };                     

            #
            # add name for organizations
            #
            
            delete { ?s :org ?o }
            insert { ?s foaf:name ?o }
            where { ?s :org ?o }; 
            
            #delete { ?s :value ?o }
            #where { ?s :value ?o };
            
            delete { ?s :data ?data; :type ?type }
            where {
                ?s :data ?data; :type ?type
                minus {?r ?p ?s}
                filter (isblank(?s))
            };
            """
        );
        pss.setLiteral("rfciri", "https://www.iana.org/go/");
        pss.setLiteral("peopleNS", PEOPLENS);
        pss.setLiteral("templateiri", "https://www.iana.org/assignments/media-types/");
        pss.setLiteral("errataBase", "https://www.rfc-editor.org/errata/");
        pss.setLiteral("noteBase", "https://www.iana.org/assignments/media-types/media-types.xhtml#note");
        pss.setLiteral("registryBase", "https://www.iana.org/assignments/");
        pss.setNsPrefix("", REG.NS);
        pss.setNsPrefix("xsd",XSD.NS);        
        pss.setNsPrefix("html","https://tools.ietf.org/html/");
        pss.setLiteral("html","https://tools.ietf.org/html/");
        pss.setNsPrefix("foaf",FOAF.NS);
        pss.setNsPrefix("rdfs",RDFS.uri);
        pss.setNsPrefix("prov",PROVO.NS);
        pss.setNsPrefix("dct", DCTerms.NS);
        request.add(pss.toString()); 
        UpdateAction.execute(request,m);
    }
    
    public static void setNameSpaces(Model m) {
        m.setNsPrefix("reg", REG.NS);
        m.removeNsPrefix("");
        m.setNsPrefix("xsd", XSD.NS);
        m.setNsPrefix("person", PEOPLENS);
        m.setNsPrefix("dct", DCTerms.NS);
        m.setNsPrefix("foaf", FOAF.NS);
        m.setNsPrefix("rdfs", RDFS.uri);
        m.setNsPrefix("errata", "https://www.rfc-editor.org/errata/");
        m.setNsPrefix("html","https://tools.ietf.org/html/");
        m.setNsPrefix("prov", PROVO.NS);
        m.setNsPrefix("note", "https://www.iana.org/assignments/media-types/media-types.xhtml#");
    }
}
