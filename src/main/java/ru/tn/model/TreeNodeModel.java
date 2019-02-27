package ru.tn.model;

import java.io.Serializable;

public class TreeNodeModel implements Serializable, Comparable<Object> {

    private String name, id, objectType;
    private boolean leaf = true;

    public TreeNodeModel(String name) {
        this(name, name, name);
    }

    public TreeNodeModel(String name, String id, String objectType) {
        this.name = name;
        this.id = id;
        this.objectType = objectType;

        if (objectType.matches("[S]\\d*")) {
            leaf = false;
        }
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getObjectType() {
        return objectType;
    }

    public boolean isLeaf() {
        return leaf;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Object o) {
        TreeNodeModel item = (TreeNodeModel) o;
        if (!this.leaf && item.leaf) {
            return -1;
        } else {
            if (this.leaf && !item.leaf) {
                return 1;
            }
        }
        return this.name.compareTo(item.name);
    }
}
