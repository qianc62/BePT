package qc;

import com.mysql.jdbc.Buffer;
import qc.common.Common;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

//统一的文本生成树类QcTreeNode
public class QcTreeNode {
    public String type = "";
    public String label = "";
    public ArrayList<QcTreeNode> children = null;

    //常用关键词
    public String[] thens = {"then","subsequently","after that"};
    public String[] whens = {"when","once","after"};
    public String[] attentions = {"attention","notice that"};

    public QcTreeNode( String type_, String label_ ){
        this.type = type_;
        this.label = label_;
        this.children = new ArrayList<>();
    }

    //创建QcTreeNode树
    public static QcTreeNode createTree( ArrayList<QcSentence> sentences ){
        return dfsCreateTree( 0, sentences.size()-1, 0, sentences );
    }

    //赋随上一个方法
    private static QcTreeNode dfsCreateTree( int begin, int end, int depth, ArrayList<QcSentence> sentences ){
        QcTreeNode parent;

        QcSentence sentence_begin = sentences.get( begin );
        QcSentence sentence_end   = sentences.get( end );

        if ( sentenceMatch( sentence_begin.type, sentence_end.type ) == false ) {
            return null;
        }

        String[] strs = sentence_begin.type.split("_");
        if ( strs[0].equals(Common.Trivial) ) {
            QcSentence sentence_mid = sentences.get( begin+1 );
            parent = new QcTreeNode( strs[0], sentence_mid.string );
        } else {
            parent = new QcTreeNode( strs[0], "" );

            int index1 = begin + 1;
            while( index1 < end ) {
                int index2 = findEndIndex( index1, sentences );
                QcTreeNode child = dfsCreateTree( index1, index2, depth+1, sentences );

                if ( parent.type.contains("Rigid") ) {
                    if ( parent.children.size()!=0 ) {
                        child.type = Common.RigidOtherBehavior;
                    }
                }

                parent.children.add( child );
                index1 = index2 + 1;
            }
        }

        return parent;
    }

    private static int findEndIndex( int index1, ArrayList<QcSentence> sentences ){
        QcSentence sentence_begin = sentences.get( index1 );

        int index2 = index1;
        while( index2 < sentences.size() ) {
            QcSentence sentence_end = sentences.get( index2 );
            if ( sentence_begin.depth == sentence_end.depth && sentenceMatch( sentence_begin.type, sentence_end.type ) == true ) {
                return index2;
            }
            index2++;
        }

        return -1;
    }

    private static boolean sentenceMatch( String str1, String str2 ){
        String[] str1s = str1.split("_");
        String[] str2s = str2.split("_");
        int len1 = str1s.length;
        int len2 = str2s.length;

        if ( len1 != len2 ) {
            return false;
        }

        if ( str1s[len1-1].equals("Template") && str2s[len2-1].equals("Template") && str1s[len1-2].equals("Begin") && str2s[len2-2].equals("End") && str1s[len1-3].equals(str2s[len2-3]) ) {
            return true;
        }

        return false;
    }

    //输出QcTreeNode树
    public void print( int depth ){
        System.out.println( Common.getTab(depth) + this.type + ", " + this.label );
        for (int i = 0; i < this.children.size(); i++) {
            children.get(i).print( depth+1 );
        }
    }

    public void dfsWrite( int depth, BufferedWriter writer ) throws Exception {
        writer.write( Common.getTab(depth) + this.type + ", " + this.label + "\n" );
        for (int i = 0; i < this.children.size(); i++) {
            children.get(i).dfsWrite( depth+1, writer );
        }
    }

    //当前节点的所有子孙节点生成对应文本
    /*
    * 遍历给类结构,对每类结构分类处理
    * */
    public String toText( int depth, String beginSentence, String endSentence, HashMap<String, String> templateMap, HashMap<String,Integer> depthMap ) {
        String text = "";

        if (this.type.equals(Common.Trivial)) {
            String str = templateMap.get( "Trivial_Sentence_Template" );
            str = str.replace( "role_slot", "manager" );
            str = str.replace( "trivial_slot", this.label.split("-")[0] );
            text = createSentence( depth, beginSentence, str, endSentence );
            depthMap.put( Common.getPrefix(this.label), depth );
            return text;
        }

        else if ( this.type.equals(Common.Polygon) ) {
            if ( trivialsNum()==children.size() ) {
                for (int i = 0; i < this.children.size(); i++) {
                    String beginSen = i==0 ? "" : random(thens)+", ";
                    text += children.get(i).toText( depth, beginSen, ". ", templateMap, depthMap );
                }
            }
            else {
                text += createSentence( depth, beginSentence, templateMap.get("Polygon_Begin_Template"), ": \n" );
                for (int i = 0; i < this.children.size(); i++) {
                    String beginSen = (i==0||!children.get(i-1).type.equals(Common.Trivial)) ? "" : random(thens)+", ";
                    String endSen = children.get(i).type.equals(Common.Trivial) ? ". " : "";
                    text += children.get(i).toText( depth+1, beginSen, endSen, templateMap, depthMap );
                }
                text += createSentence( depth+1, random(thens)+", ", templateMap.get("Polygon_End_Template"), ". " );
            }
        }

        else if (this.type.equals(Common.BondPlace)) {
            text += createSentence( depth, beginSentence, templateMap.get("BondPlace_Begin_Template"), ": \n" );
            for (int i = 0; i < this.children.size(); i++) {
                if ( children.get(i).type.equals(Common.Loop)==false ) {
                    text += children.get(i).toText( depth+1, "", ". \n", templateMap, depthMap );
                } else {
                    text += children.get(i).toText( depth+1, random(attentions)+", ", ". \n", templateMap, depthMap );
                }
            }
            text += createSentence( depth+1, random(whens)+" ", templateMap.get("BondPlace_End_Template"), ", " );
        }

        else if (this.type.equals(Common.BondTransition)) {
            for (int i = 0; i < this.children.size(); i++) {
                if (i == 0) {
                    text += children.get(i).toText( depth+1, "", ". ", templateMap, depthMap );
                    text += createSentence( depth, "", templateMap.get("BondTransition_Begin_Template"), ": \n" );
                } else if (i < this.children.size() - 1) {
                    text += children.get(i).toText( depth+1, "", ". \n", templateMap, depthMap );
                }
                if (i == this.children.size() - 1) {
                    text += createSentence( depth, random(whens)+" ", templateMap.get("BondTransition_End_Template"), ", " );
                    text += children.get(i).toText( depth+1, "", ". ", templateMap, depthMap );
                }
            }
        }

        else if (this.type.equals(Common.Loop)) {
            text += createSentence( depth, beginSentence, templateMap.get("Loop_Begin_Template"), ": \n" );
            for (int i = 0; i < this.children.size(); i++) {
                text += children.get(i).toText( depth+1, "", ". ", templateMap, depthMap );
            }
            text += createSentence( depth, random(thens)+", ", templateMap.get("Loop_End_Template"), ". " );
        }

        text = text + endSentence;


        return text;
    }

