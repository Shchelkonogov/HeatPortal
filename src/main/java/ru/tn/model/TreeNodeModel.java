package ru.tn.model;

import java.io.Serializable;

public class TreeNodeModel implements Serializable {

    private String name, id;
    private boolean leaf = false;

    public TreeNodeModel(String name) {
        this(name, name, name);
    }

    public TreeNodeModel(String name, String id, String objectType) {
        this.name = name;
        this.id = id;

        //Если тип начинается на "L" или "H", то это леписток дерева
        if (objectType.matches("[LH]\\d*")) {
            leaf = true;
        }
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public boolean isLeaf() {
        return leaf;
    }

    @Override
    public String toString() {
        return name;
    }
}
