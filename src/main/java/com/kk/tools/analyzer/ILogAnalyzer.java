package com.kk.tools.analyzer;

import java.util.Map;

/**
 * Author: PDudin
 * Created date: 03.08.2017.
 */
public interface ILogAnalyzer {

    void analyze();

    Map<String, Integer> getStatByPatternName(String patternName);
}
