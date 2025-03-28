package com.ebremer.halcyonproject.iana2rdf;

import com.ebremer.halcyonproject.iana2rdf.parameters.Parameters;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.RIOT;
import org.apache.jena.vocabulary.XSD;

public class UTILS {
    
    private static final Logger LOGGER = Logger.getLogger(UTILS.class.getName());
    
    public static JsonNode stringToJsonNode(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree(jsonString);
        } catch (JsonProcessingException ex) {
            LOGGER.log(Level.SEVERE,ex.getMessage());
        }
        return null;
    }    
    
    private static void renameTextNodes(JsonNode node, String oldKey) {
        if (node.isObject()) {
            ObjectNode objNode = (ObjectNode) node;
            if (objNode.has(oldKey)) {
                JsonNode valueNode = objNode.get(oldKey);
                if (valueNode.isArray()) {
                    StringBuilder builder = new StringBuilder();
                    for (JsonNode x : valueNode) {
                        builder.append(x.asText());
                    }
                    objNode.set("value", JsonNodeFactory.instance.textNode(builder.toString()));
                    objNode.remove(oldKey);
                } else {
                    objNode.set("value", valueNode);
                    objNode.remove(oldKey);
                }
            }
            objNode.fields().forEachRemaining(entry -> renameTextNodes(entry.getValue(), oldKey));
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                renameTextNodes(element, oldKey);
            }
        }
    }   
    
    public static String xml2jsonld(String xml, String NS) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p, 
                JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) {
                return true;
            }
        });
        JsonNode jsonNode = xmlMapper.readTree(xml.getBytes());
        renameTextNodes(jsonNode, "");
        ObjectMapper jsonMapper = new ObjectMapper();
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonMapper.writeValueAsString(jsonNode)))) {
            JsonObject jo = jsonReader.readObject();
            JsonObject jo2 = Json.createObjectBuilder(jo).add("@context", Json.createObjectBuilder()
                    .add("@vocab", NS)
                    //.add("id", "@id")
                    //.add("note", Json.createObjectBuilder().add("@container", "@list"))
                    //.add("td", Json.createObjectBuilder().add("@container", "@list"))
                    //.add("para", Json.createObjectBuilder().add("@container", "@list"))
            ).build();
            Map<String, Object> config = new HashMap<>();
            config.put(JsonGenerator.PRETTY_PRINTING, true);
            JsonWriterFactory writerFactory = Json.createWriterFactory(config);
            StringWriter stringWriter = new StringWriter();
            try (JsonWriter jsonWriter = writerFactory.createWriter(stringWriter)) {
                jsonWriter.write(jo2);
            }
            return stringWriter.toString();            
        }
    }
    
    public static Model xml2Model(String xml, String NS) throws IOException {
        Model model = ModelFactory.createDefaultModel();
        try (StringReader reader = new StringReader(xml2jsonld(xml, NS))) {       
            RDFDataMgr.read(model, reader, null, Lang.JSONLD);
        }
        return model;
    }
    
    public static Model clean(Model m) {
        Model neo = ModelFactory.createDefaultModel();
        m.listStatements().forEach(t->{
            if (t.getObject().isLiteral()) {
                Literal node = t.getObject().asLiteral();
                if (node.getDatatypeURI().equals(XSD.xstring.toString())) {
                    neo.add(t.getSubject(), t.getPredicate(), node.getString().trim());                    
                }
            } else {
                neo.add(t);
            }
        });
        return neo;
    }
    
    public static void Dump(Model m, File file) {
        m.setNsPrefix("", "https://halycon.is/d2s/");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            RDFDataMgr.write(fos, m, Lang.TURTLE);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE,ex.getMessage());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE,ex.getMessage());
        }
    }
    
    public static void Dump(Model m, String... path) {   
        File file = Path.of("/DicomShapes/", path).toFile();
        Dump(m, file);
    }
    
    public static String retrieveXml(String urlString) throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(urlString))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new IOException("HTTP error: " + response.statusCode());
        }
    }
    
    public static void Spray(Parameters params, Model m) {
        File target = params.spray;
        if (!target.exists()) {
            target.mkdirs();
        }
        ParameterizedSparqlString pss = new ParameterizedSparqlString(
            """
            select distinct ?s
            where {
                ?s ?p ?o
                filter(strstarts(str(?s),?BASE))
            }
            """);
        pss.setLiteral("BASE", iana2rdf.BASE);
        URI base = URI.create(iana2rdf.BASE);
        QueryExecutionFactory.create(pss.toString(), m)
            .execSelect()
            .materialise()
            .forEachRemaining(t->{
                String s = t.get("s").asResource().getURI();
                URI p = URI.create(s);
                String r = base.relativize(p).toString();
                ParameterizedSparqlString pss2 = new ParameterizedSparqlString(
                """
                construct {
                    ?s ?p ?o .
                    ?o ?op ?oo .
                    ?ss ?pp ?s
                } where {{
                    ?s ?p ?o
                    optional { ?o ?op ?oo filter (isblank(?o)) }                        
                } union {
                    ?ss ?pp ?s
                }}
                """);
                pss2.setIri("s", t.getResource("s").getURI());
                Model frag = QueryExecutionFactory.create(pss2.toString(), m).execConstruct();
                r = r.split("#")[0]+".ttl";
                File dump = new File(target,r);
                if (!dump.getParentFile().exists()) {
                    dump.getParentFile().mkdirs();
                }
                frag.setNsPrefix("", "#");
                try (FileOutputStream fos = new FileOutputStream(dump)) {
                    RDFWriter.create()
                        .set(RIOT.symTurtleOmitBase, true)
                        .base(t.get("s").asResource().getURI().split("#")[0]+"#")                           
                        .format(RDFFormat.TURTLE_PRETTY)
                        .source(frag).output(fos);                    
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE,ex.getMessage());
                }
            });
    }      
}
