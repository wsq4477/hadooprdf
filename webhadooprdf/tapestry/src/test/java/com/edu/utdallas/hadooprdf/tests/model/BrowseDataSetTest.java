/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.edu.utdallas.hadooprdf.tests.model;

import com.edu.utdallas.hadooprdf.model.DataSetInfo;
import com.edu.utdallas.hadooprdf.pages.BrowseDataSet;
import org.testng.annotations.Test;

/**
 *
 * @author Asif
 */
public class BrowseDataSetTest {

    private BrowseDataSet bset;
    public BrowseDataSetTest() {
        bset=new BrowseDataSet();
    }

    @Test
    public void testGetDataSets(){
        for(DataSetInfo dsinfo:bset.getDataSets()){
            System.out.println("DATA SETS");
            System.out.println(dsinfo.getDataSetName());
            System.out.println();
        }
    }
}
