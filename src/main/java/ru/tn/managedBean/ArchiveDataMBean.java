package ru.tn.managedBean;

import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import ru.tn.model.archiveData.ColumnModel;
import ru.tn.model.archiveData.DataModel;
import ru.tn.sessionBean.ArchiveData.ArchiveDataSBean;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@ManagedBean(name = "archiveData")
@ViewScoped
public class ArchiveDataMBean implements Serializable {

    private Date date = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    private int object;

    @Resource(name = "mnemoUrl")
    private String mnemoUrl;

    @EJB
    private ArchiveDataSBean bean;

    private static final List<ColumnModel> COLUMN_KEYS = Arrays.asList(
            new ColumnModel("Обозн.", "name", "name"), new ColumnModel("Тех. пр.", "techProc", "techProc"),
            new ColumnModel("Е.И.", "si", "si"), new ColumnModel("Min", "min", "min"),
            new ColumnModel("Max", "max", "max"), new ColumnModel("Итоги", "result", "result"));

    private List<ColumnModel> columns = new ArrayList<>();
    private List<DataModel> gridData = new ArrayList<>();

    private String headerName;

    @PostConstruct
    private void init() {
        columns.addAll(COLUMN_KEYS);
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
        System.out.println(event.getObject() + " " + date);

        gridData = bean.loadData(object, sdf.format(date));
    }

    private boolean mapStatus = false;

    public void onTabChange(TabChangeEvent event) {
        if (!mapStatus && event.getTab().getId().equals("tab3")) {
            mapStatus = true;
            System.out.println("asdasdad");
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
}
