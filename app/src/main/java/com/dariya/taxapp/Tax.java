package com.dariya.taxapp;

public class Tax {
    private double income;
    private double RRSP;
    private static final double limit_percentage=0.18;
    private static final double max_rrsp=27230; //for 2024 limit according to the website it is 31560
    //however, based on instructions it should be 27230 as in 2020 years

    private static final double [] [] Taxes={
            {0,51446,0.2005},
            {51446,55867,0.2415},
            {55867,90599,0.2965},
            {90599,102984,0.3148},
            {102984,106733,0.3389},
            {106733,111733,0.3791},
            {111733,150000,0.4341},
            {150000,173205,0.4497},
            {173205,220000,0.4829},
            {220000,246752,0.4985},
            {246752, Double.MAX_VALUE,0.5353}
    };

    public Tax(double income, double RRSP){
        this.income=income;
        this.RRSP=Math.min(RRSP,max_rrsp);
    }
    public double TaxCalculator() {
        double TaxableIncome = Math.max(0, income - RRSP);
        System.out.println("Taxable Income: " + TaxableIncome);

        double tax = 0;

        for (double[] bracket : Taxes) {
            if (TaxableIncome > bracket[0]) {
                double amount = Math.min(TaxableIncome, bracket[1]) - bracket[0];
                tax += amount * bracket[2];
                System.out.println("Bracket: " + bracket[0] + " - " + bracket[1]);
                System.out.println("Taxable Amount: " + amount);
                System.out.println("Tax for this bracket: " + (amount * bracket[2]));
            } else {
                break;
            }
        }

        System.out.println("Total Tax: " + tax);
        return tax;
    }
        public double NextLimit(){
        return Math.min(max_rrsp,income*limit_percentage);
        }

    }

