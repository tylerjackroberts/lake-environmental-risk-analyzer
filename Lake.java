/*  
* Lake
* 
* Author: Tyler Roberts
* First Created: 2/23/2026
* Last Edited: 3/11/2026
* Represents a lake and stores environmental attributes used to calculate 
* ecological risk and confidence scores. Instances of this class are populated 
* from CSV data and later analyzed by the RiskModel.
*/

public class Lake implements Comparable<Lake> {

    //Attribes of our Lake objects
    private int lakeCode;
    private String name;
    private String town;
    private String county;

    //Attributes used in Risk Score
    private String waterQualityStatement; //if null pass in an empty string
    private String trophicZone;
    private String invasive;
    private double flushingRate; //if null pass in -1
    private double lakeDepth; //use mean //if null pass in -1
    private double lakeSize; //if null pass in -1
    private double totalDrainageArea; //divided by area to get drainage ratio 
    

    //Key Attributes Calc Outside Lake Class
    private double riskScore;
    private double confidenceScore;


    // --------------------------------------- Constructor ---------------------------------------
    public Lake(int lakeCode, String name, String town, String county, 
        String waterQualityStatement, String trophicZone, String invasive, 
        double flushingRate, double lakeDepth, double lakeSize, double totalDrainageArea) 
        {
        this.lakeCode=lakeCode;
        this.name=name;
        this.town=town;
        this.county=county;
        this.waterQualityStatement=waterQualityStatement;
        this.trophicZone=trophicZone;
        this.invasive=invasive;
        this.flushingRate=flushingRate;
        this.lakeDepth=lakeDepth;
        this.lakeSize=lakeSize;
        this.totalDrainageArea=totalDrainageArea;
        }



    // --------------------------------------- Getters ---------------------------------------
    
    public String getWaterQualityStatement()
    {
        return waterQualityStatement;
    }

    public String getTrophicZone()
    {
        return trophicZone;
    }

    public String getInvasive()
    {
        return invasive;
    }

    public double getFlushingRate()
    {
        return flushingRate;
    }

    public double getLakeDepth()
    {
        return lakeDepth;
    }

    public double getLakeSize()
    {
        return lakeSize;
    }

    public double getTotalDrainageArea()
    {
        return totalDrainageArea;
    }
    
    public double getRiskScore()
    {
        return riskScore;
    }

    public double getConfidenceScore()
    {
        return confidenceScore;
    }

    public String getName()
    {
        return name;
    }
    public String getTown()
    {
        return town;
    }
    public String getCounty()
    {
        return county;
    }
    public int getLakeCode()
    {
        return lakeCode;
    }
    

    // --------------------------------------- Setters ---------------------------------------
    public void setScores(double [] scores)
    {
        setScore(scores[0]);
        setConfidenceScore(scores[1]);
    }
    
    public void setScore(double riskScore) {
        this.riskScore = riskScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }


    // --------------------------------------- toString ---------------------------------------

    public String toString() {
        return String.format(   //format the string so we don't print full doubles
            "%s | Risk: %.3f | Confidence: %.2f",
            name, riskScore, confidenceScore
        );
    }

    // -------------------- Risk Score Comparision --------------------
    @Override
    public int compareTo(Lake other)
    {
        return Double.compare(other.riskScore, this.riskScore);
    }
}