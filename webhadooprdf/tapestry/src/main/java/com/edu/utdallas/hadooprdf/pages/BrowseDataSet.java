/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.edu.utdallas.hadooprdf.pages;
import com.edu.utdallas.hadooprdf.model.DataSetInfo;
import edu.utdallas.hadooprdf.controller.HadoopRDF;
import edu.utdallas.hadooprdf.controller.HadoopRDFException;
import edu.utdallas.hadooprdf.data.metadata.DataSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tapestry5.annotations.Property;


/**
 *
 * 
 */
public class BrowseDataSet {

   private HadoopRDF hrdf;
   @Property
   private ArrayList<DataSetInfo> dataSets;


    public BrowseDataSet() {
        dataSets=new ArrayList<DataSetInfo>();
     }

    void setupRender() {
		// Get all persons - ask business service to find them (from the database)
		initDataSets();
	}

    /**
     * @return the dataSets
     */
    public ArrayList<DataSetInfo> getDataSets() {
        return dataSets;
    }

    /**
     * @param dataSets the dataSets to set
     */
    public void initDataSets() {
        try {
            hrdf = new HadoopRDF("conf/SemanticWebLabCluster", "/user/farhan/hadooprdf");
            System.out.println("hrdf:"+ hrdf.hashCode());
            Map<String,DataSet> ds=hrdf.getDataSetMap();
            Iterator iterator = ds.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<String,DataSet> entry = (Map.Entry<String,DataSet>)iterator.next();
                dataSets.add(new DataSetInfo(entry.getKey()));
         }

        } catch (HadoopRDFException ex) {
            Logger.getLogger(BrowseDataSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    /**
//     * @return the dataSet
//     */
//    public DataSetInfo getDataSet() {
//        return dataSet;
//    }
//
//    /**
//     * @param dataSet the dataSet to set
//     */
//    public void setDataSet(DataSetInfo dataSet) {
//        this.dataSet = dataSet;
//    }

}
