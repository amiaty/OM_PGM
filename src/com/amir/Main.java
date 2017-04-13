package com.amir;
import fr.inrialpes.exmo.ontosim.string.JWNLDistances;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Evaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import java.net.URI;
import java.util.*;

@SuppressWarnings("Duplicates")
public class Main {
    final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // write your code here
        Scanner reader = new Scanner(System.in);
        System.out.println("Enter a number: ");
        int testNumber = reader.nextInt();
        switch (testNumber)
        {
            case 0:
                testAlign();
                break;
            case 1:
                testMatcher();
                break;
            case 2:
                testGroupEvaluation();
                break;
            case 3:
                testEvaluationSingle();
                break;
            case 4:
                testEvaluationPaper();
                break;
            case 5:
                testEvaluationPaperNew();
                break;
            case 6:
                runBenchmarks();
                break;
            case 9:
                testJWNL();
                break;
        }
    }
    // Choice number 0
    private static void testAlign() {
        try {
            //init and matching
            //URI uri1 = new URI("file:./res/example/myOnto.owl");
            //URI uri2 = new URI("file:./res/example/edu.mit.visus.bibtex.owl");
            URI uri1 = new URI("file:./res/anatomy-onto/mouse.owl");
            URI uri2 = new URI("file:./res/anatomy-onto/human.owl");
            AMAlignment2 alignment = new AMAlignment2();
            alignment.init(uri1, uri2);

            long startTime = System.currentTimeMillis();
            alignment.align(null, System.getProperties());
            System.out.println(String.format("\nTime : %d sec", (System.currentTimeMillis() - startTime) / 1000));
            System.out.println("Our method nCells: " + alignment.nbCells());
            //matching output
            //OutputStream stream = System.out;
            OutputStream stream = new FileOutputStream( "./res/anatomy-alignments/OurResult.rdf", false );
            //OutputStream stream = new FileOutputStream( "./res/example/OurResult.rdf", false );
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter( stream, "UTF-8" )),false);
            AlignmentVisitor rendererVisitor = new RDFRendererVisitor(printWriter);
            alignment.render(rendererVisitor);

            printWriter.flush();
            printWriter.close();

            //evaluation
            AlignmentParser alignmentParser = new AlignmentParser(0);
            Alignment ref = alignmentParser.parse("file:./res/anatomy-onto/reference.rdf");
            //Alignment ref = alignmentParser.parse("file:./res/example/reference.rdf");
            ref.init(uri1, uri2);
            ref.harden(0.01);
            Evaluator evaluator = new PRecEvaluator(ref, alignment);
            evaluator.eval(System.getProperties());

            //print eval res
            OutputStream stream1 = System.out;
            PrintWriter printWriter1 = new PrintWriter(new BufferedWriter(new OutputStreamWriter( stream1, "UTF-8" )),false);
            evaluator.write(printWriter1);
            printWriter1.flush();
            printWriter1.close();

        } catch (Exception e) {
            e.printStackTrace();
            logger.debug(e.getMessage());
        }
    }

    private static void testMatcher() {
        try {
            URI uri1 = new URI("file:./res/example/edu.mit.visus.bibtex.owl");
            URI uri2 = new URI("file:./res/example/myOnto.owl");
            MyAlignment myAlignment = new MyAlignment();
            myAlignment.init(uri1, uri2);
            myAlignment.align( null, new Properties());

            //OutputStream stream = new FileOutputStream( "./result.html" );
            OutputStream stream = System.out;
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter( stream, "UTF-8" )),false);
            AlignmentVisitor rendererVisitor = new HTMLRendererVisitor(printWriter);
            myAlignment.render(rendererVisitor);
            printWriter.flush();
            printWriter.close();
        } catch (Exception e) {
            logger.debug(e.getMessage());
        }
    }

    private static void testEvaluationPaper() {
        try {

            URI uri1 = new URI("file:./res/anatomy-onto/mouse.owl");
            URI uri2 = new URI("file:./res/anatomy-onto/human.owl");

            File[] dir = (new File("res/anatomy-alignments-new")).listFiles();
            assert dir != null;
            System.out.println("Number of file(s): " + dir.length);
            AlignmentParser alignmentParser = new AlignmentParser(0);
            Alignment ref = alignmentParser.parse("file:./res/anatomy-onto/reference.rdf");
            ref.init(uri1, uri2);
            ref.harden(0.01);

            Vector<Alignment> algns = new Vector<>();
            Vector<String> methodNames = new Vector<>();
            for (File file : dir)
            {
                methodNames.add(file.getName().replaceFirst("[.][^.]+$", ""));
                Alignment al = alignmentParser.parse(file.toURI());
                al.init(uri1, uri2);
                al.harden(0.5);
                algns.add(al);
            }
            PrintStream printStream1 = new PrintStream( new FileOutputStream( "./resultPaper1new.txt"  ) );
            Evaluation.evalPaper(algns, ref, methodNames, printStream1);
            PrintStream printStream2 = new PrintStream( new FileOutputStream( "./resultPaper2new.txt"  ) );
            Evaluation.evalPaper2(algns, ref, methodNames, printStream2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testEvaluationPaperNew() {
        try {

            URI uri1 = new URI("file:./res/anatomy-onto/mouse.owl");
            URI uri2 = new URI("file:./res/anatomy-onto/human.owl");

            AlignmentParser alignmentParser = new AlignmentParser(0);
            Alignment ref = alignmentParser.parse("file:./res/anatomy-onto/reference.rdf");
            ref.init(uri1, uri2);
            ref.harden(0.01);

            Vector<String> methodNames = new Vector<>(Arrays.asList(new String[]{"Levenshtein", "SMOA", "JaroWinkler", "N-gram", "Equal", "Hamming", "Jaro", "NeedlemanWunsch2", "SubString"}));
            Properties properties = new Properties();

            long startTime = System.currentTimeMillis();
            for (String method:methodNames)
            {
                properties.setProperty("StringDistanceMethod", method);
                PaperAlignment alignment = new PaperAlignment();
                alignment.init(uri1, uri2);
                alignment.align(null, properties);
                alignment.harden(0.1);
                System.out.println(String.format("\nTime : %d min", (System.currentTimeMillis() - startTime) / 60000));
                OutputStream stream = new FileOutputStream( "./res/anatomy-alignments-new/" + method + ".rdf", false );
                PrintWriter printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter( stream, "UTF-8" )),false);
                AlignmentVisitor rendererVisitor = new RDFRendererVisitor(printWriter);
                alignment.render(rendererVisitor);
                printWriter.flush();
                printWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Choice number 3
    private static void testEvaluationSingle() {

        try {
            OutputStream stream = System.out;
            PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter( stream, "UTF-8" )),false);
            AlignmentVisitor renderer = new OWLAxiomsRendererVisitor(writer);
            URI uri1 = new URI("file:./res/anatomy-onto/mouse.owl");;
            URI uri2 = new URI("file:./res/anatomy-onto/human.owl");

            AlignmentParser alignmentParser = new AlignmentParser(0);

            Alignment ref = alignmentParser.parse("file:./res/anatomy-onto/reference.rdf");
            ref.init(uri1, uri2);
            Alignment a1 = alignmentParser.parse("file:./res/anatomy-alignments-new/Levenshtein.rdf");
            a1.init(uri1, uri2);
            ref.harden(0.01);
            a1.harden(0.01);

            Evaluator e1 = new PRecEvaluator(ref, a1);
            e1.eval(System.getProperties());

            a1.render(renderer);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug(e.getMessage());
        }
    }
    // Choice number 2
    private static void testGroupEvaluation() {
        try {
            URI uri1 = new URI("file:./res/example/myOnto.owl");
            URI uri2 = new URI("file:./res/example/edu.mit.visus.bibtex.owl");

            //URI uri1 = new URI("file:./res/anatomy-onto/mouse.owl");
            //URI uri2 = new URI("file:./res/anatomy-onto/human.owl");

            //File[] dir = (new File("res/anatomy-alignments")).listFiles();
            File[] dir = (new File("res/example")).listFiles((FileFilter)new WildcardFileFilter("*.rdf"));
            assert dir != null;
            System.out.println("Number of file(s): " + dir.length);
            AlignmentParser alignmentParser = new AlignmentParser(0);
            //Alignment ref = alignmentParser.parse("file:./res/anatomy-onto/reference.rdf");
            Alignment ref = alignmentParser.parse("file:./res/example/reference.rdf");
            ref.init(uri1, uri2);
            ref.harden(0.01);

            Vector<String> listAlgo = new Vector<>();
            Vector<Object> ee1 = new Vector<>();
            ee1.add("Test1");

            for (File file : dir)
            {
                listAlgo.add(file.getName().replaceFirst("[.][^.]+$", ""));
                Alignment alignment = alignmentParser.parse(file.toURI());
                alignment.init(uri1, uri2);
                alignment.harden(0.01);
                Evaluator evaluator = new PRecEvaluator(ref, alignment);
                evaluator.eval(System.getProperties());

                ee1.add(evaluator);
            }
            Vector<Vector<Object>> vector = new Vector<>();
            vector.add(ee1);
            PrintStream printStream = new PrintStream( new FileOutputStream( "./result1.html"  ) );
            Evaluation.printHTML(vector,printStream,"prf", listAlgo);
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug(e.getMessage());
        }
    }
    // Choice number 6
    private static void runBenchmarks()
    {
        try {
            URI uri1 = new URI("file:./res/benchmarks/101/onto.rdf");
            URI uri2 = new URI("file:./res/benchmarks/101/onto.rdf");

            AlignmentParser alignmentParser = new AlignmentParser(0);
            Alignment ref = alignmentParser.parse("file:./res/benchmarks/101/refalign.rdf");
            ref.init(uri1, uri2);
            ref.harden(0.01);

            AMAlignment alignment = new AMAlignment();
            alignment.init(uri1, uri2);
            Properties properties = new Properties();
            // ObjType : class, property, data_property, object_property, individual, all
            properties.setProperty("ObjType", "class");
            alignment.align(null, properties);
            Evaluation.PrintPRecEval(ref, alignment);

            properties.setProperty("ObjType", "property");
            alignment.align(alignment, properties);
            Evaluation.PrintPRecEval(ref, alignment);

            OutputStream stream = new FileOutputStream( "./res/benchmarks/101/OurResult.rdf", false );
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter( stream, "UTF-8" )),false);
            AlignmentVisitor rendererVisitor = new RDFRendererVisitor(printWriter);
            alignment.render(rendererVisitor);
            printWriter.flush();
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Choice number 9
    private static void testJWNL() {
        JWNLDistances Dist = new JWNLDistances();
        Dist.Initialize("./dict", "3.1");
        System.out.println(Dist.computeSimilarity("blue", "sky"));
        System.out.println(Dist.basicSynonymDistance("good", "good"));
        System.out.println(Dist.wuPalmerSimilarity("blue", "sky"));
        System.out.println(Dist.cosynonymySimilarity("blue", "bag"));
    }
}
