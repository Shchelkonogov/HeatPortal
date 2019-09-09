package ru.tn.managedBean;

import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScaleLabel;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;
import org.primefaces.model.charts.optionconfig.title.Title;
import ru.tn.model.archiveData.*;
import ru.tn.sessionBean.ArchiveData.ArchiveDataSBean;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ManagedBean(name = "archiveData")
@ViewScoped
public class ArchiveDataMBean implements Serializable {

    private static final Logger LOG = Logger.getLogger(ArchiveDataMBean.class.getName());

    private static final int COLUMN_SIZE = 31;

    @EJB
    private ArchiveDataSBean bean;

    private Date date = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
    private LocalDateTime startHeadDate;

    private int object = 0;

    private String mySelectedColumnField;
    private String oldSelectedColumn = "";

    private String mnemoUrl;

    private List<ColumnModel> columns = new ArrayList<>();
    private List<HeaderWrapper> headerWrapper = new ArrayList<>();
    private List<DataModel> gridData = new ArrayList<>();
    private List<DataModel> selectedRows = new ArrayList<>();

    private int colSpan = 0;

    private String headerName;

    private boolean mapStatus = false;

    private Set<String> techProcFilter = new TreeSet<>();
    private Set<ParamType> paramTypeFilter = new TreeSet<>();

    private String dateType = "Hour";

    //Имена для заголовка панели
    private String tabName = "Архивные данные";

    //Модели для графиков
    private List<LineChartModel> charts = new ArrayList<>();
    //Набор цветов для графиков
    private List<String> colors = new ArrayList<>(Arrays.asList("rgb(75, 192, 192)", "#EF161E", "#2DBE2C",
            "#0078BE", "#DE64A1", "#8D5B2D", "#ED9121", "#800080", "#FFD702", "#99CC00", "#9999FF"));

    @PostConstruct
    private void init() {
        mnemoUrl = bean.getMnemoUrl();
        startHeadDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
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

        updateHeader();
    }

    /**
     * Метод обновляет данные для шапки таблицы
     */
    private void updateHeader() {
        columns.clear();
        headerWrapper.clear();
        for (int i = 0; i < colSpan; i++) {
            String date = startHeadDate.plusHours(i).toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            if (headerWrapper.contains(new HeaderWrapper(date))) {
                headerWrapper.get(headerWrapper.indexOf(new HeaderWrapper(date))).incrementColumnCount();
            } else {
                headerWrapper.add(new HeaderWrapper(date));
            }
            columns.add(new ColumnModel(startHeadDate.plusHours(i).getHour() + "ч", "data." + i, "data." + i + ".color"));
        }
    }

    /**
     * Метод который запускает начальную загрузку формы с данными
     * запускается с дерева навигации
     * @param value id объекта
     */
    public void showName(String value) {
        if (!value.equals("")) {
            setDate(Date.from(startHeadDate.truncatedTo(ChronoUnit.DAYS).atZone(ZoneId.systemDefault()).toInstant()));
            updateHeader();

            headerName = "по объекту: " + bean.getObjectPath(value);

            object = Integer.parseInt(value.substring(1));

            selectedRows.clear();
            loadData();
            selectColumn(0);

            mapStatus = false;
        }
    }

