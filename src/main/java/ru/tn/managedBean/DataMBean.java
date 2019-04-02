package ru.tn.managedBean;

import ru.tn.model.ArchiveGridColumnM;
import ru.tn.model.ArchiveGridDataM;
import ru.tn.sessionBean.ArchiveDataSBean;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ManagedBean(name = "data")
@ViewScoped
public class DataMBean implements Serializable {

    @EJB
    private ArchiveDataSBean bean;

    private static final List<ArchiveGridColumnM> COLUMN_KEYS = Arrays.asList(
            new ArchiveGridColumnM("Обозн.", "name"), new ArchiveGridColumnM("Тех. пр.", "techProc"),
            new ArchiveGridColumnM("Е.И.", "si"), new ArchiveGridColumnM("Min", "min"),
            new ArchiveGridColumnM("Max", "max"), new ArchiveGridColumnM("Итоги", "result"));

    private List<ArchiveGridColumnM> columns = new ArrayList<>();
    private List<ArchiveGridDataM> gridData = new ArrayList<>();

    private String headerName;

    @PostConstruct
    private void init() {
        columns = COLUMN_KEYS;
    }

    public void showName(String value) {
        if (!value.equals("")) {
            headerName = "по объекту: " + bean.getObjectPath(value);

            gridData = bean.loadData(Integer.parseInt(value.substring(1)));
        }
    }

    public List<ArchiveGridColumnM> getColumns() {
        return columns;
    }

    public void setColumns(List<ArchiveGridColumnM> columns) {
        this.columns = columns;
    }

    public List<ArchiveGridDataM> getGridData() {
        return gridData;
    }

    public void setGridData(List<ArchiveGridDataM> gridData) {
        this.gridData = gridData;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }
}
