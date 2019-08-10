package qc;

public class QcSentence {
    int depth;
    String type   = "";
    String string = "";

    public QcSentence( int depth_, String type_, String string_ ) {
        this.depth = depth_;
        this.type = type_;
        this.string = string_;
    }

    @Override
    public String toString() {
        return String.format( "%s", string);
    }
}
