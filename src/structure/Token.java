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
    /*标识符类型*/
    private String idKind;

    public Token(int line,int column,String idKind,String kind) {
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

    public String getIdKind(){
        return idKind;
    }
    public void setIdKind(String idKind){
        this.idKind=idKind;
    }
}