    //创建基础句(不可再分)
    private String createSentence( int depth, String beginStr, String str, String endStr ){
        String str1 = "";
        if ( depth!=-1 ) {
            str1 = "[" + depth + "]";
        }
        return str1 + beginStr + str + endStr;
    }

    //随机选取关键词
    private String random( String[] words ){
        Random rand = new Random();
        int n = rand.nextInt( words.length );
        return words[n];
    }

    //得到数据结构以计算信息增益
    public ArrayList<ArrayList<String>> getTrivialLabels(){
        ArrayList<ArrayList<String>> trivialLabels = new ArrayList<>();
        dfsGetTrivialLabels( trivialLabels );
        return trivialLabels;
    }

    //赋随上一个方法
    private void dfsGetTrivialLabels( ArrayList<ArrayList<String>> trivialLabels ){
        if ( this.type == Common.Trivial ) {
            return ;
        }

        for (int i = 0; i < this.children.size(); i++) {
            if ( children.get(i).type.equals(Common.Trivial) == false ) {
                children.get(i).dfsGetTrivialLabels( trivialLabels );
            } else {
                ArrayList<String> trivialLabels_ = new ArrayList<>();
                for (int j = i; j < this.children.size() && this.children.get(j).type.equals(Common.Trivial); j++) {
                    trivialLabels_.add( this.children.get(j).label.split("-")[0] );
                    i = j;
                }
                trivialLabels.add( trivialLabels_ );
            }
        }
    }

    //是否为字母
    public static boolean isLetter( char ch ){
        if ( (ch>='a' && ch<='z') || (ch>='A'&&ch<='Z') ) {
            return true;
        }
        return false;
    }

    //下一个字母
    public static int nextLetter( int i, char[] chars ){
        for (int j = i; j < chars.length; j++) {
            if ( isLetter(chars[j]) ) {
                return j;
            }
        }
        return -1;
    }

    //文本后处理
    public static String postProcessText( QcPetriNet petri, String text ) {
//        System.out.println( text );

        //格式控制
        text = text.replace( "mid_slot", "" );
        while( text.indexOf("  ")!=-1 ){
            text = text.replace( "  ", " " );
        }
        while( text.indexOf(". .")!=-1 ){
            text = text.replace( ". .", "." );
        }
        text = text.replace( " .", "." );
        text = text.replace( ". ,", ", " );
        text = text.replace( ", .", "," );
//        text = text.replaceFirst( "\n", " " );
//        text = text.replace( "\n\n", "\n" );

        //缩进控制
        int maxDepth = 10;
        for ( int i=0; i<maxDepth; i++ ) {
            text = text.replace( String.format(" [%d]",i), " " );
        }
        for ( int i=0; i<maxDepth; i++ ) {
            text = text.replace( String.format("[%d]",i), Common.getTab(i) );
        }

        //首字母大写控制
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if ( i ==0 || chars[i]=='.' || chars[i]==':' ) {
                int nextIndex = nextLetter( i , chars );
                if ( nextIndex != -1 ) {
                    chars[nextIndex] = (char)(chars[nextIndex]-32);
                }
            }
        }

        text = new String( chars );

        for ( QcNode node_: petri.getTransitions() ) {
            text = text.replaceAll( node_.id, node_.name );
        }

        return text;
    }

    //清理句子(前后空格)
    public static String cleanSentence( String string ){
        int index1 = -1;
        for (int i = 0; i < string.length(); i++) {
            if ( string.charAt(i)!=' ' ) {
                index1 = i;
                break;
            }
        }

        int index2 = -1;
        for (int i = string.length()-1; i >=0 ; i--) {
            if ( string.charAt(i)!=' ' ) {
                index2 = i;
                break;
            }
        }

        return string.substring( index1, index2+1 );
    }

    //计算孩子结点中T结构的数量
    public int trivialsNum(){
        int sum = 0;

        for (int i = 0; i < this.children.size(); i++) {
            if (children.get(i).type.equals(Common.Trivial) == true) {
                sum++;
            }
        }

        return sum;
    }
}
