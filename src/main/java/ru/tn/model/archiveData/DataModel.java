package ru.tn.model.archiveData;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

public class DataModel implements Serializable {

    private int paramId;
    private int statAgr;

    private int paramTypeId;
    private String paramTypeName;

    private boolean analog;
    private String calculateType;

    private String name;
    private String techProc;
    private String si;
    private String min = "-";
    private String max = "-";
    private String result = "-";
    private DataValueModel[] data;

    public DataModel(boolean analog, String calculateType, String name, String techProc, String si, int paramTypeId,
                     String paramTypeName) {
        this.analog = analog;
        this.calculateType = calculateType;
        this.name = name;
        this.techProc = techProc;
        this.si = si;
        this.paramTypeId = paramTypeId;
        this.paramTypeName = paramTypeName;
    }

    public DataModel(int paramId, int statAgr, boolean analog, String name, String techProc,
                     String si, String calculateType, int paramTypeId, String paramTypeName) {
        this.paramId = paramId;
        this.statAgr = statAgr;
        this.analog = analog;
        this.name = name;
        this.techProc = techProc;
        this.si = si;
        this.calculateType = calculateType;
        this.paramTypeId = paramTypeId;
        this.paramTypeName = paramTypeName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTechProc() {
        return techProc;
    }

    public void setTechProc(String techProc) {
        this.techProc = techProc;
    }

    public String getSi() {
        return si;
    }

    public void setSi(String si) {
        this.si = si;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public DataValueModel[] getData() {
        return data;
    }

    public void setData(DataValueModel[] data) {
        this.data = data;
    }

    public int getParamId() {
        return paramId;
    }

    public int getStatAgr() {
        return statAgr;
    }

    public boolean isAnalog() {
        return analog;
    }

    public int getParamTypeId() {
        return paramTypeId;
    }

    public String getCalculateType() {
        return calculateType;
    }

    public String getParamTypeName() {
        return paramTypeName;
    }

    public void calcResult() {
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal tempValue;
        int count = 0;

        for (DataValueModel datum: data) {
            if ((datum != null) && (datum.getValue() != null)) {
                if (analog) {
                    try {
                        tempValue = new BigDecimal(datum.getValue().replaceAll(",", "."));
                        sum = sum.add(tempValue);
                        count++;
                        datum.setValue(tempValue.setScale(2, RoundingMode.HALF_EVEN)
                                .stripTrailingZeros().toPlainString());
                    } catch (NumberFormatException e) {
                    }
                } else {
                    if (datum.getValue().startsWith("~")) {
                        sum = sum.add(new BigDecimal(datum.getValue().substring(1)));
                        count++;
                    }
                }
            }
        }

        if (analog) {
            if (count != 0) {
                switch (calculateType) {
                    case "D": {
                        result = sum.divide(new BigDecimal(String.valueOf(count)), 2, RoundingMode.HALF_EVEN)
                                .stripTrailingZeros().toPlainString();
                        break;
                    }
                    case "I": {
                        result = sum.setScale(2, RoundingMode.HALF_EVEN)
                                .stripTrailingZeros().toPlainString();
                        break;
                    }
                }
            }
        } else {
            if (count != 0) {
                result = "~" + sum.toPlainString();
            } else {
                if ((data[0] != null) && (data[0].getValue() != null)) {
                    result = data[0].getValue();
                }
            }
        }

        for (DataValueModel el: data) {
            el.setResult(result);
        }
    }

    @Override
    public String toString() {
        return "DataModel{" + "paramId=" + paramId +
                ", statAgr=" + statAgr +
                ", analog=" + analog +
                ", name='" + name + '\'' +
                ", techProc='" + techProc + '\'' +
                ", si='" + si + '\'' +
                ", min='" + min + '\'' +
                ", max='" + max + '\'' +
                ", result='" + result + '\'' +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
