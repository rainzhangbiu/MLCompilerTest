package compiler;

import structure.Token;
import structure.TreeNode;

import java.util.ArrayList;


public class Main {
    public static void main(String[] args){
        /**
         * 词法分析部分
         * 展示displayTokens
         */
        Lexer lexer = new Lexer();
        lexer.execute();
        //展示DisplayTokens
        ArrayList<Token> display = lexer.getTokens();
        for (Token token : display) {
            System.out.println("行号:" + token.getLine() + "  列号:" + token.getColumn() + "  格式:" + token.getKind());
        }

        /**语法分析部分
         * 用词法分析获得的Tokens完成
         * 生成语法树
         */
        ArrayList<Token> tokens = lexer.getTokens();
        Parser parser = new Parser(tokens);
        parser.setIndex(0);
        parser.setErrorInfo("");
        parser.setErrorNum(0);
        TreeNode ast = parser.execute();

        /**
         * 语义分析部分
         */
        SemanticAnalysis sa = new SemanticAnalysis(ast);
        sa.execute();
    }
}
