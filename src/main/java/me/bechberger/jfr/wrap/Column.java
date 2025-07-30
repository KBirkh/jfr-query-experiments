package me.bechberger.jfr.wrap;

public class Column {
    private String name;
    private String type;
    private String alias;

    public Column(String name, String type) {
        this.name = name;
        this.type = type;
    }
    public Column(String name, String type, String alias) {
        this.name = name;
        this.type = type;
        this.alias = alias;
    }
    public String getFullName() {
        return alias != null ? alias + "_" + name: name;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getName() {
        return name;
    }
}
