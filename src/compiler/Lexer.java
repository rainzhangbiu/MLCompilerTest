package compiler;

import jdk.jshell.execution.JdiInitiator;
import structure.ConstVar;
import structure.Token;
import structure.TreeNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Standard ML词法分析类
 *
 * @author 章雨
 *
 */
public class Lexer {
    //注释的标志
    private boolean isNotation=false;
    //分析后得到的Tokens集合，用于之后的语法以及语义分析
    private ArrayList<Token> tokens=new ArrayList<Token>();
    //分析后得到的所有Token集，包含注释，空格等
    private ArrayList<Token> displayTokens=new ArrayList<Token>();
    //读取ML文件文本
    private BufferedReader reader;

    public boolean isNotation(){
        return isNotation;
    }

    public void setNotation(boolean isNotation){
        this.isNotation=isNotation;
    }

    public ArrayList<Token> getTokens(){
        return tokens;
    }

    private void setTokens(ArrayList<Token> tokens){
        this.tokens=tokens;
    }

    public ArrayList<Token> getDisplayTokens(){
        return displayTokens;
    }

    private void setDisplayTokens(ArrayList<Token> displayToken){
        this.displayTokens = displayTokens;
    }

    /**
     * 识别字母的方法
     *
     * @param c 当前字符
     *
     * @return boolean
     */
    private static boolean isLetter(char c){
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    /**
     * 识别数字
     *
     * @param c 当前字符
     *
     * @return boolean
     */
    private static boolean isDigit(char c){
        return c >= '0' && c <= '9';
    }

    /**
     * 识别正确的整数，排除多个零的情况
     *
     * @param input 要判断的字符串
     *
     * @return boolean
     */
    private static boolean matchInteger(String input){
        return input.matches("^-?\\d+$") && !input.matches("^-?0{1,}\\d+$");
    }

    /**
     * 识别正确的实数：排除00.000的情况
     *
     * @param input 要判断的字符串
     *
     * @return boolean
     */
    private static boolean matchReal(String input){
        return input.matches("^(-?\\\\d+)(\\\\.\\\\d+)+$");
    }

    /**
     *
     * 识别正确的标识符：以字母开头，由数字、字母、下划线、单引号组成
     *
     * @param input 要判断的字符
     *
     * @return boolean
     */
    private static boolean matchID(String input){
        return input.matches("^\\w+$") && !input.endsWith("_") && input.substring(0, 1).matches("[A-Za-z]");
    }

    /**
     * 识别保留字
     *
     * @param input 要输入的字符串
     *
     * @return boolean
     */
    private static boolean isKey(String input){
        return input.equals("if") || input.equals("else") || input.equals("then")
                || input.equals("val") || input.equals("true") || input.equals("false")
                || input.equals("string") || input.equals("int") || input.equals("bool")
                || input.equals("char") || input.equals("and") || input.equals("real")
                || input.equals("let") || input.equals("local") || input.equals("in")
                || input.equals("end") ;
    }

    /**
     * 识别字母运算符
     *
     * @param input 要判断的字符串
     *
     * @return boolean
     */
    private static boolean isOperator(String input){
        return input.equals("div") || input.equals("mod");
    }

    //暂时还清楚该方法的作用
    private static int find(int begin,String str){
        if(begin>=str.length())
            return str.length();
        for(int i=begin;i<str.length();i++){
            char c=str.charAt(i);
            if(c == '\n' || c == ',' || c == ' ' || c == '\t' || c == '{'
                    || c == '}' || c == '(' || c == ')' || c == ';' || c == '='
                    || c == '+' || c == '-' || c == '*' || c == '/' || c == '['
                    || c == ']' || c == '<' || c == '>')
                return i-1;
        }
        return str.length();
    }

    /**
     * 分析一行ML程序，并返回一行得到的TreeNode
     *
     * @param mlText 一行ml程序
     *
     * @lineNum 行号
     *
     * @return 一个树节点
     */
    private TreeNode executeLine(String mlText, int lineNum){
        //创建当前行的根节点
        String content = "第" + lineNum + "行" + mlText;
        TreeNode node = new TreeNode(content);
        //添加词法分析每行结束的标志
        mlText += "\n";
        //每一行的长度
        int length = mlText.length();
        //switch的状态值，用来判断
        int state = 0;
        //记录token开始的位置
        int begin = 0;
        //记录token结束的文职
        int end = 0;
        //逐个读取当前行的字符，进行分析（暂时忽略注释）
        for(int i = 0; i < length; i++){
            // ch保存着当前字符
            char ch = mlText.charAt(i);
            if(ch == '(' || ch == ')' || ch == ';' || ch == '{'
                    || ch == '}' || ch == ',' || ch == '+' || ch == '~'
                    || ch == '-' || ch == '*' || ch == '/'
                    || ch == '=' || ch == '<' || ch == '>' || ch == '"'
                    || isLetter(ch) || isDigit(ch) || ch == '.'
                    || String.valueOf(ch).equals(" ")
                    || String.valueOf(ch).equals("\n")
                    || String.valueOf(ch).equals("\r")
                    || String.valueOf(ch).equals("\t")){
                switch(state){
                    //循环程序开始时进入case 0
                    case 0:
                        //分隔符直接打印
                        if(ch == '(' || ch == ')' || ch == '{' || ch == '}' || ch == ';' || ch == ',' || ch == ':')
                            state = 0 ;
                        else if (ch == '+')
                            state = 1;
                        else if(ch == '-')
                            state = 2;
                        else if(ch == '*')
                            state = 3;
                        else if(ch == '/')
                            state = 4;
                        else if(ch == '=')
                            state = 5;
                        else if(ch == '<')
                            state = 6;
                        else if(ch == '>')
                            state = 7;
                        else if(isDigit(ch)){
                            state = 8;
                            begin = i;
                        }
                        else if(isLetter(ch)){
                            state = 9;
                            begin = i;
                        }
                        //双引号
                        else if(String.valueOf(ch).equals(ConstVar.DQ)){
                            begin = i + 1;
                            state = 10;
                            tokens.add(new Token(lineNum,begin,"分隔符",ConstVar.DQ));
                            displayTokens.add(new Token(lineNum,begin,"分隔符",ConstVar.DQ));
                        }
                        //取反
                        else if(ch == '~'){
                            state = 11;
                        }
                        //空白符
                        else if(String.valueOf(ch).equals(" ")){
                            state = 0;
                            displayTokens.add(new Token(lineNum, i + 1,"空白符"," "));
                        }
                        //换行符
                        else if(String.valueOf(ch).equals("\n")){
                            state = 0;
                            displayTokens.add(new Token(lineNum, i + 1, "换行符","\n"));
                        }
                        //回车符
                        else if(String.valueOf(ch).equals("\r")){
                            state = 0;
                            displayTokens.add(new Token(lineNum, i + 1, "回车符","\r"));
                        }
                        //制表符
                        else if(String.valueOf(ch).equals("\t")){
                            state = 0;
                            displayTokens.add(new Token(lineNum, i + 1, "换行符","\t"));
                        }
                        break;

                    case 1:
                        tokens.add(new Token(lineNum, i, "运算符", ConstVar.PLUS));
                        displayTokens.add(new Token(lineNum,i,"运算符", ConstVar.PLUS));
                        //如果没有，则会跳过一个字符
                        i--;
                        state = 0;
                        break;

                    case 2:
                        tokens.add(new Token(lineNum, i, "运算符： ", ConstVar.MINUS));
                        displayTokens.add(new Token(lineNum, i, "运算符： ", ConstVar.MINUS));
                        i--;
                        state = 0;
                        break;

                    case 3:
                        tokens.add(new Token(lineNum, i, "运算符", ConstVar.TIMES));
                        displayTokens.add(new Token(lineNum, i, "运算符", ConstVar.TIMES));
                        i--;
                        state = 0;
                        break;

                    case 4:
                        tokens.add(new Token(lineNum, i, "运算符", ConstVar.DIVIDE));
                        displayTokens.add(new Token(lineNum, i, "运算符", ConstVar.DIVIDE));
                        i--;
                        state = 0;
                        break;
                    case 5:
                        if(ch == '='){
                            tokens.add(new Token(lineNum, i, "运算符", ConstVar.EQUAL));
                            displayTokens.add(new Token(lineNum, i, "运算符", ConstVar.EQUAL));
                            state = 0 ;
                        }else{
                            tokens.add(new Token(lineNum, i, "运算符", ConstVar.ASSIGN));
                            displayTokens.add(new Token(lineNum, i, "运算符", ConstVar.ASSIGN));
                            i--;
                            state = 0;
                        }
                        break;
                    case 6:
                        if (ch == '>'){
                            tokens.add(new Token(lineNum, i, "运算符",
                                    ConstVar.NEQUAL));
                            displayTokens.add(new Token(lineNum, i, "运算符",
                                    ConstVar.NEQUAL));
                        }else {
                            tokens.add(new Token(lineNum, i, "运算符", ConstVar.LT));
                            displayTokens.add(new Token(lineNum, i, "运算符", ConstVar.LT));
                            i--;
                        }
                       state = 0;
                       break;
                    case 7:
                        tokens.add(new Token(lineNum, i, "运算符", ConstVar.GT));
                        displayTokens.add(new Token(lineNum, i, "运算符", ConstVar.GT));
                        i--;
                        state = 0;
                        break;
                    case 8:
                        if(isDigit(ch) || String.valueOf(ch).equals(".")){
                            state = 8;
                        }else{
                            end = i;
                            String id = mlText.substring(begin, end);
                            if (matchInteger(id)) {
                                tokens.add(new Token(lineNum, begin + 1, "整数", id));
                                displayTokens.add(new Token(lineNum, begin + 1, "整数", id));
                            }else if (matchReal(id)){
                                tokens.add(new Token(lineNum, begin + 1, "实数", id));
                                displayTokens.add(new Token(lineNum, begin + 1, "实数", id));
                            }
                            i = find(i, mlText);
                            state = 0;
                        }
                        break;
                    case 9:
                        if (isLetter(ch) || isDigit(ch)) {
                            state = 9;
                        }else{
                            end = i;
                            String id = mlText.substring(begin, end);
                            if (isKey(id)) {
                                tokens.add(new Token(lineNum, begin + 1, "关键字", id));
                                displayTokens.add(new Token(lineNum, begin + 1, "关键字", id));
                            }else if (matchID(id)) {
                                tokens.add(new Token(lineNum, begin + 1, "标识符", id));
                                displayTokens.add(new Token(lineNum, begin + 1, "标识符", id));
                            }else if(isOperator(id)){
                                if(id.equals("div")) {
                                    tokens.add(new Token(lineNum, begin + 1, "运算符", ConstVar.DIV));
                                    displayTokens.add(new Token(lineNum, begin + 1, "运算符", ConstVar.DIV));
                                }else if (id.equals("mod")){
                                    tokens.add(new Token(lineNum, begin + 1, "运算符", ConstVar.MOD));
                                    displayTokens.add(new Token(lineNum, begin + 1, "运算符", ConstVar.MOD));
                                }
                            }
                            i--;
                            state = 0;
                        }
                        break;
                    case 10:
                        if (ch == '"') {
                            end = i;
                            String string = mlText.substring(begin, end);
                            tokens.add(new Token(lineNum, begin + 1, "字符串",
                                    string));
                            displayTokens.add(new Token(lineNum, begin + 1,
                                    "字符串", string));
                            tokens.add(new Token(lineNum, end + 1, "分隔符",
                                    ConstVar.DQ));
                            displayTokens.add(new Token(lineNum, end + 1,
                                    "分隔符", ConstVar.DQ));
                            state = 0;
                        }
                    case 11:
                       begin = i-1;
                       i--;
                       state = 8;
                       break;
                }
            }
        }
        return node;
    }

    /**
     * 按行读取ML程序，并执行executeLine方法
     *
     * @param mlText ML文本
     * @return 词法分析结果跟节点
     */
    public TreeNode execute(String mlText) {
        setTokens(new ArrayList<Token>());
        setDisplayTokens(new ArrayList<Token>());
        StringReader stringReader = new StringReader(mlText);
        String eachLine = "";
        int lineNum = 1;
        TreeNode root = new TreeNode("PROGRAM");
        reader = new BufferedReader(stringReader);
        while (eachLine != null) {
            try {
                eachLine = reader.readLine();
                if (eachLine != null) {
                    root.add((executeLine(eachLine, lineNum)));
                }
                lineNum++;
            } catch (IOException e) {
                System.err.println("读取文本时出错了！");
            }
        }
        return root;
    }

    public static void main(String[] args){
        Scanner input = new Scanner(System.in);
        System.out.println("请输入ML文本: ");
        String mltext = input.nextLine();
        Lexer lexer = new Lexer();
        while (!mltext.equals("\n")) {
            lexer.execute(mltext);
            mltext = input.next();
        }
    }

}
