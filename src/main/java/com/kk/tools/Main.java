package com.kk.tools;

import com.kk.tools.analyzer.ILogAnalyzer;
import com.kk.tools.analyzer.LogAnalyzer;
import com.kk.tools.analyzer.LogAnalyzerParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Author: PDudin
 * Created date: 20.08.2017.
 */
public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    // TODO: (pdudin) refactor to SpringBootApplication CommandLineRunner
    // TODO: (pdudin) add help command

    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            throw new IllegalArgumentException("Count of arguments must be equals 2");
        }
        LOG.info(">> main > parameters: logFilePath = {}, paramFilePath = {}", args[0], args[1]);

        ILogAnalyzer logAnalyzer =
                new LogAnalyzer(
                        new LogAnalyzerParameters(
                                new File(args[0]), new File(args[1])));
        logAnalyzer.analyze();

        LOG.info("<< main");
    }
}
