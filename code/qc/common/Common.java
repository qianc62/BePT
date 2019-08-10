
package qc.common;

public class Common {
    public static final int INF = 999999;

    public static final String Place = "Place";
    public static final String Transition = "Transition";
    public static final String NullType = "NullType";

    public static final String StartNode  = "StartNode";
    public static final String EndNode    = "EndNode";
    public static final String ShadowNode = "ShadowNode";
    public static final String NormalNode = "NormalNode";

    public static final String Trivial = "Trivial";
    public static final String Polygon = "Polygon";
    public static final String BondPlace = "BondPlace";
    public static final String BondTransition = "BondTransition";
    public static final String RigidPlace = "RigidPlace";
    public static final String RigidTransition = "RigidTransition";
    public static final String RigidOtherBehavior = "RigidOtherBehavior";
    public static final String Loop = "Loop";

    public static void printLine(){
        System.out.println("------------------------------------------------------------");
    }

    public static void printLine( char ch ){
        for (int i = 0; i < 60; i++) {
            System.out.print( ch );
        }
        System.out.println();
    }

    public static void printLine( char ch, String str ){

        for (int i = 0; i < 30; i++) {
            System.out.print( ch );
        }
        System.out.print( "  " + str + "  " );
        for (int i = 0; i < 30; i++) {
            System.out.print( ch );
        }
        System.out.println();

        if ( ch == '<' ) {
            System.out.println();
        }
    }

    public static void printTab( int n ){
        for (int i = 0; i < n; i++) {
            System.out.print( "    " );
        }
    }

    public static void printEnter( int n ){
        for (int i = 0; i < n; i++) {
            System.out.println();
        }
    }

    public static String getTab( int n ){
        String str = "";
        for (int i = 0; i < n; i++) {
            str += "    ";
        }
        return str;
    }

    public static void printError( String str ){
        for (int i = 0; i < 10; i++) {
            System.out.println( "!!!!!!!!!!" + str + "!!!!!!!!!!" );
        }
    }

    public static String getFileName( String str ){
        if ( str==null || str.contains(".")==false ) {
            printError("public static String getFileName( String str )");
        }

        String[] array1 = str.split( "/" );
        String[] array2 = array1[array1.length-1].split( "\\." );

        return array2[0];
    }

    public static String getPrefix( String str ){
        String str_ = str.split("-")[0];
        return str_;
    }

    public static boolean isSamePrefix( String str1, String str2 ){
        String str1_ = getPrefix( str1 );
        String str2_ = getPrefix( str2 );
        if ( str1_.equals(str2_) ) {
            return true;
        }
        return false;
    }

    public static String getCleanName( String str ){
        str = str.replaceAll(" ","_");
        str = str.replaceAll("-","_");
        str = str.replaceAll("/.","_");
        return str;
    }
}
