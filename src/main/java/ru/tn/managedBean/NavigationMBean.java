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
import ru.tn.sessionBean.NavigationSBean;
import ru.tn.util.AlphaNumComparator;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@ManagedBean(name = "navigation")
@ViewScoped
public class NavigationMBean implements Serializable {

    private static Logger log = Logger.getLogger(NavigationMBean.class.getName());

    @ManagedProperty("#{headerTemplate.userPrincipal}")
    private String user;

    @EJB
    private NavigationSBean bean;

    //Переменные нужные для типа объекта
    private long selectedObjType;
    private List<ObjTypeEntity> objTypeList;

    //Переменные нужные для дерева
    private TreeNode root;
    private String selectedNodeId;
    private TreeNode selectedNode;
    private TreeNode oldSelectNode;
    private long timeOldSelectNode;
    private String parentNode = DEFAULT_PARENT_NODE;
    private List<TreeNode> nodePath = new ArrayList<>();
    private static final String DEFAULT_NODE_NAME = "NODE";
    private static final String DEFAULT_PARENT_NODE = "S545";

    //Переменные нужные для поиска
    private long selectedSearch;
    private List<ObjTypePropertyModel> searchList;
    private String searchText;

    @PostConstruct
    private void init() {
        objTypeList = new ArrayList<>();
        objTypeList.addAll(bean.getTypes());
        try {
            selectedObjType = bean.getDefaultObjectTypeId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        searchList = new ArrayList<>();
        searchList.addAll(bean.getObjTypeProps(selectedObjType));
        selectedSearch = searchList.get(0).getObjTypeId();

        root = new DefaultTreeNode(new TreeNodeModel("ROOT"), null);
        addTreeItems(root);
    }

    /**
     * Метод доваляет новые данные в ветку дерева
     * @param rootNode ветка дерева
     */
    private void addTreeItems(TreeNode rootNode) {
        //Проверка надо ли перестраивать узел
        if ((rootNode == root) || ((rootNode.getChildCount() == 1)
                && (((TreeNodeModel) rootNode.getChildren().get(0).getData()).getName().equals(DEFAULT_NODE_NAME)))) {
            rootNode.getChildren().clear();
        } else {
            return;
        }

        //Загрузка данных для узла
        log.info("NavigationMBean.addTreeItems start load data for node: " + parentNode);
        List<TreeNodeModel> data = bean.getTreeNode(selectedObjType, selectedSearch, searchText, user, parentNode);

        data.sort(new AlphaNumComparator());

        for (TreeNodeModel item: data) {
            if (item.isLeaf()) {
                new DefaultTreeNode("leaf", item, rootNode);
            } else {
                TreeNode node = new DefaultTreeNode(item, rootNode);
                node.getChildren().add(new DefaultTreeNode(new TreeNodeModel(DEFAULT_NODE_NAME)));

                //В случае если заполнен nodePath (используется для раскрытии множества веток)
                //открывает ветку и вызывает загрузку данных для него
                if (!nodePath.isEmpty()
                        && (item.getId().equals(((TreeNodeModel) nodePath.get(nodePath.size() - 1).getData()).getId()))) {
                    nodePath.remove(nodePath.size() - 1);
                    parentNode = item.getId();
                    node.setExpanded(true);
                    addTreeItems(node);
                }
            }
        }

        nodePath.clear();
    }

    /**
     * Обработчик события открытия ветки дерева
     * @param event ветка дерева
     */
    public void onNodeExpand(NodeExpandEvent event) {
        log.log(Level.INFO, "NavigationMBean.onNodeExpand load start");

        log.info("NavigationMBean.onNodeExpand expand: " + event.getTreeNode());
        updateSelect(selectedNode, event.getTreeNode());

        parentNode = ((TreeNodeModel) event.getTreeNode().getData()).getId();
        addTreeItems(event.getTreeNode());

        log.log(Level.INFO, "NavigationMBean.onNodeExpand ok load");
    }

    /**
     * обработчик события свертывания ветки дерева
     * @param event ветка дерева
     */
    public void onNodeCollapse(NodeCollapseEvent event) {
        log.info("NavigationMBean.onNodeCollapse collapse: " + event.getTreeNode());
        updateSelect(selectedNode, event.getTreeNode());
    }

    /**
     * Метод обновляет выделение ветки дерева с помощью JS комманд
     * @param oldNode старая ветка
     * @param newNode новая ветка
     */
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

    /**
     * Обработчик выделения ветки дерева
     * обрабатывает двойное нажатие
     * @param event ветка дерева
     */
    public void onNodeSelect(NodeSelectEvent event) {
        if ((oldSelectNode != null) && (oldSelectNode == selectedNode)
                && ((System.currentTimeMillis() - timeOldSelectNode) < 500)) {
            log.info("NavigationMBean.onNodeSelect dblClick");

            if (((TreeNodeModel) selectedNode.getData()).isLeaf()) {
                PrimeFaces.current().ajax().update(":action");

                FaceletContext faceletContext = (FaceletContext) FacesContext.getCurrentInstance()
                        .getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
                PrimeFaces.current().ajax().update((String) faceletContext.getAttribute("updatePath"));

                if (!faceletContext.getAttribute("invokeScript").equals("")) {
                    PrimeFaces.current().executeScript((String) faceletContext.getAttribute("invokeScript"));
                }
            } else {
                if (selectedNode.isExpanded()) {
                    setExpandedNode(false, selectedNode);
                } else {
                    parentNode = ((TreeNodeModel) event.getTreeNode().getData()).getId();
                    addTreeItems(event.getTreeNode());

                    setExpandedNode(true, selectedNode);
                }
            }
        } else {
            oldSelectNode = selectedNode;
            timeOldSelectNode = System.currentTimeMillis();
        }
    }

    /**
     * Метод раскрывает ветку дерева с помощью JS команды
     * В этом случает обновляется только одна ветка а не все дерево
     * @param value открыть или закрыть ветку
     * @param node ветка дерева
     */
    private void setExpandedNode(boolean value, TreeNode node) {
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

    /**
     * Обработчик события изменения выбора типа объекта
     */
    public void changeObjTypeListener() {
        reloadAllTree();

        //Обновляем список типов поиска
        searchList.clear();
        searchList.addAll(bean.getObjTypeProps(selectedObjType));
    }

    /**
     * Нажатие на кнопку "перестроить"
     */
    public void updateSearch() {
        reloadAllTree();
    }

    /**
     * Метод для перестройки всего дерева.
     * Определяет глубину перестройки по выделенному объекту
     * Стирает все дерево и строить его пытаясь открыть старые узлы, если они есть
     */
    private void reloadAllTree() {
        //Определяем глубину вхождения
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

        log.info("NavigationMBean.reloadAllTree глубина обновления: " + nodePath);

        selectedNode = null;

        //Чистим дерево и запускаем его перстроение
        root.getChildren().clear();
        parentNode = DEFAULT_PARENT_NODE;
        addTreeItems(root);
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
        if (Objects.nonNull(this.selectedNode)) {
            selectedNodeId = ((TreeNodeModel) this.selectedNode.getData()).getId();
        }
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

    public String getSelectedNodeId() {
        return selectedNodeId;
    }

    public void setSelectedNodeId(String selectedNodeId) {
        this.selectedNodeId = selectedNodeId;
    }
}
