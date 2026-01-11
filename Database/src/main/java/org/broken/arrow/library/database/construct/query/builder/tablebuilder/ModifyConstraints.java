package org.broken.arrow.library.database.construct.query.builder.tablebuilder;

public class ModifyConstraints {

    private String dropPrimaryKey;
    private String addPrimaryKey;
    private String addUnique;

    public void dropPrimaryKey() {
        this.dropPrimaryKey = "DROP PRIMARY KEY";
    }

    public void addPrimaryKey(final String... columns) {
        this.addPrimaryKey = "ADD PRIMARY KEY (" + String.join(", ", columns) + ")";
    }

    public void addUnique(final String...  columns) {
        this.addUnique = "ADD UNIQUE (" + String.join(", ", columns) +")";
    }

    public String getDropPrimaryKey() {
        return dropPrimaryKey;
    }

    public String getAddPrimaryKey() {
        return addPrimaryKey;
    }

    public String getAddUnique() {
        return addUnique;
    }
}
