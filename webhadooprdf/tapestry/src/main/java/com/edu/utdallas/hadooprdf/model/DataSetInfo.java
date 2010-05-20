/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.edu.utdallas.hadooprdf.model;

/**
 *
 * @author hadoop
 */
public class DataSetInfo {
    
    private String dataSetName;

    public DataSetInfo(String key) {
       dataSetName=key;
    }

    /**
     * @return the dataSetName
     */
    public String getDataSetName() {
        return dataSetName;
    }

    /**
     * @param dataSetName the dataSetName to set
     */
    public void setDataSetName(String dataSetName) {
        this.dataSetName = dataSetName;
    }

}
