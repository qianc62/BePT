package qc;

import qc.common.Common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class QcMath {
    public static void getTwoArray( HashMap<String,Integer> goldDepthMap, HashMap<String,Integer> depthMap, ArrayList<Double> goldDepthArray, ArrayList<Double> depthArray ){
        for ( String key: goldDepthMap.keySet() ) {
            int goldValue = goldDepthMap.get( key );
            int value = depthMap.get(key)==null ? -1 : depthMap.get(key);
            goldDepthArray.add( goldValue * 1.0 );
            depthArray.add( value * 1.0 );
        }
    }

    public static double getExpectation( ArrayList<Double> list ){
        double sum = 0.0;
        for ( double item: list ) {
            sum += item;
        }
        return sum * 1.0 / list.size();
    }

    public static double getVariance( ArrayList<Double> list ){
        double E = QcMath.getExpectation( list );
        double sum = 0.0;
        for ( double item: list ) {
            sum += (item-E)*(item-E);
        }
        return sum * 1.0 / list.size();
    }

    public static double getCorrelationCoefficient( ArrayList<Double> list1, ArrayList<Double> list2 ){

        if ( list1.size() != list2.size() ) {
            return -Common.INF;
        }

        double e1 = getExpectation( list1 );
        double e2 = getExpectation( list2 );

        ArrayList<Double> list3 = new ArrayList<>();
        for ( int i=0; i<list1.size(); i++ ) {
            list3.add( (list1.get(i)-e1)*(list2.get(i)-e2) );
        }
        double e3 = getExpectation( list3 );

        double d1 = getVariance( list1 );
        double d2 = getVariance( list2 );

        if ( Math.sqrt(d1) * Math.sqrt(d2) == 0 ) {
            if ( e3 == 0 ) {
                return 1.0;
            }
            return 0.0;
        }

        return e3 / ( Math.sqrt(d1) * Math.sqrt(d2) );
    }

    public static double log( double a, double n ) {
        double ans = Math.log( n ) / Math.log( a );
        return ans;
    }

    public static double exp( double a, double n ){
        double E = 2.71828;
        double ans = Math.exp( n ) / Math.exp( n * log( E, E/a ) );
        return ans;
    }
}
