package com.kk.tools.analyzer;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Map;

/**
 * Author: PDudin
 * Created date: 03.08.2017.
 */
public class LogAnalyzerTest {

    @Test
    public void testOnePattern() {
        ILogAnalyzer logAnalyzer =
                new LogAnalyzer(
                        new LogAnalyzerParameters(
                                new File(getClass().getClassLoader().getResource("test.log").getFile()),
                                new File(getClass().getClassLoader().getResource("parameters/1pattern.txt").getFile()))
        );
        logAnalyzer.analyze();

        String patternName = ".*Got \\d+ payments from db to check, id = '(\\d+)'";
        Map<String, Integer> stat = logAnalyzer.getStatByPatternName(patternName);
        Assert.assertEquals(new Integer(2), stat.get("112"));
        Assert.assertEquals(new Integer(1), stat.get("113"));
        Assert.assertEquals(new Integer(1), stat.get("114"));
        Assert.assertEquals(new Integer(1), stat.get("115"));
        Assert.assertNull(stat.get("116"));
        Assert.assertEquals(new Integer(1), stat.get("117"));
        Assert.assertEquals(new Integer(1), stat.get("118"));
        Assert.assertEquals(new Integer(1), stat.get("119"));
    }

    @Test
    public void testTwoPattern() {
        ILogAnalyzer logAnalyzer =
                new LogAnalyzer(
                        new LogAnalyzerParameters(
                                new File(getClass().getClassLoader().getResource("test.log").getFile()),
                                new File(getClass().getClassLoader().getResource("parameters/2pattern.txt").getFile()))
        );
        logAnalyzer.analyze();

        String patternName = ".*Got \\d+ payments from db to check, id = '(\\d+)'";
        Map<String, Integer> stat = logAnalyzer.getStatByPatternName(patternName);
        Assert.assertEquals(new Integer(2), stat.get("112"));
        Assert.assertEquals(new Integer(1), stat.get("113"));
        Assert.assertEquals(new Integer(1), stat.get("114"));
        Assert.assertEquals(new Integer(1), stat.get("115"));
        Assert.assertNull(stat.get("116"));
        Assert.assertEquals(new Integer(1), stat.get("117"));
        Assert.assertEquals(new Integer(1), stat.get("118"));
        Assert.assertEquals(new Integer(1), stat.get("119"));

        patternName = ".*<< checkPaymentStatus finished at .*, id = '(\\d+)'";
        stat = logAnalyzer.getStatByPatternName(patternName);
        Assert.assertEquals(new Integer(1), stat.get("112"));
        Assert.assertNull(stat.get("113"));
        Assert.assertEquals(new Integer(1), stat.get("114"));
        Assert.assertEquals(new Integer(1), stat.get("115"));
        Assert.assertNull(stat.get("116"));
        Assert.assertEquals(new Integer(1), stat.get("117"));
        Assert.assertEquals(new Integer(1), stat.get("118"));
        Assert.assertEquals(new Integer(1), stat.get("119"));
    }

    // TODO: (pdudin) check print methods... full stat and standart stat
    // TODO: (pdudin) LogAnalyzerParametersTest 'init' method test - diffirent validations (success and failed)
}
