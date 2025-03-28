package com.ebremer.halcyonproject.iana2rdf.parameters;

import com.beust.jcommander.Parameter;
import java.io.File;
import java.util.logging.Level;

/**
 *
 * @author erich
 */
public class Parameters {  
    @Parameter(names = {"-xml"}, converter = FileConverter.class, description = "Source IANA XML File", required = false, validateWith = Dcm2RdfValidator.class, order = 0)
    public File xml = null;

    @Parameter(names = {"-rdf"}, converter = FileConverter.class, description = "Destination RDF Turtle File", required = false, validateWith = Dcm2RdfValidator.class, order = 1)
    public File rdf = null;  

    @Parameter(names = {"-spray"}, converter = FileConverter.class, description = "Destination Spray Folder", required = false, validateWith = Dcm2RdfValidator.class, order = 2)
    public File spray = null;  

    @Parameter(names = {"-c"}, converter = BooleanConverter.class, description = "results file will be gzipped compressed", validateWith = Dcm2RdfValidator.class, order = 3)
    public Boolean compress = false;
    
    @Parameter(names = {"-version"}, converter = BooleanConverter.class, description = "Display software version", validateWith = Dcm2RdfValidator.class, order = 4)
    public Boolean version = false;
    
    @Parameter(names = {"-help"}, converter = BooleanConverter.class, description = "Display help information", validateWith = Dcm2RdfValidator.class, order = 5)
    public Boolean help = false;

    @Parameter(names = {"-level"}, converter = LogLevelConverter.class, description = "Sets logging level (OFF, ALL, WARNING, SEVERE)", order = 6, hidden = true)
    public Level level = Level.SEVERE;
}
