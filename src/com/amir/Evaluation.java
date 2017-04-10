package com.amir;

import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Evaluator;

import java.io.*;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.Vector;

public class Evaluation {

    public Evaluation() {
    }
    public static void PrintPRecEval(Alignment ref, Alignment alignment)
    {
        try {
            PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter( System.out, "UTF-8" )),false);
            Evaluator evaluator = new PRecEvaluator(ref, alignment);
            evaluator.eval(System.getProperties());
            evaluator.write(writer);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static String getTableContents(int n[][])
    {
        String outStr = "";
        int sz = n[0].length;
        for (int i = 0; i < sz; ++i) {
            outStr += String.format("%12d", n[i][0]);
            for (int j = 1; j < sz; ++j) {
                outStr += "\t" + String.format("%12d", n[i][j]);
            }
            outStr += "\n";
        }
        return outStr;
    }
    public static void evalPaper(Vector<Alignment> result, Alignment ref, Vector<String> mNames, PrintStream writer)
    {
        try {
            int NumberOfAlgorithms = result.size();
            int n00[][] = new int[NumberOfAlgorithms][NumberOfAlgorithms];
            int n10[][] = new int[NumberOfAlgorithms][NumberOfAlgorithms];
            int n11[][] = new int[NumberOfAlgorithms][NumberOfAlgorithms];

            for (int i = 0; i < NumberOfAlgorithms; ++i) {
                Alignment first = result.get(i);
                Alignment firstTrue = first.meet(ref); // eshterak
                for (int j = 0; j < NumberOfAlgorithms; ++j) {
                    if (j == i)
                    {
                        n10[i][j] = 0;
                        continue;
                    }
                    Alignment second = result.get(j);
                    Alignment ans = firstTrue.diff(second);

                    n10[i][j] = ans.nbCells();
                }
            }

            for (int i = 0; i < NumberOfAlgorithms; ++i) {
                Alignment first = result.get(i);
                for (int j = i; j < NumberOfAlgorithms; ++j) {
                    Alignment second = result.get(j);
                    Alignment ans = ref.diff(first);
                    ans = ans.diff(second);
                    n00[j][i] = n00[i][j] = ans.nbCells();

                    ans = ref.meet(first);
                    ans = ans.meet(second);
                    n11[j][i] = n11[i][j] = ans.nbCells();
                }
            }

            // Printing
            String nameMethods = String.format("%12s", mNames.get(0));
            for (int i = 1; i < NumberOfAlgorithms; ++i) {
                nameMethods += "\t" + String.format("%12s", mNames.get(i));
            }

            writer.println("n10 & n01 = \n");
            writer.println(nameMethods);
            writer.println(getTableContents(n10));

            writer.println("\nn11 = \n");
            writer.println(nameMethods);
            writer.println(getTableContents(n11));

            writer.println("\nn00 = \n");
            writer.println(nameMethods);
            writer.println(getTableContents(n00));

            PrintStream printStream = new PrintStream( new FileOutputStream( "./resultPaper1.tex"  ) );
            printMatrix(mNames, n10, printStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void evalPaper2(Vector<Alignment> result, Alignment ref, Vector<String> mNames, PrintStream writer)
    {
        try {
            int NumberOfAlgorithms = result.size();
            int n00[][] = new int[NumberOfAlgorithms][NumberOfAlgorithms];
            int n10[][] = new int[NumberOfAlgorithms][NumberOfAlgorithms];
            int n11[][] = new int[NumberOfAlgorithms][NumberOfAlgorithms];

            for (int i = 0; i < NumberOfAlgorithms; ++i) {
                Alignment first = result.get(i);
                Alignment firstTrue = first.meet(ref); // eshterak
                Alignment joins = first.join(ref); // union
                for (int j = 0; j < NumberOfAlgorithms; ++j) {
                    Alignment second = result.get(j);
                    Alignment ans = firstTrue.diff(second);

                    Alignment ans2 = second.diff(joins);

                    n10[i][j] = ans.nbCells() + ans2.nbCells();
                }
            }

            // Printing
            String nameMethods = String.format("%12s", mNames.get(0));
            for (int i = 1; i < NumberOfAlgorithms; ++i) {
                nameMethods += "\t" + String.format("%12s", mNames.get(i));
            }

            writer.println("n10 & n01 = \n");
            writer.println(nameMethods);
            writer.println(getTableContents(n10));

            PrintStream printStream = new PrintStream( new FileOutputStream( "./resultPaper2.tex"  ) );
            printMatrix(mNames, n10, printStream);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void printMatrix( Vector<String> labels, int matrix[][], PrintStream writer ) {
        // Number format class to format the values
        int nb1 = labels.size();
        writer.print("\\begin{tabular}{r|");
        for ( int i = 0; i < nb1 ; i++ ) writer.print("c");
        writer.println("}");
        try {
            for( String label : labels ){
                writer.print(" & \\rotatebox{90}{"+ label +"}");
            }
            writer.println(" \\\\ \\hline");
            for ( int i = 0; i < nb1; ++i ) {
                writer.print( labels.get(i) );
                for ( int j = 0; j < nb1; ++j ){
                    writer.print(" & "+String.format("%d", matrix[i][j]));
                }
                writer.println("\\\\");
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        writer.println("\n\\end{tabular}");
    }
    public static void printHTML(Vector<Vector<Object>> result, PrintStream writer, String format, Vector<String> listAlgo) {
        // variables for computing iterative harmonic means
        int expected = 0; // expected so far
        int foundVect[]; // found so far
        int correctVect[]; // correct so far
        long timeVect[]; // time so far
        Formatter formatter = new Formatter(writer);
        String color = "gray";
        int fsize = format.length();
        int algoSize = listAlgo.size();
        // JE: the h-means computation should be put out as well
        // Print the header
        writer.println("<html><head></head><body>");
        writer.println("<table border='2' frame='sides' rules='groups'>");
        writer.println("<colgroup align='center' />");
        // for each algo <td spancol='2'>name</td>
        for (int i = 0; i < algoSize; ++i) {
            writer.println("<colgroup align='center' span='"+fsize+"' />");
        }
        // For each file do a
        writer.println("<thead valign='top'><tr><th>algo</th>");
        // for each algo <td spancol='2'>name</td>
        for ( String m : listAlgo ) {
            writer.println("<th colspan='"+fsize+"'>"+m+"</th>");
        }
        writer.println("</tr></thead><tbody><tr><td>test</td>");
        // for each algo <td>Prec.</td><td>Rec.</td>
        for (int j = 0; j < algoSize; ++j) {
            for ( int i = 0; i < fsize; i++){
                writer.print("<td>");
                if ( format.charAt(i) == 'p' ) {
                    writer.print("Prec.");
                } else if ( format.charAt(i) == 'f' ) {
                    writer.print("FMeas.");
                } else if ( format.charAt(i) == 'o' ) {
                    writer.print("Over.");
                } else if ( format.charAt(i) == 't' ) {
                    writer.print("Time");
                } else if ( format.charAt(i) == 'r' ) {
                    writer.print("Rec.");
                }
                writer.println("</td>");
            }
            //writer.println("<td>Prec.</td><td>Rec.</td>");
        }
        writer.println("</tr></tbody><tbody>");
        int size = listAlgo.size();
        foundVect = new int[ size ];
        correctVect = new int[ size ];
        timeVect = new long[ size ];
        for( int k = size-1; k >= 0; k-- ) {
            foundVect[k] = 0;
            correctVect[k] = 0;
            timeVect[k] = 0;
        }
        // </tr>
        // For each directory <tr>
        boolean colored = false;
        for ( Vector<Object> test : result ) {
            int nexpected = -1;
            if ( colored ){
                colored = false;
                writer.println("<tr bgcolor=\""+color+"\">");
            } else {
                colored = true;
                writer.println("<tr>");
            };
            // Print the directory <td>bla</td>
            writer.println("<td>"+ test.get(0).toString()+"</td>");
            // For each record print the values <td>bla</td>
            Enumeration<Object> f = test.elements();
            f.nextElement();
            for( int k = 0 ; f.hasMoreElements() ; k++) {
                PRecEvaluator eval = (PRecEvaluator)f.nextElement();
                if ( eval != null ){
                    // iterative H-means computation
                    if ( nexpected == -1 ){
                        expected += eval.getExpected();
                        nexpected = 0;
                    }
                    foundVect[k] += eval.getFound();
                    correctVect[k] += eval.getCorrect();
                    timeVect[k] += eval.getTime();

                    for ( int i = 0 ; i < fsize; i++){
                        writer.print("<td>");
                        if ( format.charAt(i) == 'p' ) {
                            formatter.format("%1.2f", eval.getPrecision());
                        } else if ( format.charAt(i) == 'f' ) {
                            formatter.format("%1.2f", eval.getFmeasure());
                        } else if ( format.charAt(i) == 'o' ) {
                            formatter.format("%1.2f", eval.getOverall());
                        } else if ( format.charAt(i) == 't' ) {
                            if ( eval.getTime() == 0 ){
                                writer.print("-");
                            } else {
                                formatter.format("%1.2f", eval.getTime() / 1000.0);
                            }
                        } else if ( format.charAt(i) == 'r' ) {
                            formatter.format("%1.2f", eval.getRecall());
                        }
                        writer.print("</td>");
                    }
                } else {
                    for ( int i = 0 ; i < fsize; i++) writer.print("<td>n/a</td>");
                }
            }
            writer.println("</tr>");
        }
        writer.print("<tr bgcolor=\"yellow\"><td>H-mean</td>");
        // Here we are computing a sheer average.
        // While in the column results we print NaN when the returned
        // alignment is empty,
        // here we use the real values, i.e., add 0 to both correctVect and
        // foundVect, so this is OK for computing the average.
        int k = 0;
        // ???
        for ( String m : listAlgo ) {
            double precision = (double)correctVect[k]/foundVect[k];
            double recall = (double)correctVect[k]/expected;
            for ( int i = 0 ; i < fsize; i++){
                writer.print("<td>");
                if ( format.charAt(i) == 'p' ) {
                    formatter.format("%1.2f", precision);
                } else if ( format.charAt(i) == 'f' ) {
                    formatter.format("%1.2f", 2 * precision * recall / (precision + recall));
                } else if ( format.charAt(i) == 'o' ) {
                    formatter.format("%1.2f", recall * (2 - (1 / precision)));
                } else if ( format.charAt(i) == 't' ) {
                    if ( timeVect[k] == 0 ){
                        writer.print("-");
                    } else {
                        formatter.format("%1.2f", timeVect[k] / 1000.0);
                    }
                } else if ( format.charAt(i) == 'r' ) {
                    formatter.format("%1.2f", recall);
                }
                writer.println("</td>");
            }
            k++;
        }
        writer.println("</tr>");
        writer.println("</tbody></table>");
        writer.println("<p><small>n/a: result alignment not provided or not readable<br />");
        writer.println("NaN: division per zero, likely due to empty alignment.</small></p>");
        writer.println("</body></html>");
        writer.close();
    }
}