    public void onDateSelect(SelectEvent event) {
        LOG.info("ArchiveDataMBean.onDateSelect select date " + event.getObject());
        updateHeader();
        if (object != 0) {
            loadData();
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
                data.setResult(data.getData()[Integer.parseInt(mySelectedColumnField)].getResult());
            }

            oldSelectedColumn = mySelectedColumnField;
        }
    }

    private void selectColumn(int index) {
        for (DataModel data: gridData) {
            if ((data.getData()[index] != null) && data.getData()[index].getColor().equals("none")) {
                data.getData()[index].setColor("blue");
            }
            data.setMin(data.getData()[0].getMin());
            data.setMax(data.getData()[0].getMax());
            data.setResult(data.getData()[0].getResult());
        }
        oldSelectedColumn = String.valueOf(index);
    }

    /**
     * Метод срабатывает при изменение закладки
     * @param event событие {@link TabChangeEvent}
     */
    public void onTabChange(TabChangeEvent event) {
        if (!mapStatus && event.getTab().getId().equals("tab3")) {
            LOG.info("ArchiveDataMBean.onTabChange select tab3 (map)");
            mapStatus = true;
            PrimeFaces.current().executeScript("initMap('Москва " + bean.getAddress(object) + "')");
        }

        if (event.getTab().getId().equals("tab4")) {
            LOG.info("ArchiveDataMBean.onTabChange select tab4 (charts)");
            initGraph();
        } else {
            if (!charts.isEmpty()) {
                LOG.info("ArchiveDataMBean.onTabChange clear tab4 (charts) data");
                charts = new ArrayList<>();
                PrimeFaces.current().ajax().update("tabView:charts");
            }
        }

        //Изменение заголовка панели данных при смене закладки
        switch (event.getTab().getId()) {
            case "tab3": {
                tabName = "Карта";
                break;
            }
            case "tab2": {
                tabName = "Мнемосхема";
                break;
            }
            case "tab4": {
                tabName = "Графики";
                break;
            }
            default: tabName = "Архивные данные";
        }
        PrimeFaces.current().executeScript("updatePanelHeader()");
    }

    /**
     * Метод реализует инициализацию графиков по выделенным строкам в таблице
     */
    private void initGraph() {
        if (!selectedRows.isEmpty()) {
            charts = new ArrayList<>();

            //Разбиваем выбранные данные на группы по одинаковому paramTypeId
            Map<Integer, List<DataModel>> chartsMap = new HashMap<>();
            for (DataModel row: selectedRows) {
                if (chartsMap.containsKey(row.getParamTypeId())) {
                    chartsMap.get(row.getParamTypeId()).add(row);
                } else {
                    chartsMap.put(row.getParamTypeId(), new ArrayList<>(Collections.singletonList(row)));
                }
            }

            int index;
            for (Map.Entry<Integer, List<DataModel>> entry: chartsMap.entrySet()) {
                LineChartModel chartModel = new LineChartModel();
                ChartData chartData = new ChartData();

                //Размещаем на одном контейнере графика графики одного типа
                index = 0;
                for (DataModel row: entry.getValue()) {
                    LineChartDataSet chartDataSet = new LineChartDataSet();

                    //Задаем знаечние графика
                    //Приводим данные в числовой тип и убираем оттуда все лишнее
                    List<Number> values = Arrays.stream(row.getData())
                            .map(e -> {
                                if (e == null || e.getValue() == null) {
                                    return null;
                                } else {
                                    try {
                                        return new BigDecimal(e.getValue()).doubleValue();
                                    } catch (NumberFormatException ex) {
                                        return null;
                                    }
                                }
                            })
                            .collect(Collectors.toList());
                    values = values.subList(0, colSpan);

                    chartDataSet.setData(values);
                    chartDataSet.setFill(false); //Позволяет делать график разрывным если есть значение null
                    chartDataSet.setLabel(row.getName());
                    chartDataSet.setBorderColor(colors.get(index));

                    chartData.addChartDataSet(chartDataSet);

                    //Задаем значения графика для min
                    List<Number> valuesMin = Arrays.stream(row.getData())
                            .map(e -> {
                                if (e == null || e.getMin() == null) {
                                    return null;
                                } else {
                                    try {
                                        return new BigDecimal(e.getMin()).doubleValue();
                                    } catch (NumberFormatException ex) {
                                        return null;
                                    }
                                }
                            })
                            .collect(Collectors.toList());
                    valuesMin = valuesMin.subList(0, colSpan);
                    //Если есть хотя бы одно значение
                    if (valuesMin.stream().anyMatch(Objects::nonNull)) {
                        LineChartDataSet chartDataSetMin = new LineChartDataSet();

                        chartDataSetMin.setData(valuesMin);
                        chartDataSetMin.setFill(false);
                        chartDataSetMin.setLabel(row.getName() + " Min");
                        chartDataSetMin.setBorderColor(colors.get(index));
                        chartDataSetMin.setBorderDash(Arrays.asList(5, 5)); //Делаем линию пунктирной

                        chartData.addChartDataSet(chartDataSetMin);
                    }

                    //Задаем значения графика для max
                    List<Number> valuesMax = Arrays.stream(row.getData())
                            .map(e -> {
                                if (e == null || e.getMax() == null) {
                                    return null;
                                } else {
                                    try {
                                        return new BigDecimal(e.getMax()).doubleValue();
                                    } catch (NumberFormatException ex) {
                                        return null;
                                    }
                                }
                            })
                            .collect(Collectors.toList());
                    valuesMax = valuesMax.subList(0, colSpan);
                    if (valuesMax.stream().anyMatch(Objects::nonNull)) {
                        LineChartDataSet chartDataSetMax = new LineChartDataSet();

                        chartDataSetMax.setData(valuesMax);
                        chartDataSetMax.setLabel(row.getName() + " Max");
                        chartDataSetMax.setFill(false);
                        chartDataSetMax.setBorderColor(colors.get(index));
                        chartDataSetMax.setBorderDash(Arrays.asList(5, 5));

                        chartData.addChartDataSet(chartDataSetMax);
                    }

                    //Индекс для выбора цвета
                    index++;
                    if (index == colors.size()) {
                        index = 0;
                    }
                }

                //Задаем массив значений для оси x
                List<String> labels = new ArrayList<>();
                switch (dateType) {
                    case "Hour": {
                        for (int i = 0; i < colSpan; i++) {
                            labels.add(startHeadDate.plusHours(i).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH")) + ":00:00");
                        }
                        break;
                    }
                }
                chartData.setLabels(labels);

                LineChartOptions options = new LineChartOptions();

                //Опция подписи для контейнера графика
                Title title = new Title();
                title.setDisplay(true);
                title.setText(entry.getValue().get(0).getParamTypeName()); //Текст подписи контейнера графика

                CartesianScales scales = new CartesianScales();
                CartesianLinearAxes linearAxes = new CartesianLinearAxes();

                //Опция подписи для оси y
                CartesianScaleLabel scaleLabel = new CartesianScaleLabel();
                scaleLabel.setDisplay(true);
                scaleLabel.setLabelString(entry.getValue().get(0).getSi()); //Текст подписи оси y

                linearAxes.setScaleLabel(scaleLabel);
                scales.addYAxesData(linearAxes);

                options.setTitle(title);
                options.setScales(scales);

                chartModel.setOptions(options);
                chartModel.setData(chartData);
                charts.add(chartModel);
            }

            PrimeFaces.current().ajax().update("tabView:charts");
        }
    }

    /**
     * Метод обрабатывает движение мышкой по таблице
     * Смещение дынных вправо или влево
     */
    public void moveTableData() {
        String direction = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("direction");

        LocalDateTime lDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        switch (direction) {
            case "left": {
                setDate(Date.from(lDate.plusHours(1).atZone(ZoneId.systemDefault()).toInstant()));
                break;
            }
            case "right": {
                setDate(Date.from(lDate.minusHours(1).atZone(ZoneId.systemDefault()).toInstant()));
                break;
            }
        }

        updateHeader();

        if (object != 0) {
            loadData();
            selectColumn(0);
        }
    }

    /**
     * Метод формирует данные для таблицы из данных из бина.
     * Копирует данные из бина, что бы не изменялись они в кешах.
     * Формирует новые данные для отображения.
     */
    private void loadData() {
        techProcFilter.clear();
        paramTypeFilter.clear();
        List<DataModel> model;
        gridData = new ArrayList<>();
        LocalDate tempDate;
        LocalDateTime tempTime = LocalDateTime.from(startHeadDate);
        int index  = 0;
        for (int i = 0; i < headerWrapper.size(); i++) {
            model = bean.loadData(object, headerWrapper.get(i).getName());
            if (model == null) {
                continue;
            }

            if (i == 0) {
                for (int j = 0; j < model.size(); j++) {
                    gridData.add(new DataModel(model.get(j).getParamId(), model.get(j).getStatAgr(),
                            model.get(j).isAnalog(), model.get(j).getName(),
                            model.get(j).getTechProc(), model.get(j).getSi(),
                            model.get(j).getCalculateType(), model.get(j).getParamTypeId(),
                            model.get(j).getParamTypeName()));
                    gridData.get(j).setMin(model.get(j).getMin());
                    gridData.get(j).setMax(model.get(j).getMax());
                    gridData.get(j).setResult(model.get(j).getResult());
                    gridData.get(j).setData(new DataValueModel[COLUMN_SIZE]);

                    techProcFilter.add(gridData.get(j).getTechProc());
                    paramTypeFilter.add(new ParamType(gridData.get(j).getParamTypeId(), gridData.get(j).getParamTypeName()));
                }
            }

            tempDate = LocalDate.parse(headerWrapper.get(i).getName(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            do {
                for (int j = 0; j < gridData.size(); j++) {
                    gridData.get(j).getData()[index] = new DataValueModel(model.get(j).getData()[tempTime.getHour()].getValue(),
                            model.get(j).getData()[tempTime.getHour()].getColor());
                    gridData.get(j).getData()[index].setMin(model.get(j).getData()[tempTime.getHour()].getMin());
                    gridData.get(j).getData()[index].setMax(model.get(j).getData()[tempTime.getHour()].getMax());
                    gridData.get(j).getData()[index].setResult(model.get(j).getData()[tempTime.getHour()].getResult());
                }
                tempTime = tempTime.plusHours(1);
                index++;
            } while (tempDate.isEqual(tempTime.toLocalDate()) && index < COLUMN_SIZE);
        }

        PrimeFaces.current().executeScript("PF('dataGridWidget').filter()");
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
        startHeadDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
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

    public List<HeaderWrapper> getHeaderWrapper() {
        return headerWrapper;
    }

    public Set<String> getTechProcFilter() {
        return techProcFilter;
    }

    public Set<ParamType> getParamTypeFilter() {
        return paramTypeFilter;
    }

    public String getDateType() {
        return dateType;
    }

    public void setDateType(String dateType) {
        this.dateType = dateType;
    }

    public List<LineChartModel> getCharts() {
        return charts;
    }

    public String getTabName() {
        return tabName;
    }
}
