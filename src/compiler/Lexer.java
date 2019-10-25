package compiler;

import jdk.jshell.execution.JdiInitiator;
import structure.ConstVar;
import structure.Token;
import structure.TreeNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Standard ML词法分析类
 *
 * @author 章雨
 *
 * 参考链接:https://github.com/WuXianglong/CMMCompiler/blob/master/CMMCompiler/src/compiler/CMMLexer.java
 */
public class Lexer {
    //注释的标志
    private boolean isNotation=false;
    //分析后得到的Tokensjihe，用于之后的语法以及语义分析
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

    public void setTokens(ArrayList<Token> tokens){
        this.tokens=tokens;
    }

    public ArrayList<Token> getDisplayTokens(){
        return displayTokens;
    }

    public void setDisplayTokens(ArrayList<Token> displayToken){
        this.displayTokens=displayTokens;
    }

    /**
     * 识别字母的方法
     *
     * @param c
     *
     * @return
     */
    private static boolean isLetter(char c){
        if((c>='a'&&c<='z')||(c>='A'&&c<='Z')||c=='_')
            return true;
        return false;
    }

    /**
     * 识别数字的方法
     *
     * @param c
     *
     * @return
     */
    private static boolean isDigit(char c){
        if(c>='0'&&c<='9')
            return true;
        return false;
    }

    /**
     * 识别正确的整数，排除多个零的情况
     *
     * @param input
     *
     * @return
     */
    private static boolean matchInteger(String input){
        //理解matches方法
        if(input.matches("^-?\\d+$")&&input.matches("^-?0{1,}\\d+$"))
            return true;
        else
            return false;
    }

    /**
     * 识别正确的实数：排除00.000的情况
     *
     * @param input
     *
     * @return
     */
    private static boolean matchReal(String input){
        if(input.matches("^(-?\\\\d+)(\\\\.\\\\d+)+$"))
            return true;
        else
            return false;
    }

    /**
     *
     * 识别正确的标识符：以字母开头，由数字、字母、下划线、单引号组成
     *
     * @param input
     *
     * @return
     */
    private static boolean matchID(String input){
        if(input.matches("^\\w+$") && !input.endsWith("_") && input.substring(0, 1).matches("[A-Za-z]"))
            return true;
        else
            return false;
    }

