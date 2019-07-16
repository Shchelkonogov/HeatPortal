package ru.tn.managedBean;

import com.sun.xml.bind.v2.TODO;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import ru.tn.model.archiveData.ColumnModel;
import ru.tn.model.archiveData.DataModel;
import ru.tn.sessionBean.ArchiveData.ArchiveDataSBean;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@ManagedBean(name = "archiveData")
@ViewScoped
public class ArchiveDataMBean implements Serializable {

    // TODO Следущий этап с таблицей это реализовать подгрузку соседних данных в других потоках
    //  а затем реализовать обработку смещения данных влево вправо на 1 час на не на сутки
    //  основываясь на подгруженной информации
    //  можно еще вначале сделать смещение шапки на 1 час

    private static final Logger LOG = Logger.getLogger(ArchiveDataMBean.class.getName());

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    @EJB
    private ArchiveDataSBean bean;

    private Date date = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

    private int object = 0;

    private String mySelectedColumnField;
    private String oldSelectedColumn = "";

    private String mnemoUrl;

    private List<ColumnModel> columns = new ArrayList<>();
    private List<DataModel> gridData = new ArrayList<>();
    private List<DataModel> selectedRows = new ArrayList<>();

    private int colSpan = 0;

    private String headerName;

    private boolean mapStatus = false;

    @PostConstruct
    private void init() {
        mnemoUrl = bean.getMnemoUrl();
    }

    /**
     * Метод инициализирует шапку
     */
    public void initTable() {
        String count = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("count");

        switch (count) {
            case "min": {
                colSpan = 12;
                break;
            }
            case "max": {
                colSpan = 24;
                break;
            }
        }
        columns.clear();
        for (int i = 0; i < colSpan; i++) {
            columns.add(new ColumnModel((i + 1) + "ч", "data." + i, "data." + i + ".color"));
        }
    }

    /**
     * Метод который запускает начальную загрузку формы с данными
     * запускается с дерева навигации
     * @param value id объекта
     */
    public void showName(String value) {
        if (!value.equals("")) {
            headerName = "по объекту: " + bean.getObjectPath(value);

            object = Integer.parseInt(value.substring(1));

            selectedRows.clear();
            gridData = bean.loadData(object, sdf.format(date));
            selectColumn(0);

            mapStatus = false;
        }
    }

    public void onDateSelect(SelectEvent event) {
        LOG.info("ArchiveDataMBean.onDateSelect select date " + event.getObject());
        if (object != 0) {
            gridData = bean.loadData(object, sdf.format(date));
            selectColumn(0);
        }
    }

    public void updateSelectedRows() {
        LOG.info("ArchiveDataMBean.updateSelectedRows click column " + mySelectedColumnField);
        if (!oldSelectedColumn.equals(mySelectedColumnField)) {
            for (DataModel data: gridData) {
                if (!oldSelectedColumn.equals("")) {
                    if (data.getData()[Integer.parseInt(oldSelectedColumn)].getColor().equals("blue")) {
                        data.getData()[Integer.parseInt(oldSelectedColumn)].setColor("none");
                    }
                }
                if (data.getData()[Integer.parseInt(mySelectedColumnField)].getColor().equals("none")) {
                    data.getData()[Integer.parseInt(mySelectedColumnField)].setColor("blue");
                }
                data.setMin(data.getData()[Integer.parseInt(mySelectedColumnField)].getMin());
                data.setMax(data.getData()[Integer.parseInt(mySelectedColumnField)].getMax());
            }

            oldSelectedColumn = mySelectedColumnField;
        }
    }

    private void selectColumn(int index) {
        for (DataModel data: gridData) {
            if ((data.getData()[index] != null) && data.getData()[index].getColor().equals("none")) {
                data.getData()[index].setColor("blue");
            }
        }
        oldSelectedColumn = String.valueOf(index);
    }

    public void onTabChange(TabChangeEvent event) {
        if (!mapStatus && event.getTab().getId().equals("tab3")) {
            mapStatus = true;
            LOG.info("ArchiveDataMBean.onTabChange select tab3");
            PrimeFaces.current().executeScript("initMap('Москва " + bean.getAddress(object) + "')");
        }
    }

    /**
     * Метод обрабатывает движение мышкой по таблице
     * Смещение дынных вправо или влево
     */
    public void moveTableData() {
        String direction = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("direction");

        LocalDate lDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        switch (direction) {
            case "left": {
                date = Date.from(lDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                break;
            }
            case "right": {
                date = Date.from(lDate.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                break;
            }
        }

        if (object != 0) {
            gridData = bean.loadData(object, sdf.format(date));
            selectColumn(0);
        }
    }

    public List<ColumnModel> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnModel> columns) {
        this.columns = columns;
    }

    public List<DataModel> getGridData() {
        return gridData;
    }

    public void setGridData(List<DataModel> gridData) {
        this.gridData = gridData;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMnemoUrl() {
        return mnemoUrl;
    }

    public int getObject() {
        return object;
    }

    public String getMySelectedColumnField() {
        return mySelectedColumnField;
    }

    public void setMySelectedColumnField(String mySelectedColumnField) {
        this.mySelectedColumnField = mySelectedColumnField;
    }

    public List<DataModel> getSelectedRows() {
        return selectedRows;
    }

    public void setSelectedRows(List<DataModel> selectedRows) {
        this.selectedRows = selectedRows;
    }

    public int getColSpan() {
        return colSpan;
    }
}
