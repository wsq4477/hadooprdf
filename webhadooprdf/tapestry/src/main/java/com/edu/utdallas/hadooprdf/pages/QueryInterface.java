/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.edu.utdallas.hadooprdf.pages;

import com.edu.utdallas.hadooprdf.model.PredicateInfo;
import com.edu.utdallas.hadooprdf.model.PrefixInfo;
import java.util.ArrayList;

/**
 *
 * 
 */
public class QueryInterface {
    private ArrayList<PrefixInfo> prefixes;
    private String subject;
    private String object;
    private ArrayList<PredicateInfo> predicates;
    private String selectedPredicate;
    private String Query;

    /**
     * @return the prefixes
     */
    public ArrayList<PrefixInfo> getPrefixes() {
        return prefixes;
    }

    /**
     * @param prefixes the prefixes to set
     */
    public void setPrefixes(ArrayList<PrefixInfo> prefixes) {
        this.prefixes = prefixes;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return the Object
     */
    public String getObject() {
        return object;
    }

    /**
     * @param Object the Object to set
     */
    public void setObject(String Object) {
        this.object = Object;
    }

    /**
     * @return the predicates
     */
    public ArrayList<PredicateInfo> getPredicates() {
        return predicates;
    }

    /**
     * @param predicates the predicates to set
     */
    public void setPredicates(ArrayList<PredicateInfo> predicates) {
        this.predicates = predicates;
    }

    /**
     * @return the selectedPredicate
     */
    public String getSelectedPredicate() {
        return selectedPredicate;
    }

    /**
     * @param selectedPredicate the selectedPredicate to set
     */
    public void setSelectedPredicate(String selectedPredicate) {
        this.selectedPredicate = selectedPredicate;
    }

    /**
     * @return the Query
     */
    public String getQuery() {
        return Query;
    }

    /**
     * @param Query the Query to set
     */
    public void setQuery(String Query) {
        this.Query = Query;
    }
}
