/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.edu.utdallas.hadooprdf.model;

/**
 *
 * @author hadoop
 */
public  class QueryBuilder {
    private static String Query;

     public static void BuildQuery(String subject,String predicate,String object) {
        setQuery(getQuery() + subject + " " + predicate + " " + object + "\n");
    }

    /**
     * @return the Query
     */
    public static String getQuery() {
        return Query;
    }

    /**
     * @param aQuery the Query to set
     */
    protected static void setQuery(String aQuery) {
        Query = aQuery;
    }

    /**
     * to set it to null
     */
    public void setEmpty(){
        Query="";
    }
    
    protected QueryBuilder(){
        //to avoid instances
    }

}
