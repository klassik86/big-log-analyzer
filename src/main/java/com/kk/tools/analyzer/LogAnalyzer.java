package com.kk.tools.analyzer;

import com.kk.tools.exception.LogAnalyzerValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Author: PDudin
 * Created date: 03.08.2017.
 */
public class LogAnalyzer implements ILogAnalyzer {

    private static final Logger LOG = LoggerFactory.getLogger(LogAnalyzer.class);

    private Map<String, Map<String, Integer>> resultMap; /*Map<PatternRegexp, Map<Id, Count>>*/

    private ILogAnalyzerParameters parameters;

    public LogAnalyzer(ILogAnalyzerParameters parameters) {
        this.parameters = parameters;
        resultMap = new HashMap<>();
    }

    public void init() throws IOException, LogAnalyzerValidationException {
        parameters.init();
        for (Pattern pattern : parameters.getPatterns()) {
            /*String created by 'new' operator (in Files.readAllLines).
            Keys in map check by 'equals' method, but 'new' String not contains in StringPool.
            And we can not use it like map`s key.
            So put 'pattern name' to StringPool by 'intern' method*/
            resultMap.put(pattern.pattern().intern(), new HashMap<>());
        }
    }

    @Override
    public void analyze() {
        try {
            init();

            int lineCount = calcLogLineCount();
            processLogFile(lineCount);

            printFullStat();
            printStandartStat();

        } catch (LogAnalyzerValidationException e) {
            LOG.error("Error occured while validating file", e);
        } catch (IOException e) {
            LOG.error("Error occured while read file", e);
        }
    }

    @Override
    public Map<String, Integer> getStatByPatternName(String patternName) {
        return resultMap.get(patternName);
    }

    // TODO: (pdudin) add javaDocs to all methods
    private void processLogFile(int lineCount) throws IOException {
        LOG.debug(">> processLogFile > lineCount = {}", lineCount);

        // TODO: (pdudin) add 'trace' logging with details info

        final int[] lineNum = {0};
        final int progressDelta = ((lineCount / 100) * 5) + 1; /*delta = 5% when lines` count is big*/

        /*Read lines in stream instead read all lines, cause file can be very big (defense from OutOfMemoryError)*/
        try (Stream<String> stream = Files.lines(parameters.getLogFile().toPath())) {
            stream.forEach(line -> {
                for (Pattern pattern : parameters.getPatterns()) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        String id = matcher.group(1);
                        addStatistic(pattern.pattern(), id);
                    }
                }

                printProgressLine(++lineNum[0], lineCount, progressDelta);
            });
        }
        printProgressLine(++lineNum[0], lineCount, progressDelta);

        LOG.debug("<< processLogFile");
    }

    private int calcLogLineCount() throws IOException {
        LOG.debug(">> calcLogLineCount");

        int lineCount = 0;
        try (LineNumberReader  lnr = new LineNumberReader(new FileReader(parameters.getLogFile()))) {
            lnr.skip(Long.MAX_VALUE);
            lineCount = lnr.getLineNumber() + 1; //Add 1 because line index starts at 0
        }

        LOG.debug("<< calcLogLineCount < lineCount = {}", lineCount);
        return lineCount;
    }

    private void addStatistic(String patternName, String id) {
        Map<String, Integer> map = resultMap.get(patternName);
        Integer count = Optional.ofNullable(map.get(id)).orElse(0);
        map.put(id, ++count);
    }

    private void printProgressLine(int lineNum, int lineCount, int progressDelta) {
        if (lineNum > lineCount) {
            /*need to fix last empty line problem.*/
            return;
        }
        /*write progress data*/
        if ((progressDelta != 0 && lineNum % progressDelta == 0) || lineNum == lineCount) {
            int percent = lineNum * 100 / lineCount;
            String progressLine = String.join("", Collections.nCopies(percent, "-"));
            LOG.debug("{}> {}%", progressLine, percent);
        }
    }

    private void printFullStat() {
        LOG.debug(">> printFullStat");

        StringBuilder sb = new StringBuilder("FULL stat (all found ids, not only in parameters` file)");
        for (Map.Entry<String, Map<String, Integer>> entry : resultMap.entrySet()) {
            sb.append(String.format("\n*** Pattern Name: %s ***", entry.getKey()));

            for (Map.Entry<String, Integer> stat : entry.getValue().entrySet()) {
                sb.append(String.format("\n%s\t%d", stat.getKey(), stat.getValue()));
            }
        }
        /*Log level set to info, cause this is main app`s report*/
        LOG.info(sb.toString());

        LOG.debug("<< printFullStat");
    }

    private void printStandartStat() {
        LOG.debug(">> printStandartStat");
        if (parameters.getIds().isEmpty()) {
            LOG.debug("skip standart statistic, cause there are not ids in parameters` file");
            return;
        }

        LOG.info("----------------------------------------------------------");

        StringBuilder sb = new StringBuilder("STANDART stat (only ids defined in parameters` file)");
        for (Map.Entry<String, Map<String, Integer>> entry : resultMap.entrySet()) {
            sb.append(String.format("\n*** Pattern Name: %s ***", entry.getKey()));

            for (String id : parameters.getIds()) {
                Integer count = entry.getValue().get(id);
                sb.append(String.format("\n%s\t%d",id, Optional.ofNullable(count).orElse(0)));
            }
        }
        /*Log level set to info, cause this is main app`s report*/
        LOG.info(sb.toString());

        LOG.debug("<< printStandartStat");
    }

}
