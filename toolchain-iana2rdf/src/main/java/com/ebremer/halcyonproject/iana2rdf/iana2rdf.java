package com.ebremer.halcyonproject.iana2rdf;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.ebremer.halcyonproject.iana2rdf.parameters.Parameters;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.LoggerFactory;

public class iana2rdf {
    
    public static String VERSION = "1.0.0";
    private static final Logger LOGGER = Logger.getLogger(iana2rdf.class.getName());
    
    public static final String BASE =     "https://www.w3.org/ns/iana/";
    public static final String NS = BASE + "/media-types/";
    public static final String PEOPLENS = BASE + "person/";
    
    public static void main(String[] args) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(ch.qos.logback.classic.Level.OFF);                
        Parameters params = new Parameters();
        JCommander jc = JCommander.newBuilder().addObject(params).build();
        jc.setProgramName("iana2rdf");    
        try {
            jc.parse(args);
            if (params.version) {
                System.out.println(String.format("iana2rdf - version [%s]", VERSION));
                System.exit(0);
            }
            if ((params.help)||(((params.rdf==null)&&(params.spray==null)))) {
                jc.usage();
                System.out.println("Use -Xmx to set maximum memory.  For example, '-Xmx30G' will set maximum at 30G");
                System.exit(0);
            }
            LOGGER.setLevel(Level.WARNING);
            LOGGER.setUseParentHandlers(false);
            ConsoleHandler consoleHandler = new ConsoleHandler();
            LOGGER.setLevel(params.level);
            LOGGER.addHandler(consoleHandler);      
            try {
                String xml = null;
                if (params.xml!=null) {
                    if (params.xml.exists()) {
                        xml = Files.readString(params.xml.toPath());
                    } else {
                        throw new Error("Source XML does not exist "+params.xml.toString());
                    }
                } else {
                    try {
                        xml = UTILS.retrieveXml("https://www.iana.org/assignments/media-types/media-types.xml");
                    } catch (URISyntaxException ex) {
                        LOGGER.log(Level.SEVERE,ex.getMessage());
                    } catch (InterruptedException ex) {
                        LOGGER.log(Level.SEVERE,ex.getMessage());
                    }
                }
                Model m = UTILS.xml2Model(xml, REG.NS);
                    
                Protocol.IRIGeneration(m);
                Protocol.General_Mods(m);
                Protocol.setNameSpaces(m);
                    
                if (params.rdf!=null) {
                    if (!params.rdf.getParentFile().exists()) {
                        params.rdf.getParentFile().mkdirs();
                    }
                    if (params.compress) {
                        File ff = new File(params.rdf.toString()+".gz");
                        try (                                
                            FileOutputStream fos = new FileOutputStream(ff);
                            GZIPOutputStream gos = new GZIPOutputStream(fos);
                        ) {
                            RDFDataMgr.write(gos, m, Lang.TURTLE);
                        }                            
                    } else {
                        try (FileOutputStream fos = new FileOutputStream(params.rdf)) {
                            RDFDataMgr.write(fos, m, Lang.TURTLE);
                        }
                    }
                }
                if (params.spray!=null) {
                    UTILS.Spray(params, m);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE,ex.getMessage());
            } catch (SecurityException ex) {
                LOGGER.log(Level.SEVERE,ex.getMessage());
            }
        } catch (ParameterException ex) {
            if (params.version) {
                System.out.println("iana2rdf - Version : "+VERSION);
            } else {
                jc.usage();
            }
        }
    }
}
