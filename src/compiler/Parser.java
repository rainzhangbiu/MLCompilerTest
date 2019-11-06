package compiler;

import java.util.ArrayList;

import structure.ConstVar;
import structure.Token;
import structure.TreeNode;

public class Parser {
    // 词法分析得到的tokens向量
    private ArrayList<Token> tokens;
    // 标记当前token的游标
    private int index = 0;
    // 存放当前token的值
    private Token currentToken = null;
    // 错误个数
    private int errorNum = 0;
    // 错误信息
    private String errorInfo = "";
    // 语法分析根结点
    private static TreeNode root;

    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        if (tokens.size() != 0)
            currentToken = tokens.get(0);
    }

    public int getErrorNum() {
        return errorNum;
    }

    public void setErrorNum(int errorNum) {
        this.errorNum = errorNum;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * 语法分析主方法
     *
     * @return TreeNode
     */
    public TreeNode execute() {
        root = new TreeNode("PROGRAM");
        for (; index < tokens.size();) {
            root.add(statement());
        }
        return root;
    }

    /**
     * 取出tokens中的下一个token
     *
     */
    private void nextToken() {
        index++;
        if (index > tokens.size() - 1) {
            currentToken = null;
            if (index > tokens.size())
                index--;
            return;
        }
        currentToken = tokens.get(index);
    }

    /**
     * 出错处理函数
     *
     * @param error
     *            出错信息
     */
    private void error(String error) {
        String line = "    ERROR:第 ";
        Token previous = tokens.get(index - 1);
        if (currentToken != null
                && currentToken.getLine() == previous.getLine()) {
            line += currentToken.getLine() + " 行,第 " + currentToken.getColumn()
                    + " 列：";
        } else
            line += previous.getLine() + " 行,第 " + previous.getColumn() + " 列：";
        errorInfo += line + error;
        errorNum++;
    }

    /**
     * statement: if_stm | assign_stm |declare_stm |type_stm
     *
     * @return TreeNode
     */
    private TreeNode statement() {
        // 保存要返回的结点
        TreeNode tempNode = null;
        // 赋值语句
        if (currentToken != null && currentToken.getKind().equals("标识符")) {
            tempNode = assign_stm();
        }
        // 声明语句
        else if (currentToken != null
                && (currentToken.getContent().equals(ConstVar.VAL)
                || currentToken.getContent().equals(ConstVar.LET)
                || currentToken.getContent().equals(ConstVar.LOCAL))){
            tempNode = declare_stm();
        }
        // If条件语句
        else if (currentToken != null
                && currentToken.getContent().equals(ConstVar.IF)) {
            tempNode = if_stm();
        }
        //类型约束
        else if (currentToken != null
                && currentToken.getContent().equals(ConstVar.TYPE)){
            tempNode = type_stm();
        }
        return tempNode;
    }

    /**
     * if_stm: IF condition THEN statement ELSE statement SEMICOLON;
     *
     * @return TreeNode
     */
    private TreeNode if_stm() {
        // if函数返回结点的根结点
        TreeNode ifNode = new TreeNode("关键字", "if", currentToken.getLine());
        nextToken();
        // condition
        TreeNode conditionNode = new TreeNode("condition", "Condition",
                currentToken.getLine());
        ifNode.add(conditionNode);
        conditionNode.add(condition());
        //匹配THEN
        if (currentToken != null && currentToken.getContent().equals(ConstVar.THEN)){
            // statement
            TreeNode statementNode = new TreeNode("statement", "Statements",
                    currentToken.getLine());
            ifNode.add(statementNode);
            nextToken();
            if (currentToken != null)
                statementNode.add(statement());
            nextToken();
        } else {
            String error = " 缺少关键字THEN" + "\n";
            error(error);
            ifNode.add(new TreeNode(ConstVar.ERROR
                    + "缺少关键字THEN"));
            nextToken();
        }
        //匹配ELSE
        if (currentToken != null
                && currentToken.getContent().equals(ConstVar.ELSE)) {
            TreeNode elseNode = new TreeNode("关键字", ConstVar.ELSE, currentToken
                    .getLine());
            ifNode.add(elseNode);
            nextToken();
            if (currentToken != null)
                elseNode.add(statement());
            nextToken();
        }else {
            String error = " 缺少关键字ELSE" + "\n";
            error(error);
            ifNode.add(new TreeNode(ConstVar.ERROR
                    + "缺少关键字ELSE"));
            nextToken();
        }
        return ifNode;
    }

    /**
     * assign_stm: ID ASSIGN expression SEMICOLON;
     *
     * @return TreeNode
     */
    private TreeNode assign_stm() {
        // assign函数返回结点的根结点
        TreeNode assignNode = new TreeNode("运算符", ConstVar.ASSIGN, currentToken
                .getLine());
        TreeNode idNode = new TreeNode("标识符", currentToken.getContent(),
                currentToken.getLine());
        assignNode.add(idNode);
        nextToken();
        // 匹配赋值符号=
        if (currentToken != null
                && currentToken.getContent().equals(ConstVar.ASSIGN)) {
            nextToken();
        } else { // 报错
            String error = " 赋值语句缺少\"=\"" + "\n";
            error(error);
            return new TreeNode(ConstVar.ERROR + "赋值语句缺少\"=\"");
        }
        // expression
        assignNode.add(condition());
        // 匹配分号;
        if (currentToken != null
                && currentToken.getContent().equals(ConstVar.SEMICOLON)) {
            nextToken();
        } else { // 报错
            String error = " 赋值语句缺少分号\";\"" + "\n";
            error(error);
            assignNode.add(new TreeNode(ConstVar.ERROR + "赋值语句缺少分号\";\""));
            nextToken();
        }
        return assignNode;
    }

    /**
     * declare_stm: (LOCAL | LET) val_stm (IN expression END) SEMICOLON;
     *
     * @return TreeNode
     */
    private TreeNode declare_stm() {
        TreeNode declareNode = new TreeNode("关键字", currentToken.getContent(),
                currentToken.getLine());
        if (currentToken.getContent().equals(ConstVar.VAL)){
            declareNode = val_stm(declareNode);
        } else if (currentToken.getContent().equals(ConstVar.LOCAL) || currentToken.getContent().equals(ConstVar.LET)){
            nextToken();
            TreeNode valNode = new TreeNode("关键字", ConstVar.VAL, currentToken.getLine());
            declareNode.add(val_stm(valNode));
            //判断IN
            if (currentToken != null && currentToken.getContent().equals(ConstVar.IN)){
                declareNode.add(new TreeNode("关键字", ConstVar.IN, currentToken.getLine()));
                nextToken();
            }else {
                String error = "   缺少关键字IN" + "\n";
                error(error);
                declareNode.add(new TreeNode(ConstVar.ERROR + "缺少关键字IN"));
                nextToken();
            }
            //判断codition
            declareNode.add(condition());
            //判断END
            if (currentToken != null && currentToken.getContent().equals(ConstVar.END)){
                declareNode.add(new TreeNode("关键字", ConstVar.END, currentToken.getLine()));
                nextToken();
            }else {
                String error = "   缺少关键字END" + "\n";
                error(error);
                declareNode.add(new TreeNode(ConstVar.ERROR + "缺少关键字END"));
                nextToken();
            }
        } else {
            String error = " 声明语句中关键词出错" + "\n";
            error(error);
            declareNode.add(new TreeNode(ConstVar.ERROR + "声明语句中标识符出错"));
            nextToken();
        }
        // 匹配分号;
        if (currentToken != null
                && currentToken.getContent().equals(ConstVar.SEMICOLON)) {
            nextToken();
        } else { // 报错
            String error = " 声明语句缺少分号\";\"" + "\n";
            error(error);
            declareNode.add(new TreeNode(ConstVar.ERROR + "声明语句缺少分号\";\""));
            nextToken();
        }
        return declareNode;
    }

    /**
     * val_stm: VAL ID COLON TYPE ASSGIN condition
     *
     * @param root 根结点declareNode
     */
    private TreeNode val_stm(TreeNode root){
        nextToken();
        //匹配标识符
        if (currentToken != null && currentToken.getKind().equals("标识符")){
            root.add(new TreeNode("标识符", currentToken.getContent(), currentToken.getLine()));
            nextToken();
        }else {
            String error = " 声明语句中标识符出错" + "\n";
            error(error);
            root.add(new TreeNode(ConstVar.ERROR + "声明语句中标识符出错"));
            nextToken();
        }
        //匹配冒号
        if (currentToken != null && currentToken.getContent().equals(ConstVar.COLON)){
            nextToken();
        }else {
            String error = " val声明语句缺少冒号\";\"" + "\n";
            error(error);
            root.add(new TreeNode(ConstVar.ERROR + "val声明语句缺少冒号\";\""));
            nextToken();
        }
        //匹配类型关键字
        if (currentToken != null && (currentToken.getContent().equals(ConstVar.INT)
                || currentToken.getContent().equals(ConstVar.STRING)
                || currentToken.getContent().equals(ConstVar.CHAR)
                || currentToken.getContent().equals(ConstVar.BOOL)
                || currentToken.getContent().equals(ConstVar.REAL) )){
            root.add(new TreeNode("关键字", currentToken.getContent(), currentToken.getLine()));
            nextToken();
        }else {
            String error = " 声明语句中缺少类型关键字" + "\n";
            error(error);
            root.add(new TreeNode(ConstVar.ERROR + "声明语句中缺少类型关键字"));
            nextToken();
        }
        //匹配赋值符号
        if (currentToken != null && currentToken.getContent().equals(ConstVar.ASSIGN)) {
            root.add(new TreeNode("分隔符", ConstVar.ASSIGN, currentToken.getLine()));
            nextToken();
            root.add(condition());
        } else { // 报错
            String error = " 赋值语句缺少\"=\"" + "\n";
            error(error);
            root.add(new TreeNode(ConstVar.ERROR + "\" 赋值语句缺少\\\"=\\\"\" "));
            nextToken();
        }

        return root;
    }

    /**
     * condition: (expression (comparison_op expression)? | ID) COLON type;
     *
     * @return TreeNode
     */
    private TreeNode condition() {
        // 记录expression生成的结点
        TreeNode tempNode = expression();
        nextToken();
        // 如果条件判断为比较表达式
        if (currentToken != null
                && (currentToken.getContent().equals(ConstVar.EQUAL)
                || currentToken.getContent().equals(ConstVar.LT)
                || currentToken.getContent().equals(ConstVar.GT)
                || currentToken.getContent().equals(ConstVar.NEQUAL))) {
            TreeNode comparisonNode = comparison_op();
            comparisonNode.add(tempNode);
            comparisonNode.add(expression());
            return comparisonNode;
        } else if (currentToken != null && currentToken.getContent().equals(ConstVar.COLON)){//匹配冒号
            tempNode.add(new TreeNode("分隔符", ConstVar.COLON, currentToken.getLine()));
            nextToken();
        }
        //匹配类型关键字
        if (currentToken != null && (currentToken.getContent().equals(ConstVar.INT)
                || currentToken.getContent().equals(ConstVar.STRING)
                || currentToken.getContent().equals(ConstVar.CHAR)
                || currentToken.getContent().equals(ConstVar.BOOL)
                || currentToken.getContent().equals(ConstVar.REAL) )){
            tempNode.add(new TreeNode("关键字", currentToken.getContent(), currentToken.getLine()));
            nextToken();
        }else {
            String error = " 类型断言语句中缺少类型关键字" + "\n";
            error(error);
            tempNode.add(new TreeNode(ConstVar.ERROR + "类型断言语句中缺少类型关键字"));
            nextToken();
        }
        return tempNode;
    }

    /**
     * expression: term (add_op term)?;
     *
     * @return TreeNode
     */
    private TreeNode expression() {
        // 记录term生成的结点
        TreeNode tempNode = term();

        // 如果下一个token为加号或减号
        while (currentToken != null
                && (currentToken.getContent().equals(ConstVar.PLUS)
                || currentToken.getContent().equals(ConstVar.MINUS))) {
            // add_op
            TreeNode addNode = add_op();
            addNode.add(tempNode);
            tempNode = addNode;
            tempNode.add(term());
        }
        return tempNode;
    }

    /**
     * term : factor (mul_op factor)?;
     *
     * @return TreeNode
     */
    private TreeNode term() {
        // 记录factor生成的结点
        TreeNode tempNode = factor();

        // 如果下一个token为乘号或除号
        while (currentToken != null
                && (currentToken.getContent().equals(ConstVar.TIMES) || currentToken
                .getContent().equals(ConstVar.DIVIDE) || currentToken.getContent().equals(ConstVar.DIV))) {
            // mul_op
            TreeNode mulNode = mul_op();
            mulNode.add(tempNode);
            tempNode = mulNode;
            tempNode.add(factor());
        }
        return tempNode;
    }

    /**
     * factor : TRUE | FALSE | REAL_LITERAL | INTEGER_LITERAL | ID | LPAREN
     * expression RPAREN | DQ string DQ
     *
     * @return TreeNode
     */
    private TreeNode factor() {
        // 保存要返回的结点
        TreeNode tempNode = null;
        if (currentToken != null && currentToken.getKind().equals("整数")) {
            tempNode = new TreeNode("整数", currentToken.getContent(),
                    currentToken.getLine());
            nextToken();
        } else if (currentToken != null && currentToken.getKind().equals("实数")) {
            tempNode = new TreeNode("实数", currentToken.getContent(),
                    currentToken.getLine());
            nextToken();
        } else if (currentToken != null
                && currentToken.getContent().equals(ConstVar.TRUE)) {
            tempNode = new TreeNode("布尔值", currentToken.getContent(),
                    currentToken.getLine());
            nextToken();
        } else if (currentToken != null
                && currentToken.getContent().equals(ConstVar.FALSE)) {
            tempNode = new TreeNode("布尔值", currentToken.getContent(),
                    currentToken.getLine());
            nextToken();
        } else if (currentToken != null && currentToken.getKind().equals("标识符")) {
            tempNode = new TreeNode("标识符", currentToken.getContent(),
                    currentToken.getLine());
            nextToken();
        } else if (currentToken != null
                && currentToken.getContent().equals(ConstVar.LPAREN)) { // 匹配左括号(
            nextToken();
            tempNode = expression();
            // 匹配右括号)
            if (currentToken != null
                    && currentToken.getContent().equals(ConstVar.RPAREN)) {
                nextToken();
            } else { // 报错
                String error = " 算式因子缺少右括号\")\"" + "\n";
                error(error);
                return new TreeNode(ConstVar.ERROR + "算式因子缺少右括号\")\"");
            }
        } else if (currentToken != null
                && currentToken.getContent().equals(ConstVar.DQ)) { // 匹配双引号
            nextToken();
            tempNode = new TreeNode("字符串", currentToken.getContent(),
                    currentToken.getLine());
            nextToken();
            // 匹配另外一个双引号
            nextToken();
        } else { // 报错
            String error = " 算式因子存在错误" + "\n";
            error(error);
            if (currentToken != null
                    && !currentToken.getContent().equals(ConstVar.SEMICOLON)) {
                nextToken();
            }
            return new TreeNode(ConstVar.ERROR + "算式因子存在错误");
        }
        return tempNode;
    }

    /**
     * add_op : PLUS | MINUS;
     *
     * @return TreeNode
     */
    private TreeNode add_op() {
        // 保存要返回的结点
        TreeNode tempNode = null;
        if (currentToken != null
                && currentToken.getContent().equals(ConstVar.PLUS)) {
            tempNode = new TreeNode("运算符", ConstVar.PLUS, currentToken
                    .getLine());
            nextToken();
        } else if (currentToken != null
                && currentToken.getContent().equals(ConstVar.MINUS)) {
            tempNode = new TreeNode("运算符", ConstVar.MINUS, currentToken
                    .getLine());
            nextToken();
        } else { // 报错
            String error = " 加减符号出错" + "\n";
            error(error);
            return new TreeNode(ConstVar.ERROR + "加减符号出错");
        }
        return tempNode;
    }

    /**
     * mul_op : TIMES | DIVIDE;
     *
     * @return TreeNode
     */
    private TreeNode mul_op() {
        // 保存要返回的结点
        TreeNode tempNode = null;
        if (currentToken != null
                && currentToken.getContent().equals(ConstVar.TIMES)) {
            tempNode = new TreeNode("运算符", ConstVar.TIMES, currentToken
                    .getLine());
            nextToken();
        } else if (currentToken != null
                && currentToken.getContent().equals(ConstVar.DIVIDE)) {
            tempNode = new TreeNode("运算符", ConstVar.DIVIDE, currentToken
                    .getLine());
            nextToken();
        } else if(currentToken != null && currentToken.getContent().equals(ConstVar.DIV)){
            tempNode = new TreeNode("运算符", ConstVar.DIV, currentToken.getLine());
            nextToken();
        } else { // 报错
            String error = " 乘除符号出错" + "\n";
            error(error);
            return new TreeNode(ConstVar.ERROR + "乘除符号出错");
        }
        return tempNode;
    }

    /**
     * comparison_op: LT | GT | EQUAL | NEQUAL;
     *
     * @return TreeNode
     */
    private TreeNode comparison_op() {
        // 保存要返回的结点
        TreeNode tempNode = null;
        if (currentToken != null
                && currentToken.getContent().equals(ConstVar.LT)) {
            tempNode = new TreeNode("运算符", ConstVar.LT, currentToken.getLine());
            nextToken();
        } else if (currentToken != null
                && currentToken.getContent().equals(ConstVar.GT)) {
            tempNode = new TreeNode("运算符", ConstVar.GT, currentToken.getLine());
            nextToken();
        } else if (currentToken != null
                && currentToken.getContent().equals(ConstVar.EQUAL)) {
            tempNode = new TreeNode("运算符", ConstVar.EQUAL, currentToken
                    .getLine());
            nextToken();
        } else { // 报错
            String error = " 比较运算符出错" + "\n";
            error(error);
            return new TreeNode(ConstVar.ERROR + "比较运算符出错");
        }
        return tempNode;
    }
    /**
     * type_stm:TYPE ID ASSIGN type
     *
     * @ return TreeNode
     */
    private TreeNode type_stm(){
        TreeNode typeNode = new TreeNode("关键字", ConstVar.TYPE,currentToken.getLine());
        nextToken();
        //匹配标识符
        if (currentToken != null && currentToken.getKind().equals("标识符")){
            typeNode.add(new TreeNode("标识符", currentToken.getContent(), currentToken.getLine()));
            nextToken();
        } else {
            String error = "  类型约束语句中缺少标识符" + "\n";
            error(error);
            typeNode.add(new TreeNode(ConstVar.ERROR + "类型约束语句中缺少标识符"));
            nextToken();
        }
        //匹配赋值符号
        if (currentToken != null && currentToken.getContent().equals(ConstVar.ASSIGN)){
            typeNode.add(new TreeNode("分隔符", ConstVar.ASSIGN, currentToken.getLine()));
            nextToken();
        } else {
            String error = " 类型约束语句缺少\"=\"" + "\n";
            error(error);
            typeNode.add(new TreeNode(ConstVar.ERROR + "\" 类型约束语句缺少\\\"=\\\"\" "));
            nextToken();
        }
        //匹配类型关键字
        if (currentToken != null && (currentToken.getContent().equals(ConstVar.INT)
                || currentToken.getContent().equals(ConstVar.STRING)
                || currentToken.getContent().equals(ConstVar.CHAR)
                || currentToken.getContent().equals(ConstVar.BOOL)
                || currentToken.getContent().equals(ConstVar.REAL) )){
            typeNode.add(new TreeNode("关键字", currentToken.getContent(), currentToken.getLine()));
            nextToken();
        }else {
            String error = " 类型约束语句中缺少类型关键字" + "\n";
            error(error);
            typeNode.add(new TreeNode(ConstVar.ERROR + "类型约束语句中缺少类型关键字"));
            nextToken();
        }
        return typeNode;
    }

}
