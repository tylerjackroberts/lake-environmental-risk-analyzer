/*  
* LakeAnalyzer
* 
* Author: Tyler Roberts
* First Created: 3/11/2026
* Last Edited: 3/29/2026
*
* Lakes are stored in both a PriorityQueue and an ArrayList to compare performance.
* The program demonstrates how a PriorityQueue improves efficiency when retrieving
* the highest-risk lakes.
*
* Command Line Usage:
* Compile with: javac *.java
* Run with: java LakeAnalyzer numLakes
* where numLakes is an int between 1 and 500 inclusive.
* if there is no command line input, not an int type, or int out of range, run with numLakes = 50 
*/


//Imports
import java.io.*;
import java.util.*;


public class LakeAnalyzer
{
    public static void main(String[] args) throws Exception {
        // ----------- Set up command line input -----------
        int numLakes = 50;
        if (args.length > 0) {
            try {
                int input = Integer.parseInt(args[0]);
                if (input >= 1 && input <= 500) {
                    numLakes = input;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid argument. Using default of 50 lakes.");
            }
        }
        
        // ----------- Data Structures -----------
        PriorityQueue<Lake> pq = new PriorityQueue<>();
        ArrayList<Lake> lakes = new ArrayList<>();
        
        // ----------- Read CSV -----------
        BufferedReader br = new BufferedReader(new FileReader("MaineLakes.csv")); 
        br.readLine(); // skip header
        String line;
        while ((line = br.readLine()) != null) {
            //The following weird line of code is used to split CSVs on commas, excluding commas in quote
            String[] row = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); 
            //Skip the lake if it has no trophic category - typically means lots of null values. 
            if (row[12] == null || row[12].trim().equals("")) continue; 
            Lake lake = new Lake(
                Integer.parseInt(row[2]),
                cleanString(row[3]),
                cleanString(row[21]),
                cleanString(row[22]),
                cleanString(row[27]),
                cleanString(row[12]),
                cleanString(row[28]),
                cleanDouble(row[11]),
                cleanDouble(row[6]),
                cleanDouble(row[4]),
                cleanDouble(row[10])
            );
            RiskModel model = new RiskModel();
            double[] scores = model.runCalculations(lake);
            lake.setScores(scores);

            // Add to both data structures
            pq.add(lake);
            lakes.add(lake);
        }
        br.close();

        // ---------- Time Comparison: ArrayList vs PQ Version ----------
        // PriorityQueue timing
        long startPQ = System.nanoTime();
        runPriorityQueue(new PriorityQueue<>(pq), numLakes);  // copy so original isn't changed
        long endPQ = System.nanoTime();
        // ArrayList timing
        long startList = System.nanoTime();
        runArrayList(new ArrayList<>(lakes), numLakes);  // also use copy 
        long endList = System.nanoTime();

        // Print Out Time Comparison
        System.out.println("\n--- Performance Comparison ---");
        System.out.println("PriorityQueue Time: " + (endPQ - startPQ)/1000 + " microseconds");
        System.out.println("ArrayList Time: " + (endList - startList)/1000 + " microseconds");
        System.out.println("Note that 1 second = 1000000 microseconds");

        //This is where we print top lakes
        System.out.println("\n--- Top Lakes ---");
        printTopLakes(pq, numLakes);

    }


    // ------------------ PQ Version ------------------
    /**
     * Give me the top N highest-risk lakes
     * @param PriorityQueue<Lake> to work with
     * @param int number of lakes
     */ 
    private static void runPriorityQueue(PriorityQueue<Lake> pq, int n)
    {
        for (int i = 0; i < n && !pq.isEmpty(); i++)
        {
            pq.poll();
        }
    }
    // ------------------ ArrayList Version ------------------ 
    /**
     * Give me the top N highest-risk lakes
     * @param ArrayList<Lake> to work with
     * @param int number of lakes
     */
    private static void runArrayList(ArrayList<Lake> lakes, int n)
    {
        for (int i = 0; i < n && !lakes.isEmpty(); i++)
        {
            Lake maxLake = lakes.get(0);
            for (Lake lake : lakes)
            {
                if (lake.compareTo(maxLake) < 0) 
                {
                    maxLake = lake;
                }
            }
            lakes.remove(maxLake);
        }
    }
    // ------------------  Helper for printing top n lakes ------------------ 
    /**
     * Prints top n lakes from pq
     * @param PriorityQueue<Lake> to print from
     * @param int number to print
     */
    private static void printTopLakes(PriorityQueue<Lake> pq, int n)
    {
        for (int i = 0; i < n && !pq.isEmpty(); i++)
        {
            System.out.println(pq.poll());
        }
    }

    // ------------------ Helper Functions for Processing in CSV Values ------------------------
    private static String cleanString(String value)
    {
        if (value == null || value.trim().equals(""))
            return "";
        return value.replace("\"","").trim(); //remove leading and trailing whitespace
    }

    private static double cleanDouble(String value)
    {
        if (value == null || value.trim().equals(""))
            return -1;

        return Double.parseDouble(value.replace(",", "")); //this will remove any comma in a number
    }
}