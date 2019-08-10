package com.iise.bpplus;

/**
 * Created by little on 15-11-20.
 */
public class TaskRelation {
    public static final Integer DIRECTCAUSAL = 0; //-->
    public static final Integer INVERSEDIRECTCAUSAL = 1;  //<--
    public static final Integer INDIRECTCAUSAL = 2;    //->>
    public static final Integer INVERSEINDIRECTCAUSAL = 3; //<<-
    public static final Integer ALWAYSCONCURRENCY = 4;    //||
    public static final Integer SOMETIMESCONCURRENCY = 5; //||-
    public static final Integer CONFLICT = 6; //#
    public static final String SLIENTTRANSITION = "inv_";
    public static final float[][] WEIGHTOFRELATIONS =
    {   //-->   <--    ->>    <<-    ||     ||-    #
        {1.00f, 0.00f, 0.75f, 0.00f, 0.50f, 0.49f, 0.00f},  //-->
        {0.00f, 1.00f, 0.00f, 0.75f, 0.50f, 0.49f, 0.00f},  //<--
        {0.75f, 0.00f, 1.00f, 0.00f, 0.25f, 0.24f, 0.00f},  //->>
        {0.00f, 0.75f, 0.00f, 1.00f, 0.25f, 0.24f, 0.00f},  //<<-
        {0.50f, 0.50f, 0.25f, 0.25f, 1.00f, 0.90f, 0.00f},  //||
        {0.49f, 0.49f, 0.24f, 0.24f, 0.90f, 1.00f, 0.00f},  //||-
        {0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 1.00f}   //#
    };
}
