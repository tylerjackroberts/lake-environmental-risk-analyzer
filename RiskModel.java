//* 
// RiskModel
//
// Author: Tyler Roberts
// First Created: 2/23/2026
// Last Edited: 3/11/2026
//
// This class calculates environmental risk and confidence scores for Lake objects
//based on multiple ecological and hydrological indicators.

// --------- MODEL STRUCTURE ---------
// Each Risk Factor has boolean and an array holding 4 double variables:
//      Boolean - whether the Lake Field was null or non-null
//      Array -
//          1) Double 0 to 1 - Risk Score 0 to 1 (varies)
//          2) Double 0 to 1 - Weight to risk score if non-null (varies)
//          3) Double 0 to 1 - Contribution to confidence when data is present
//          4) Double 0 to 1 - Contribution to confideence when data is missing
// 
// The Risk Factors Included in the Model are:
//      ) Trophic Zone
//      ) Water Quality Statement
//      ) Invasive Plant Species
//      ) Drainge Ratio (Requires both Drainage Area and Lake Area)
//      ) Flushing Rate
//      ) Lake Depth
//      ) Lake Size 

// --------- WORKFLOW ---------
// 1. runCalculations(Lake lake)
//     Main entry point for the model.
//     Calls update methods for each risk factor to compute normalized scores.
//     Then calculates final risk and confidence values.
//
// 2. updateFactor methods
//     Each update method:
//         - checks if the required data is present
//         - computes a normalized risk score
//         - updates the corresponding factor array.
//
// 3. calculateRisk()
//     Combines all factor risk scores using their associated weights.
//
// 4. calculateConfidence()
//     Combines confidence contributions depending on whether each
//     factor was present or missing.

// --------- DATA ASSUMPTIONS ---------
// Lake objects must convert missing values before being passed
// to this model:
//   Missing numeric values → -1
//   Missing string values  → ""
// This allows the model to detect missing inputs reliably.

//  --------- NORMALIZATION ---------
// Two helper functions are used:
// normalizeLinear(value, max)
//   -Linearly scales values into the range [0,1].
// normalizeLog(value, max)
//   -Applies logarithmic scaling to handle highly skewed variables
//     such as watershed size or lake area.

// --------- OUTPUT ---------
// runCalculations() returns a double array:
//     result[0] → final risk score
//     result[1] → confidence score

public class RiskModel {

    //---------- Create fields for risk factors ----------
    //EXAMPLE Array below 
    //{riskScore,riskWeight_if_present, confidence_if_present, confidence_if_null}
    
    //Store Indexs For Later Use
    private static final int RISK = 0;
    private static final int RISK_WEIGHT = 1;
    private static final int CONFIDENCE_PRESENT = 2;
    private static final int CONFIDENCE_NULL = 3;

    //Water Quality Statement
    private boolean hasWaterQualityStatement = false;
    private double [] waterQualityStatement = {0.0,1.0,1.0,1.0};

    //Trophic Zone
    private boolean hasTrophicZone = false;
    private double [] trophicZone = {0.0,0.8,0.8,0.8};

    // Invasive Species
    private boolean hasInvasive = false;
    private double [] invasive = {0.0,1.0,1.0,0.4};

    // Drainage Ratio
    private boolean hasDrainageRatio = false;
    private double[] drainageRatio = {0.0,0.5,0.5,0.5};

    // Flushing Rate
    private boolean hasFlushingRate = false;
    private double[] flushingRate = {0.0,0.5,0.5,0.5};

    // Lake Depth
    private boolean hasLakeDepth = false;
    private double[] lakeDepth = {0.0,0.1,0.2,0.2};

    // Lake Size
    private boolean hasLakeSize = false;
    private double[] lakeSize = {0.0,0.1,0.2,0.2};


    // ---------- Functions ----------
    /**
     * Executes the full risk model for a given Lake.
     *
     * Workflow:
     * 1. Reset internal factor flags.
     * 2. Update all risk factors using lake data.
     * 3. Compute weighted environmental risk score.
     * 4. Compute confidence score based on data availability.
     *
     * @param lake Lake object containing environmental attributes
     * @return double[] where:
     *         result[0] = risk score
     *         result[1] = confidence score
     */
    public double [] runCalculations(Lake lake)
    {
        //reset from last run
        resetFactors();
        //---------- update local fields with formulas ----------
        updateWaterQualityStatement(lake);
        updateTrophicZone(lake);
        updateInvasive(lake);
        updateDrainageRatio(lake);
        updateFlushingRate(lake);
        updateLakeDepth(lake);
        updateLakeSize(lake);
        
        //calls to helpers
        double [] result = new double [2];
        result[0]=calculateRisk(lake);
        result[1]=calculateConfidence(lake);
        return result;       
    }

