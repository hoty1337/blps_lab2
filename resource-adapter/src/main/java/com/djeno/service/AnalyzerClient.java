package com.djeno.service;

import com.djeno.model.AnalysisReport;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;

public class AnalyzerClient {

    private static final String INDEX_RESOURCE = "META-INF/spotbugs-lib.index";

    public AnalysisReport analyze(byte[] data) {
        Path tempJar = null;
        Path tempLibDir = null;
        try {
            System.out.println("=== [SPOTBUGS ANALYZE START] ===");

            System.out.println("[1] Reading index of jars...");
            List<String> jarNames = readIndex();
            if (jarNames.isEmpty()) {
                return fail("Не найден " + INDEX_RESOURCE + " в RAR. Нужен RAR с задачами prepareSpotbugsCli/generateLibIndex.");
            }
            System.out.println("[1] JARs from index: " + jarNames);

            System.out.println("[2] Writing uploaded JAR...");
            tempJar = Files.createTempFile("upload-", ".jar");
            Files.write(tempJar, data);
            System.out.println("  Uploaded JAR path: " + tempJar.toAbsolutePath());
            System.out.println("  Uploaded JAR size: " + Files.size(tempJar));

            System.out.println("[3] Building in-VM classloader for SpotBugs...");
            ClassLoader appCl = getClass().getClassLoader();

            List<URL> libUrls = new ArrayList<>();
            for (String name : jarNames) {
                String resPath = "lib/" + name;
                URL url = appCl.getResource(resPath);
                if (url == null) {
                    return fail("Resource not found: " + resPath);
                }
                System.out.println("  + " + url);
                libUrls.add(url);
            }

            System.out.println("[4] Writing uploaded JAR...");
            tempJar = Files.createTempFile("upload-", ".jar");
            Files.write(tempJar, data);
            System.out.println("  Uploaded JAR path: " + tempJar.toAbsolutePath());
            System.out.println("  Uploaded JAR size: " + Files.size(tempJar));

            System.out.println("[5] Invoking SpotBugs in-VM...");
            String[] args = {
                    "-textui",
                    "-effort:max",
                    "-low",
                    tempJar.toAbsolutePath().toString()
            };

            PrintStream oldOut = System.out;
            PrintStream oldErr = System.err;
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ByteArrayOutputStream berr = new ByteArrayOutputStream();
            try (PrintStream pout = new PrintStream(bout, true);
                 PrintStream perr = new PrintStream(berr, true);
                 URLClassLoader spotbugsCl = new URLClassLoader(libUrls.toArray(new URL[0]), appCl)) {

                System.setOut(pout);
                System.setErr(perr);

                Thread current = Thread.currentThread();
                ClassLoader prevTCCL = current.getContextClassLoader();
                current.setContextClassLoader(spotbugsCl);
                try {
                    Class<?> mainCls = Class.forName("edu.umd.cs.findbugs.LaunchAppropriateUI", true, spotbugsCl);
                    Method main = mainCls.getMethod("main", String[].class);
                    main.invoke(null, (Object) args);
                } finally {
                    current.setContextClassLoader(prevTCCL);
                }
            } catch (InvocationTargetException ite) {
                Throwable cause = ite.getTargetException();
                return fail("SpotBugs error: " + cause.getClass().getSimpleName() + ": " + cause.getMessage()
                        + "\n" + bout + "\n" + berr);
            } catch (Exception e) {
                return fail("Exception running SpotBugs in-VM: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            } finally {
                System.setOut(oldOut);
                System.setErr(oldErr);
                System.out.println("=== SpotBugs OUT ===\n" + bout);
                System.out.println("=== SpotBugs ERR ===\n" + berr);
            }

            String output = bout + (berr.size() > 0 ? ("\n[stderr]\n" + berr) : "");
            return new AnalysisReport(true, "SpotBugs OK.\n" + output);


        } catch (Exception e) {
            System.err.println("=== [SPOTBUGS EXCEPTION] ===");
            e.printStackTrace();
            return fail("Exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            safeDelete(tempJar);
            safeDeleteDir(tempLibDir);
            System.out.println("[CLEANUP DONE]");
        }
    }

    private static List<String> readIndex() throws IOException {
        try (InputStream in = AnalyzerClient.class.getClassLoader().getResourceAsStream(INDEX_RESOURCE)) {
            if (in == null) return Collections.emptyList();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                List<String> list = new ArrayList<>();
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) list.add(line);
                }
                return list;
            }
        }
    }

    private static AnalysisReport fail(String msg) { return new AnalysisReport(false, msg); }
    private static void safeDelete(Path p) { if (p != null) try { Files.deleteIfExists(p); } catch (Exception ignore) {} }
    private static void safeDeleteDir(Path dir) {
        if (dir != null) {
            try (var s = Files.list(dir)) { s.forEach(AnalyzerClient::safeDelete); } catch (Exception ignore) {}
            try { Files.deleteIfExists(dir); } catch (Exception ignore) {}
        }
    }
}