    /**
     * 识别保留字
     *
     * @param str
     *
     * @return
     */
    private static boolean isKey(String str){
        if(str.equals("if")||str.equals("else")||str.equals("then")||str.equals("val")||str.equals("true")||str.equals("false"))
            return true;
        else
            return false;
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
     * @param mlText
     *
     * @lineNum
     *
     * @return
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
                    || isLetter(ch) || isDigit(ch)
                    || String.valueOf(ch).equals(" ")
                    || String.valueOf(ch).equals("\n")
                    || String.valueOf(ch).equals("\r")
                    || String.valueOf(ch).equals("\t")){
                switch(state){
                    //循环程序开始时进入case 0
                    case 0:
                        //分隔符直接打印
                        if(ch == '(' || ch == ')' || ch == '{' || ch == '}' || ch == ';' || ch == ',')
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
                            node.add(new TreeNode("分隔符：" + ch));
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
                        node.add(new TreeNode("运算符： " + ConstVar.PLUS));
                        tokens.add(new Token(lineNum, i, "运算符", ConstVar.PLUS));
                        displayTokens.add(new Token(lineNum,i,"运算符", ConstVar.PLUS));
                        //如果没有，则会跳过一个字符
                        i--;
                        state = 0;
                        break;

                    case 2:
                        node.add(new TreeNode("运算符： " + ConstVar.MINUS));
                        tokens.add(new Token(lineNum, i, "运算符： ", ConstVar.MINUS));
                        displayTokens.add(new Token(lineNum, i, "运算符： ", ConstVar.MINUS));
                        i--;
                        state = 0;
                        break;

                    case 3:
                        node.add(new TreeNode("运算符" +  ConstVar.TIMES));
                        tokens.add(new Token(lineNum, i, "运算符", ConstVar.TIMES));
                        displayTokens.add(new Token(lineNum, i, "运算符", ConstVar.TIMES));
                        i--;
                        state = 0;
                        break;

                    case 4:
                        node.add(new TreeNode("运算符" + ConstVar.DIVIDE));
                        tokens.add(new Token(lineNum, i, "运算符", ConstVar.DIVIDE));
                        displayTokens.add(new Token(lineNum, i, "运算符", ConstVar.DIVIDE));
                        i--;
                        state = 0;
                        break;
                    case 5:
                        if(ch == '='){
                            node.add(new TreeNode("运算符" + ConstVar.EQUAL));
                            tokens.add(new Token(lineNum, i, "运算符", ConstVar.EQUAL));
                            displayTokens.add(new Token(lineNum, i, "运算符", ConstVar.EQUAL));
                            state = 0 ;
                        }else{
                            node.add(new TreeNode("运算符" + ConstVar.ASSIGN));
                            tokens.add(new Token(lineNum, i, "运算符", ConstVar.ASSIGN));
                            displayTokens.add(new Token(lineNum, i, "运算符", ConstVar.ASSIGN));
                            i--;
                            state = 0;
                        }
                        break;
                    case 6:
                       node.add(new TreeNode("运算符" + ConstVar.LT));
                       tokens.add(new Token(lineNum, i, "运算符", ConstVar.LT));
                       displayTokens.add(new Token(lineNum, i, "运算符", ConstVar.LT));
                       i--;
                       state = 0;
                       break;
                    case 7:
                        node.add(new TreeNode("运算符" + ConstVar.GT));
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
                                node.add(new TreeNode("整数 ：" + id));
                                tokens.add(new Token(lineNum, begin + 1, "整数", id));
                                displayTokens.add(new Token(lineNum, begin + 1, "整数", id));
                            }else if (matchReal(id)){
                                node.add(new TreeNode("实数 ：" + id));
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
                                node.add(new TreeNode("关键字: " + id));
                                tokens.add(new Token(lineNum, begin + 1, "关键字", id));
                                displayTokens.add(new Token(lineNum, begin + 1, "关键字", id));
                            }else if (matchID(id)) {
                                node.add(new TreeNode("标识符: " + id));
                                tokens.add(new Token(lineNum, begin + 1, "标识符", id));
                                displayTokens.add(new Token(lineNum, begin + 1, "标识符", id));
                            }
                            i--;
                            state = 0;
                        }
                        break;
                    case 10:
                        if (ch == '"') {
                            end = i;
                            String string = mlText.substring(begin, end);
                            node.add(new TreeNode("字符串 ： " + string));
                            tokens.add(new Token(lineNum, begin + 1, "字符串",
                                    string));
                            displayTokens.add(new Token(lineNum, begin + 1,
                                    "字符串", string));
                            node.add(new TreeNode("分隔符 ： " + ConstVar.DQ));
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
     * 分析ML程序，并返回词法分析结果的根节点
     *
     * @param mlText
     * ML文本
     * @return 词法分析结果跟节点
     */
    public TreeNode execute(String mlText) {
        setTokens(new ArrayList<Token>());
        setDisplayTokens(new ArrayList<Token>());
        setNotation(false);
        StringReader stringReader = new StringReader(mlText);
        String eachLine = "";
        int lineNum = 1;
        TreeNode root = new TreeNode("PROGRAM");
        reader = new BufferedReader(stringReader);
        while (eachLine != null) {
            try {
                eachLine = reader.readLine();
                if (eachLine != null) {
                    if (isNotation() && !eachLine.contains("*/")) {
                        eachLine += "\n";
                        TreeNode temp = new TreeNode(eachLine);
                        temp.add(new TreeNode("多行注释"));
                        displayTokens.add(new Token(lineNum, 1, "注释", eachLine
                                .substring(0, eachLine.length() - 1)));
                        displayTokens.add(new Token(lineNum,
                                eachLine.length() - 1, "换行符", "\n"));
                        root.add(temp);
                        lineNum++;
                        continue;
                    } else {
                        root.add((executeLine(eachLine, lineNum)));
                    }
                }
                lineNum++;
            } catch (IOException e) {
                System.err.println("读取文本时出错了！");
            }
        }
        return root;
    }
}