    /**
     * Computes the final environmental risk score using
     * a weighted average of all available risk factors.
     *
     * Only factors with available data contribute to the score.
     *
     * @return normalized risk score between 0 and 1
     */
    private  double calculateRisk(Lake lake) {
        double weightedSum = 0.0;
        double totalWeight = 0.0;
        //Water Quality Statement
        if (hasWaterQualityStatement) {
            weightedSum += waterQualityStatement[RISK] * waterQualityStatement[RISK_WEIGHT];
            totalWeight += waterQualityStatement[RISK_WEIGHT];
        }
        //Trophic Zone
        if (hasTrophicZone) {
            weightedSum += trophicZone[RISK] * trophicZone[RISK_WEIGHT];
            totalWeight += trophicZone[RISK_WEIGHT];
        }
        // Invasive Species
        if (hasInvasive) {
            weightedSum += invasive[RISK] * invasive[RISK_WEIGHT];
            totalWeight += invasive[RISK_WEIGHT];
        }
        // Drainage Ratio
        if (hasDrainageRatio) {
            weightedSum += drainageRatio[RISK] * drainageRatio[RISK_WEIGHT];
            totalWeight += drainageRatio[RISK_WEIGHT];
        }
        // Flushing Rate
        if (hasFlushingRate) {
            weightedSum += flushingRate[RISK] * flushingRate[RISK_WEIGHT];
            totalWeight += flushingRate[RISK_WEIGHT];
        }
        // Lake Depth
        if (hasLakeDepth) {
            weightedSum += lakeDepth[RISK] * lakeDepth[RISK_WEIGHT];
            totalWeight += lakeDepth[RISK_WEIGHT];
        }
        // Lake Size    
        if (hasLakeSize) {
            weightedSum += lakeSize[RISK] * lakeSize[RISK_WEIGHT];
            totalWeight += lakeSize[RISK_WEIGHT];
        }
        if (totalWeight == 0) {
            return 0;   // no usable data
        }
        return weightedSum / totalWeight;
    }

    /**
     * Computes the confidence score for the risk model.
     *
     * Confidence is based on which variables were present
     * versus missing for the lake.
     *
     * Missing variables contribute a reduced confidence weight.
     *
     * @return confidence score between 0 and 1
     */
    private  double calculateConfidence(Lake lake) {
        double calcConf = 0.0; //calculated confidence
        double potConf = 0.0;  //potential confidence
        //Water Quality Statement
        if (hasWaterQualityStatement){
            calcConf += waterQualityStatement[CONFIDENCE_PRESENT];
            potConf += waterQualityStatement[CONFIDENCE_PRESENT];
        }
        else {
            potConf += waterQualityStatement[CONFIDENCE_NULL];
        }
        //Trophic Zone
        if (hasTrophicZone){
            calcConf += trophicZone[CONFIDENCE_PRESENT];
            potConf += trophicZone[CONFIDENCE_PRESENT];
        }
        else {
            potConf += trophicZone[CONFIDENCE_NULL];
        }
        // Invasive Species
        if (hasInvasive){
            calcConf += invasive[CONFIDENCE_PRESENT];
            potConf += invasive[CONFIDENCE_PRESENT];
        }
        else {
            potConf += invasive[CONFIDENCE_NULL];
        }
        // Drainage Ratio
        if (hasDrainageRatio){
            calcConf += drainageRatio[CONFIDENCE_PRESENT];
            potConf += drainageRatio[CONFIDENCE_PRESENT];
        }
        else {
            potConf += drainageRatio[CONFIDENCE_NULL];
        }
        // Flushing Rate
        if (hasFlushingRate){
            calcConf += flushingRate[CONFIDENCE_PRESENT];
            potConf += flushingRate[CONFIDENCE_PRESENT];
        }
        else {
            potConf += flushingRate[CONFIDENCE_NULL];
        }
        // Lake Depth
        if (hasLakeDepth){
            calcConf += lakeDepth[CONFIDENCE_PRESENT];
            potConf += lakeDepth[CONFIDENCE_PRESENT];
        }
        else {
            potConf += lakeDepth[CONFIDENCE_NULL];
        }
        // Lake Size
        if (hasLakeSize){
            calcConf += lakeSize[CONFIDENCE_PRESENT];
            potConf += lakeSize[CONFIDENCE_PRESENT];
        }
        else {
            potConf += lakeSize[CONFIDENCE_NULL];
        }
        // CALC AND RETURN CONFIDENCE
        if (potConf == 0) return 0; //safeguard - should never happen though
        return (calcConf/potConf);
    }

