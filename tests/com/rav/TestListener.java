package com.rav;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.Instant;

public class TestListener implements TestExecutionListener {
    private StringWriter inMemoryWriter = new StringWriter();

    private int numSkippedInCurrentClass;
    private int numAbortedInCurrentClass;
    private int numSucceededInCurrentClass;
    private int numFailedInCurrentClass;
    private Instant startCurrentClass;

    private void resetCountsForNewClass(){
        numSkippedInCurrentClass = 0;
        numAbortedInCurrentClass = 0;
        numSucceededInCurrentClass = 0;
        numFailedInCurrentClass = 0;
        startCurrentClass = Instant.now();
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier){
        if ("[engine:junit-jupiter]".equals(testIdentifier.getParentId().orElse(""))) {
            println("Ran " + testIdentifier.getLegacyReportingName());
            resetCountsForNewClass();
        }
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason){
        numSkippedInCurrentClass++;
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult){
        if ("[engine:junit-jupiter]".equals(testIdentifier.getParentId().orElse(""))){
            int totalTestsInClass = numSucceededInCurrentClass + numAbortedInCurrentClass + numFailedInCurrentClass + numSkippedInCurrentClass;
            Duration duration = Duration.between(startCurrentClass, Instant.now());
            double numSeconds = duration.getNano() / (double) 1_000_000_000;
            String output = String.format("Tests run: %d, Failures: %d, Aborted: %d, Skipped: %d, Time elapsed: %f sec", totalTestsInClass, numFailedInCurrentClass, numAbortedInCurrentClass,numSkippedInCurrentClass,numSeconds);
            println(output);
        }

        if (testIdentifier.getType() == TestDescriptor.Type.TEST){
            if (testExecutionResult.getStatus() == TestExecutionResult.Status.SUCCESSFUL){
                numSucceededInCurrentClass++;
            } else if (testExecutionResult.getStatus() == TestExecutionResult.Status.ABORTED){
                println("  ABORTED: "+testIdentifier.getDisplayName());
                numAbortedInCurrentClass++;
            } else if (testExecutionResult.getStatus() == TestExecutionResult.Status.FAILED){
                println("  FAILED: "+testIdentifier.getDisplayName());
                numFailedInCurrentClass++;
            }
        }
    }

    private void println(String str) {
        inMemoryWriter.write(str + "\n");
    }

    private void flushToDisk() {
        try (FileWriter writer = new FileWriter("build/status-as-tests-run.txt",true)){
            writer.write(inMemoryWriter.toString());
        } catch (IOException e){
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan){
        flushToDisk();
    }
}
