package structure;

import javax.swing.tree.DefaultMutableTreeNode;

public class TreeNode extends DefaultMutableTreeNode {
    //当前节点类型
    private String nodeKind;
    //当前节点内容
    private String content;
    //当前行号
    private int lineNum;

    public TreeNode(){
        super();
        nodeKind="";
        content="";
    }
    public TreeNode(String content){
        super(content);
        nodeKind="";
        this.content=content;
    }
    public TreeNode(String nodeKind,String content){
        super(content);
        this.nodeKind=nodeKind;
        this.content=content;
    }
    public TreeNode(String nodeKind,String content,int lineNum){
        super(content);
        this.nodeKind=nodeKind;
        this.content=content;
        this.lineNum=lineNum;
    }

    public void setNodeKind(String nodeKind){
        this.nodeKind=nodeKind;
    }
    public String getNodeKind(){
        return nodeKind;
    }

    public void setContent(String content){
        this.content=content;
        setUserObject(content);
    }
    public String getContent(){
        return content;
    }

    public void setLineNum(int lindNum){
        this.lineNum=lineNum;
    }
    public int getLineNum(){
        return lineNum;
    }

    /**
     * 为节点添加子节点
     *
     * @param childNode 子节点
     *
     */
    public void add(TreeNode childNode){
        super.add(childNode);
    }

    public TreeNode getChildAt(int index){
        return (TreeNode) super.getChildAt(index);
    }
}