    private void updateWaterQualityStatement(Lake lake)
    {
        String str = lake.getWaterQualityStatement();

        if (str.equals(""))
        {
            hasWaterQualityStatement = false;
            return;
        }

        hasWaterQualityStatement = true;

        if (str.equalsIgnoreCase("Above average"))
            waterQualityStatement[RISK] = 0;

        else if (str.equalsIgnoreCase("Average"))
            waterQualityStatement[RISK] = 0.5;

        else if (str.equalsIgnoreCase("Below average"))
            waterQualityStatement[RISK] = 1.0;
    }

    private void updateTrophicZone(Lake lake)
    {
        String tempStr = lake.getTrophicZone();
        if (tempStr.equals(""))
        {
            hasTrophicZone = false; 
        }
        else if (tempStr.equalsIgnoreCase("OLIGO"))
        {
            hasTrophicZone = true;
            trophicZone[RISK]=0.1;
        }
        else if (tempStr.equalsIgnoreCase("MESO"))
        {
            hasTrophicZone = true;
            trophicZone[RISK]=0.5;
        }
        else if (tempStr.equalsIgnoreCase("EUTRO"))
        {
            hasTrophicZone = true;
            trophicZone[RISK]=0.9;
        }
        else if (tempStr.equalsIgnoreCase("DYST"))
        {
            hasTrophicZone = true;
            trophicZone[RISK]=1.0;
        }
    }

    private void updateInvasive(Lake lake)
    {
        String str = lake.getInvasive();

        if (str.equals(""))
        {
            hasInvasive = false;
            return;
        }

        hasInvasive = true;
        invasive[RISK] = 1.0;
    }
  
    private void updateDrainageRatio(Lake lake)
    {
        double drainage = lake.getTotalDrainageArea();
        double area = lake.getLakeSize();

        if (drainage == -1 || area == -1)
        {
            hasDrainageRatio = false;
            return;
        }
        hasDrainageRatio = true;
        double ratio = drainage / area;
        double Rmax = 722;   //found by creating an extra pandas column with numpy
        double score = normalizeLog(ratio, Rmax);
        drainageRatio[RISK] = score;
    }

    private void updateFlushingRate(Lake lake)
    {
        double F = lake.getFlushingRate();
        if (F == -1)
        {
            hasFlushingRate = false;
            return;
        }

        hasFlushingRate = true;

        double Fmax = 95.43; //found using pandas in python with dataset

        double score = normalizeLog(F, Fmax); 

        flushingRate[RISK] = 1 - score; // we want high flushing = lower risk
    }

    private void updateLakeDepth(Lake lake)
    {
        double D = lake.getLakeDepth();
        if (D == -1)
        {
            hasLakeDepth = false;
            return;
        }
        hasLakeDepth = true;

        double Dmax = 107.0; //found using pandas in python

        double score = normalizeLinear(D, Dmax);

        lakeDepth[RISK] = 1 - score;
    }

    private void updateLakeSize(Lake lake)
    {
        double A = lake.getLakeSize();
        if (A == -1)
        {
            hasLakeSize = false;
            return;
        }
        hasLakeSize = true;

        double Amax = 75471.0; //found using pandas in python

        double score = normalizeLog(A, Amax);

        lakeSize[RISK] = 1 - score;
    }
  
    //Math Helper Functions
    private double normalizeLinear(double value, double max)
    {
        double score = value / max;

        if (score < 0) score = 0;
        if (score > 1) score = 1;

        return score;
    }

    private double normalizeLog(double value, double max)
    {
        double score = Math.log(1 + value) / Math.log(1 + max);

        if (score < 0) score = 0;
        if (score > 1) score = 1;

        return score;
    }

    //Reset after each run - Safeguard
    private void resetFactors()
    {
        hasWaterQualityStatement = false;
        hasTrophicZone = false;
        hasInvasive = false;
        hasDrainageRatio = false;
        hasFlushingRate = false;
        hasLakeDepth = false;
        hasLakeSize = false;
    }
}