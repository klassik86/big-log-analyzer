package com.kk.tools.analyzer;

import com.kk.tools.exception.LogAnalyzerValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Author: PDudin
 * Created date: 03.08.2017.
 */
public class LogAnalyzerParameters implements ILogAnalyzerParameters {

    private static final Logger LOG = LoggerFactory.getLogger(LogAnalyzerParameters.class);

    private List<Pattern> patterns;

    private List<String> ids;

    private int patternsCount;

    private File logFile;

    private File parametersFile;

    public LogAnalyzerParameters(File logFile, File parametersFile) {
        this.logFile = logFile;
        this.parametersFile = parametersFile;

        this.patterns = new ArrayList<>();
        this.ids = new ArrayList<>();
    }

    @Override
    public void init() throws IOException, LogAnalyzerValidationException {
        List<String> lines = Files.readAllLines(parametersFile.toPath());

        checkAndInitPatternsCount(lines);
        checkAndInitPatterns(lines);
        checkAndInitIds(lines);
    }

    private void checkAndInitPatternsCount(List<String> lines) throws LogAnalyzerValidationException {
        if (lines.size() <= 1) {
            throw new LogAnalyzerValidationException("Parameters` file is empty");
        }

        /*read and check first parameter 'patternsCount'*/
        String line = lines.get(0);
        try {
            patternsCount = Integer.valueOf(line);
            if (patternsCount <= 0) {
                throw new LogAnalyzerValidationException(
                        String.format("Patterns count = %s is not positive integer (must be positive integer)", line));
            }
        } catch (NumberFormatException e) {
            throw new LogAnalyzerValidationException(
                    String.format("Patterns count = %s is not integer (must be positive integer)", line));
        }
    }

    private void checkAndInitPatterns(List<String> lines) throws LogAnalyzerValidationException {
        /*read and check patterns` parameters*/
        if (lines.size() < 1 + patternsCount) {
            throw new LogAnalyzerValidationException(
                    String.format("Patterns count less than expected: expected = %d, actual = %d", patternsCount,
                            lines.size() - 1));
        }
        for (int i = 1; i < patternsCount + 1; i++) {
            Pattern pattern = Pattern.compile(lines.get(i));
            patterns.add(pattern);
            // TODO: (pdudin) check on grouping in defined patterns - '(.+) is present'
        }
    }

    private void checkAndInitIds(List<String> lines) {
        /*read and check ids` parameters*/
        if (lines.size() < 1 + patternsCount + 1) {
            LOG.debug("There are not any id, so will calc full stat");
        } else {
            for (int i = patternsCount + 1; i < lines.size(); i++) {
                ids.add(lines.get(i));
            }
        }
    }

    @Override
    public List<Pattern> getPatterns() {
        return patterns;
    }

    @Override
    public List<String> getIds() {
        return ids;
    }

    @Override
    public File getLogFile() {
        return logFile;
    }
}
