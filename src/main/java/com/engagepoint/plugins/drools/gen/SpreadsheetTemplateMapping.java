package com.engagepoint.plugins.drools.gen;

import java.util.List;

public class SpreadsheetTemplateMapping {

    private String spreadsheetPath;
    private String templatesPath;
    private List<TemplateMappingItem> items;

    public String getSpreadsheetPath() {
        return spreadsheetPath;
    }

    public void setSpreadsheetPath(String spreadsheetPath) {
        this.spreadsheetPath = spreadsheetPath;
    }

    public String getTemplatesPath() {
        return templatesPath;
    }

    public void setTemplatesPath(String templatesPath) {
        this.templatesPath = templatesPath;
    }

    public List<TemplateMappingItem> getItems() {
        return items;
    }

    public void setItems(List<TemplateMappingItem> items) {
        this.items = items;
    }

    public static class TemplateMappingItem {

        private String worksheetName;
        private String drtFileName;
        private String agendaGroupName;
        private String drlFileName;
        private int row = 3;
        private int column = 1;

        public String getWorksheetName() {
            return worksheetName;
        }

        public void setWorksheetName(String worksheetName) {
            this.worksheetName = worksheetName;
        }

        public String getDrtFileName() {
            return drtFileName;
        }

        public void setDrtFileName(String drtFileName) {
            this.drtFileName = drtFileName;
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }

        public String getAgendaGroupName() {
            return agendaGroupName;
        }

        public void setAgendaGroupName(String agendaGroupName) {
            this.agendaGroupName = agendaGroupName;
        }

        public String getEscapedAgendaGroupName() {
            return "\"" + agendaGroupName + "\"";
        }

        public String getDrlFileName() {
            return drlFileName;
        }

        public String getDrlFileName(boolean generateIfNull) {
            if (drlFileName == null && generateIfNull) {
                String result = drtFileName.replaceAll("\\.", "_");
                return result.replaceAll("_drt", ".drl");
            }
            return drlFileName;
        }

        public void setDrlFileName(String drlFileName) {
            this.drlFileName = drlFileName;
        }

        @Override
        public String toString() {
            return "TemplateMappingItem{" +
                    "worksheetName = '" + worksheetName + '\'' +
                    ", drtFileName = '" + drtFileName + '\'' +
                    ", start row = " + row +
                    ", start column = " + column +
                    ", agenda group = " + agendaGroupName +
                    '}';
        }
    }
}
