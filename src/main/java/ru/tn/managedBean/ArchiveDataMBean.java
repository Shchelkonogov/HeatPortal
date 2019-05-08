package ru.tn.managedBean;

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

    private static final Logger LOG = Logger.getLogger(ArchiveDataMBean.class.getName());

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    @EJB
    private ArchiveDataSBean bean;

    private Date date = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

    private int object;

    private String mySelectedColumnField;
    private String oldSelectedColumn = "";

    private String mnemoUrl;

    private List<ColumnModel> columns = new ArrayList<>();
    private List<DataModel> gridData = new ArrayList<>();
    private List<DataModel> selectedRows = new ArrayList<>();

    private String headerName;

    private boolean mapStatus = false;

    @PostConstruct
    private void init() {
        mnemoUrl = bean.getMnemoUrl();

        for (int i = 0; i < 24; i++) {
            columns.add(new ColumnModel((i + 1) + "ч", "data." + i, "data." + i + ".color"));
        }
    }

    public void showName(String value) {
        if (!value.equals("")) {
            headerName = "по объекту: " + bean.getObjectPath(value);

            object = Integer.parseInt(value.substring(1));

            gridData = bean.loadData(object, sdf.format(date));

            mapStatus = false;
        }
    }

    public void onDateSelect(SelectEvent event) {
        LOG.info("ArchiveDataMBean.onDateSelect select date " + event.getObject());
        gridData = bean.loadData(object, sdf.format(date));
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
            }
            oldSelectedColumn = mySelectedColumnField;
        }
    }

    public void onTabChange(TabChangeEvent event) {
        if (!mapStatus && event.getTab().getId().equals("tab3")) {
            mapStatus = true;
            LOG.info("ArchiveDataMBean.onTabChange select tab3");
            PrimeFaces.current().executeScript("initMap('Москва " + bean.getAddress(object) + "')");
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
}
