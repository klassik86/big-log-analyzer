package com.kk.tools.analyzer;

import com.kk.tools.exception.LogAnalyzerValidationException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Author: PDudin
 * Created date: 03.08.2017.
 */
public interface ILogAnalyzerParameters {
    // TODO: (pdudin) add javaDocs to methods
    void init() throws IOException, LogAnalyzerValidationException;

    List<Pattern> getPatterns();

    List<String> getIds();

    File getLogFile();
}
