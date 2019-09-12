package ru.tn.managedBean;

import org.primefaces.PrimeFaces;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.TabChangeEvent;
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

    @EJB
    private NavigationSBean bean;

    private String user;

    //Переменные для панели закладок
    private String selectedTab;
    private static final String[] DEFAULT_TAB_ID = {"org", "ter"};

    //Переменные нужные для типа объекта
    private long selectedObjType;
    private List<ObjTypeEntity> objTypeList;

    //Переменные нужные для дерева
    private Map<String, TreeNode> root = new HashMap<>();
    private String selectedNodeId;
    private Map<String, TreeNode> selectedNode = new HashMap<>();
    private Map<String, TreeNode> oldSelectNode = new HashMap<>();
    private long timeOldSelectNode;
    private Map<String, String> parentNode = new HashMap<>();
    private Map<String, List<TreeNode>> nodePath = new HashMap<>();
    private static final String DEFAULT_NODE_NAME = "NODE";
    private static final Map<String, String> DEFAULT_PARENT_NODE = new HashMap<String, String>() {{
        put(DEFAULT_TAB_ID[0], "S545");
        put(DEFAULT_TAB_ID[1], "G1");
    }};

    //Переменные нужные для поиска
    private long selectedSearch;
    private List<ObjTypePropertyModel> searchList;
    private String searchText;

    //Параметры для обработки действия по двойному клику
    private Object beanName;
    private String method;

    @PostConstruct
    private void init() {
        user = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
        FaceletContext faceletContext = (FaceletContext) FacesContext.getCurrentInstance()
                .getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
        beanName = faceletContext.getAttribute("bean");
        method = (String) faceletContext.getAttribute("method");

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

        for (String item: DEFAULT_TAB_ID) {
            root.put(item, new DefaultTreeNode(new TreeNodeModel("ROOT"), null));
            parentNode.put(item, DEFAULT_PARENT_NODE.get(item));
            nodePath.put(item, new ArrayList<>());
            selectedTab = item;
            addTreeItems(root.get(item));
        }

        selectedTab = DEFAULT_TAB_ID[0];
    }

    /**
     * Метод доваляет новые данные в ветку дерева
     * @param rootNode ветка дерева
     */
    private void addTreeItems(TreeNode rootNode) {
        //Проверка надо ли перестраивать узел
        if ((rootNode == root.get(selectedTab)) || ((rootNode.getChildCount() == 1)
                && (((TreeNodeModel) rootNode.getChildren().get(0).getData()).getName().equals(DEFAULT_NODE_NAME)))) {
            rootNode.getChildren().clear();
        } else {
            return;
        }

        //Загрузка данных для узла
        log.info("NavigationMBean.addTreeItems start load data for node: " + parentNode.get(selectedTab));
        List<TreeNodeModel> data = bean.getTreeNode(selectedObjType, selectedSearch, searchText, user, parentNode.get(selectedTab), selectedTab);

        data.sort(new AlphaNumComparator());

        for (TreeNodeModel item: data) {
            if (item.isLeaf()) {
                new DefaultTreeNode("leaf", item, rootNode);
            } else {
                TreeNode node = new DefaultTreeNode(item, rootNode);
                node.getChildren().add(new DefaultTreeNode(new TreeNodeModel(DEFAULT_NODE_NAME)));

                //В случае если заполнен nodePath (используется для раскрытии множества веток)
                //открывает ветку и вызывает загрузку данных для него
                if (!nodePath.get(selectedTab).isEmpty()
                        && (item.getId().equals(((TreeNodeModel) nodePath.get(selectedTab).get(nodePath.get(selectedTab).size() - 1).getData()).getId()))) {
                    nodePath.get(selectedTab).remove(nodePath.get(selectedTab).size() - 1);
                    parentNode.put(selectedTab, item.getId());
                    node.setExpanded(true);
                    addTreeItems(node);
                }
            }
        }

        nodePath.get(selectedTab).clear();
    }

    /**
     * Обработчик события открытия ветки дерева
     * @param event ветка дерева
     */
    public void onNodeExpand(NodeExpandEvent event) {
        log.log(Level.INFO, "NavigationMBean.onNodeExpand load start");

        log.info("NavigationMBean.onNodeExpand expand: " + event.getTreeNode());
        updateSelect(selectedNode.get(selectedTab), event.getTreeNode());

        parentNode.put(selectedTab, ((TreeNodeModel) event.getTreeNode().getData()).getId());
        addTreeItems(event.getTreeNode());

        log.log(Level.INFO, "NavigationMBean.onNodeExpand ok load");
    }

    /**
     * обработчик события свертывания ветки дерева
     * @param event ветка дерева
     */
    public void onNodeCollapse(NodeCollapseEvent event) {
        log.info("NavigationMBean.onNodeCollapse collapse: " + event.getTreeNode());
        updateSelect(selectedNode.get(selectedTab), event.getTreeNode());
    }

    /**
     * Метод обновляет выделение ветки дерева с помощью JS комманд
     * @param oldNode старая ветка
     * @param newNode новая ветка
     */
    private void updateSelect(TreeNode oldNode, TreeNode newNode) {
        StringBuilder sb = new StringBuilder();
        if (oldNode != null) {
            sb.append("PrimeFaces.widgets.");
            sb.append(selectedTab);
            sb.append("TreeWidget.unselectNode(");
            sb.append("$(\"#navig\\\\:");
            sb.append(selectedTab);
            sb.append("Tree\\\\:");
            sb.append(oldNode.getRowKey());
            sb.append("\"), true);");

            sb.append("PrimeFaces.widgets.");
            sb.append(selectedTab);
            sb.append("TreeWidget.selections = [];");
        }
        sb.append("PrimeFaces.widgets.");
        sb.append(selectedTab);
        sb.append("TreeWidget.selectNode(");
        sb.append("$(\"#navig\\\\:");
        sb.append(selectedTab);
        sb.append("Tree\\\\:");
        sb.append(newNode.getRowKey());
        sb.append("\"));");

        PrimeFaces.current().executeScript(sb.toString());

        updateSelectProgrammaticallyFlag = true;
    }

    private boolean updateSelectProgrammaticallyFlag = false;

    /**
     * Обработчик выделения ветки дерева
     * обрабатывает двойное нажатие
     * @param event ветка дерева
     */
    public void onNodeSelect(NodeSelectEvent event) {
        if ((oldSelectNode.get(selectedTab) != null) && !updateSelectProgrammaticallyFlag
                && (oldSelectNode.get(selectedTab) == selectedNode.get(selectedTab))
                && ((System.currentTimeMillis() - timeOldSelectNode) < 500)) {
            log.info("NavigationMBean.onNodeSelect dblClick");

            if (((TreeNodeModel) selectedNode.get(selectedTab).getData()).isLeaf()) {
                PrimeFaces.current().executeScript("loadData()");

                FaceletContext faceletContext = (FaceletContext) FacesContext.getCurrentInstance()
                        .getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
                PrimeFaces.current().ajax().update((String) faceletContext.getAttribute("updatePath"));

                if (!faceletContext.getAttribute("invokeScript").equals("")) {
                    PrimeFaces.current().executeScript((String) faceletContext.getAttribute("invokeScript"));
                }
            } else {
                if (selectedNode.get(selectedTab).isExpanded()) {
                    setExpandedNode(false, selectedNode.get(selectedTab));
                } else {
                    parentNode.put(selectedTab, ((TreeNodeModel) event.getTreeNode().getData()).getId());
                    addTreeItems(event.getTreeNode());

                    setExpandedNode(true, selectedNode.get(selectedTab));
                }
            }
        } else {
            oldSelectNode.put(selectedTab, selectedNode.get(selectedTab));
            timeOldSelectNode = System.currentTimeMillis();
            updateSelectProgrammaticallyFlag = false;
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
        sb.append("PrimeFaces.widgets.");
        sb.append(selectedTab);
        sb.append("TreeWidget.");
        if (value) {
            sb.append("expandNode(");
        } else {
            sb.append("collapseNode(");
        }
        sb.append("$(\"#navig\\\\:");
        sb.append(selectedTab);
        sb.append("Tree\\\\:");
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
        selectedSearch = searchList.get(0).getObjTypeId();
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
        if (selectedNode.get(selectedTab) == null) {
            selectedNode.put(selectedTab, root.get(selectedTab));
        } else {
            if (((TreeNodeModel) selectedNode.get(selectedTab).getData()).isLeaf()) {
                selectedNode.put(selectedTab, selectedNode.get(selectedTab).getParent());
            }
        }
        nodePath.get(selectedTab).add(selectedNode.get(selectedTab));
        while (selectedNode.get(selectedTab).getParent() != null) {
            selectedNode.put(selectedTab, selectedNode.get(selectedTab).getParent());
            nodePath.get(selectedTab).add(selectedNode.get(selectedTab));
        }
        nodePath.get(selectedTab).remove(nodePath.get(selectedTab).size() - 1);

        log.info("NavigationMBean.reloadAllTree глубина обновления: " + nodePath.get(selectedTab));

        String oldSelectedTab = selectedTab;
        //Чистим дерево и запускаем его перстроение
        for (String item: DEFAULT_TAB_ID) {
            selectedNode.put(item, null);
            root.get(item).getChildren().clear();
            parentNode.put(item, DEFAULT_PARENT_NODE.get(item));
            selectedTab = item;
            addTreeItems(root.get(item));
        }
        selectedTab = oldSelectedTab;
    }

    public void onTabChange(TabChangeEvent e) {
        selectedTab = e.getTab().getId();
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public TreeNode getSelectedNode() {
        return selectedNode.get(selectedTab);
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode.put(selectedTab, selectedNode);
        if (Objects.nonNull(this.selectedNode.get(selectedTab))) {
            selectedNodeId = ((TreeNodeModel) this.selectedNode.get(selectedTab).getData()).getId();
        }
    }

    public TreeNode getRootItem(String name) {
        return root.get(name);
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

    public Object getBeanName() {
        return beanName;
    }

    public String getMethod() {
        return method;
    }
}
