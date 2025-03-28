package com.ebremer.halcyonproject.iana2rdf.parameters;

import com.beust.jcommander.IStringConverter;
import java.io.File;

public class FileConverter implements IStringConverter<File> {

    @Override
    public File convert(String value) {
        return new File(value);
    }
}
