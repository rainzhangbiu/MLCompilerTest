package structure;

public class Token {
    /* token的类型*/
    private String kind;
    /* token所在行*/
    private int line;
    /* token所在列*/
    private int column;
    /* token内容*/
    private String content;


    public Token(int line,int column,String kind, String content) {
        this.line = line;
        this.column=column;
        this.kind=kind;
        this.content=content;
    }

    public String getKind(){
        return kind;
    }
    public void setKind(String kind){
        this.kind=kind;
    }

    public int getLine(){
        return line;
    }
    public void setLine(int line){
        this.line=line;
    }

    public int getColumn(){
        return column;
    }
    public void setColumn(int column){
        this.column=column;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content){
        this.content=content;
    }


}
