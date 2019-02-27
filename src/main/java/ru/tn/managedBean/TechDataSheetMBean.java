package ru.tn.managedBean;

import org.primefaces.PrimeFaces;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import ru.tn.entity.ObjTypeEntity;
import ru.tn.model.ObjTypePropertyModel;
import ru.tn.model.TreeNodeModel;
import ru.tn.sessionBean.ObjTypePropSBean;
import ru.tn.sessionBean.ObjTypeSBean;
import ru.tn.sessionBean.TreeSBean;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ManagedBean(name = "techDataSheet")
@ViewScoped
public class TechDataSheetMBean implements Serializable {

    @ManagedProperty("#{headerTemplate.userPrincipal}")
    private String user;

    @EJB
    private ObjTypeSBean objTypeBean;
    @EJB
    private TreeSBean bean;
    @EJB
    private ObjTypePropSBean objTypePropBean;

    private long selectedObjType;
    private List<ObjTypeEntity> objTypeList;
    private static final int DEFAULT_TYPE_ID = 1;

    private TreeNode root;
    private TreeNode selectedNode;
    private TreeNode oldSelectNode;
    private long timeOldSelectNode;
    private String parentNode = DEFAULT_PARENT_NODE;
    private List<TreeNode> nodePath = new ArrayList<>();
    private static final String DEFAULT_NODE_NAME = "NODE";
    private static final String DEFAULT_PARENT_NODE = "S545";

    private long selectedSearch;
    private List<ObjTypePropertyModel> searchList;
    private String searchText;

    @PostConstruct
    private void init() {
        objTypeList = new ArrayList<>();
        objTypeList.addAll(objTypeBean.getTypes());
        selectedObjType = DEFAULT_TYPE_ID;

        searchList = new ArrayList<>();
        searchList.addAll(objTypePropBean.getObjTypeProps(selectedObjType));
        selectedSearch = objTypePropBean.getDefaultSearchId();

        root = new DefaultTreeNode(new TreeNodeModel("ROOT"), null);
        addTreeItems(root);
    }

    private void addTreeItems(TreeNode rootNode) {
        if ((rootNode == root) || ((rootNode.getChildCount() == 1)
                && (((TreeNodeModel) rootNode.getChildren().get(0).getData()).getName().equals(DEFAULT_NODE_NAME)))) {
            rootNode.getChildren().clear();
        } else {
            return;
        }

        System.out.println(System.currentTimeMillis());
        List<TreeNodeModel> data = bean.getTreeNode(selectedObjType, selectedSearch, searchText, user, 1, parentNode);
        System.out.println(System.currentTimeMillis());

        Collections.sort(data);

        for (TreeNodeModel item: data) {
            if (item.isLeaf()) {
                rootNode.getChildren().add(new DefaultTreeNode(item));
            } else {
                TreeNode node = new DefaultTreeNode(item, rootNode);
                node.getChildren().add(new DefaultTreeNode(new TreeNodeModel(DEFAULT_NODE_NAME)));

                if (!nodePath.isEmpty()
                        && (item.getId().equals(((TreeNodeModel) nodePath.get(nodePath.size() - 1).getData()).getId()))) {
                    nodePath.remove(nodePath.size() - 1);
                    parentNode = item.getId();
                    node.setExpanded(true);
                    addTreeItems(node);
                }
            }
        }

        System.out.println(nodePath);
        nodePath.clear();
    }

    public void onNodeExpand(NodeExpandEvent event) {
        updateSelect(selectedNode, event.getTreeNode());

        parentNode = ((TreeNodeModel) event.getTreeNode().getData()).getId();
        addTreeItems(event.getTreeNode());
    }

    public void onNodeCollapse(NodeCollapseEvent event) {
        updateSelect(selectedNode, event.getTreeNode());
    }

    private void updateSelect(TreeNode oldNode, TreeNode newNode) {
        StringBuilder sb = new StringBuilder();
        if (oldNode != null) {
            sb.append("PrimeFaces.widgets.orgTreeWidget.unselectNode(");
            sb.append("$(\"#navig\\\\:orgTree\\\\:");
            sb.append(oldNode.getRowKey());
            sb.append("\")");
            sb.append(", true);");
        }
        sb.append("PrimeFaces.widgets.orgTreeWidget.selectNode(");
        sb.append("$(\"#navig\\\\:orgTree\\\\:");
        sb.append(newNode.getRowKey());
        sb.append("\")");
        sb.append(", true);");
        PrimeFaces.current().executeScript(sb.toString());
    }

    public void onNodeSelect(NodeSelectEvent event) {
        if ((oldSelectNode != null) && (oldSelectNode == selectedNode)
                && ((System.currentTimeMillis() - timeOldSelectNode) < 500)) {
            if (selectedNode.isExpanded()) {
                setExpandedNode(false, selectedNode);
            } else {
                parentNode = ((TreeNodeModel) event.getTreeNode().getData()).getId();
                addTreeItems(event.getTreeNode());

                setExpandedNode(true, selectedNode);
            }
        } else {
            oldSelectNode = selectedNode;
            timeOldSelectNode = System.currentTimeMillis();
        }
    }

    private void setExpandedNode(boolean value, TreeNode node) {
        System.out.println("expand " + node);

        StringBuilder sb = new StringBuilder();
        sb.append("PrimeFaces.widgets.orgTreeWidget.");
        if (value) {
            sb.append("expandNode(");
        } else {
            sb.append("collapseNode(");
        }
        sb.append("$(\"#navig\\\\:orgTree\\\\:");
        sb.append(node.getRowKey());
        sb.append("\")");
        sb.append(", true);");
        PrimeFaces.current().executeScript(sb.toString());
    }

    public void changeObjTypeListener() {
        reloadAllTree();
    }

    public void updateSearch() {
        reloadAllTree();
    }

    private void reloadAllTree() {
        if (selectedNode == null) {
            selectedNode = root;
        } else {
            if (((TreeNodeModel) selectedNode.getData()).isLeaf()) {
                selectedNode = selectedNode.getParent();
            }
        }
        nodePath.add(selectedNode);
        while (selectedNode.getParent() != null) {
            selectedNode = selectedNode.getParent();
            nodePath.add(selectedNode);
        }
        nodePath.remove(nodePath.size() - 1);

        System.out.println(nodePath);
        selectedNode = null;

        root.getChildren().clear();
        parentNode = DEFAULT_PARENT_NODE;
        addTreeItems(root);

        searchList.clear();
        searchList.addAll(objTypePropBean.getObjTypeProps(selectedObjType));
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public TreeNode getRoot() {
        return root;
    }

    public long getSelectedSearch() {
        return selectedSearch;
    }

    public void setSelectedSearch(long selectedSearch) {
        this.selectedSearch = selectedSearch;
    }

    public List<ObjTypePropertyModel> getSearchList() {
        return searchList;
    }

    public void setSearchList(List<ObjTypePropertyModel> searchList) {
        this.searchList = searchList;
    }

    public long getSelectedObjType() {
        return selectedObjType;
    }

    public void setSelectedObjType(long selectedObjType) {
        this.selectedObjType = selectedObjType;
    }

    public List<ObjTypeEntity> getObjTypeList() {
        return objTypeList;
    }

    public void setObjTypeList(List<ObjTypeEntity> objTypeList) {
        this.objTypeList = objTypeList;
    }
}
