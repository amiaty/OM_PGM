package com.amir;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Evaluator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import java.net.URI;
import java.util.*;


import uk.ac.ox.krr.logmap2.LogMap2_RepairFacility;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;
import uk.ac.ox.krr.logmap2.oaei.reader.MappingsReaderManager;
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
                repairAlignment();
                break;
        }
    }
    private static void testAlign() {
        try {
            //URI uri1 = new URI("file:./res/example/edu.mit.visus.bibtex.owl");
            //URI uri2 = new URI("file:./res/example/myOnto.owl");
            //init and matching
            URI uri1 = new URI("file:./res/anatomy-onto/mouse.owl");
            URI uri2 = new URI("file:./res/anatomy-onto/human.owl");
            AMAlignment alignment = new AMAlignment();
            alignment.init(uri1, uri2);

            long startTime = System.currentTimeMillis();
            alignment.align(null, System.getProperties());
            System.out.println(String.format("\nTime : %d sec", (System.currentTimeMillis() - startTime) / 1000));
            System.out.println("Our method nCells: " + alignment.nbCells());
            //matching output
            //OutputStream stream = System.out;
            OutputStream stream = new FileOutputStream( "./res/anatomy-alignments/OurResult.rdf", false );
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter( stream, "UTF-8" )),false);
            AlignmentVisitor rendererVisitor = new RDFRendererVisitor(printWriter);
            alignment.render(rendererVisitor);

            printWriter.flush();
            printWriter.close();

            //evaluation
            AlignmentParser alignmentParser = new AlignmentParser(0);
            Alignment ref = alignmentParser.parse("file:./res/anatomy-onto/reference.rdf");
            ref.init(uri1, uri2);
            ref.harden(0.01);
            Evaluator evaluator = new PRecEvaluator(ref, alignment);
            evaluator.eval(System.getProperties());

            //print eval res
            OutputStream stream1 = System.out;
            PrintWriter printWriter1 = new PrintWriter(new BufferedWriter(new OutputStreamWriter( stream1, "UTF-8" )),true);
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
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter( stream, "UTF-8" )),true);
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

            File[] dir = (new File("res/anatomy-alignments")).listFiles();
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
                al.harden(0.01);
                algns.add(al);
            }
            PrintStream printStream1 = new PrintStream( new FileOutputStream( "./resultPaper1.txt"  ) );
            Evaluation.evalPaper(algns, ref, methodNames, printStream1);
            PrintStream printStream2 = new PrintStream( new FileOutputStream( "./resultPaper2.txt"  ) );
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

            Vector<Alignment> algns = new Vector<>();
            Vector<String> methodNames = new Vector<>(Arrays.asList(new String[]{"Levenshtein", "SMOA", "JaroWinkler", "N-gram", "Equal", "Hamming", "Jaro", "NeedlemanWunsch2", "SubString"}));
            Properties properties = new Properties();

            long startTime = System.currentTimeMillis();
            for (String method:methodNames)
            {
                properties.setProperty("StringDistanceMethod", method);
                PaperAlignment alignment = new PaperAlignment();
                alignment.init(uri1, uri2);
                alignment.align(null, properties);
                alignment.harden(0.3);
                algns.add(alignment);
                System.out.println(String.format("\nTime : %d sec", (System.currentTimeMillis() - startTime) / 1000));
            }

            PrintStream printStream1 = new PrintStream( new FileOutputStream( "./resultPaperNew1.txt"  ) );
            Evaluation.evalPaper(algns, ref, methodNames, printStream1);
            PrintStream printStream2 = new PrintStream( new FileOutputStream( "./resultPaperNew2.txt"  ) );
            Evaluation.evalPaper2(algns, ref, methodNames, printStream2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void repairAlignment () {

        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology onto1 = manager.loadOntology(IRI.create("file:./res/anatomy-onto/mouse.owl"));
            OWLOntology onto2 = manager.loadOntology(IRI.create("file:./res/anatomy-onto/human.owl"));
            MappingsReaderManager readermanager = new MappingsReaderManager( "file:./res/anatomy-alignments/LogMap.rdf", "RDF");
            Set<MappingObjectStr> input_mappings = readermanager.getMappingObjects();
            LogMap2_RepairFacility logmap2_repair = new LogMap2_RepairFacility( onto1, onto2, input_mappings, false, false);
            //Set of mappings repaired by LogMap
            Set<MappingObjectStr> repaired_mappings = logmap2_repair.getCleanMappings();
            System.out.println("Num repaired mappings using LogMap: " + repaired_mappings.size());

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
            logger.debug(e.getMessage());
        }

    }
    private static void testEvaluationSingle() {

        try {
            //OutputStream stream = new FileOutputStream( "./result1.rdf" );
            OutputStream stream = System.out;
            PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter( stream, "UTF-8" )),true);
            AlignmentVisitor renderer = new OWLAxiomsRendererVisitor(writer);
            URI uri1 = new URI("file:./res/example/edu.mit.visus.bibtex.owl");
            URI uri2 = new URI("file:./res/example/myOnto.owl");

            AlignmentParser alignmentParser = new AlignmentParser(0);

            Alignment ref = alignmentParser.parse("file:./res/example/reference.rdf");
            ref.init(uri1, uri2);
            Alignment a1 = alignmentParser.parse("file:./res/example/jwnl.rdf");
            a1.init(uri1, uri2);
            Alignment a2 = alignmentParser.parse("file:./res/example/equal.rdf");
            a2.init(uri1, uri2);
            Alignment a3 = alignmentParser.parse("file:./res/example/levenshtein.rdf");
            a3.init(uri1, uri2);
            ref.harden(0.01);
            a1.harden(0.01);
            a2.harden(0.01);
            a3.harden(0.01);

            //Evaluator e1 = new PRecEvaluator(ref, a3);
            //e1.eval(System.getProperties());
            //a3 = a2;

            System.err.println(ref.nbCells());
            System.err.println(a1.nbCells());
            System.err.println(a3.nbCells());

            Alignment a5 = (BasicAlignment) a3.diff(ref).clone();
            System.err.println(a5.nbCells());

            BasicAlignment a6 = (BasicAlignment) a5.join(a2).clone();

            System.err.println(a6.nbCells());

            BasicAlignment a7 = (BasicAlignment) ref.diff(a6).clone();

            System.err.println(a7.nbCells());
            a1.render(renderer);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug(e.getMessage());
        }
    }
    private static void testGroupEvaluation() {
        try {

            URI uri1 = new URI("file:./res/anatomy-onto/mouse.owl");
            URI uri2 = new URI("file:./res/anatomy-onto/human.owl");

            File[] dir = (new File("res/anatomy-alignments")).listFiles();
            assert dir != null;
            System.out.println("Number of file(s): " + dir.length);
            AlignmentParser alignmentParser = new AlignmentParser(0);
            Alignment ref = alignmentParser.parse("file:./res/anatomy-onto/reference.rdf");
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

}
