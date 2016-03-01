package com.aboutsip.performance.core.sipp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 */
public class SIPpTestBase {

    public BufferedReader loadResourceFile(final String resource) {
        return new BufferedReader(new InputStreamReader(StatsLabelsTest.class.getResourceAsStream(resource)));
    }

    public String getFirstLineOfResource(final String resource) throws IOException {
        return loadResourceFile(resource).readLine();
    }

}
