/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.utdallas.webhadooprdf.stripes.action;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

/**
 *
 * @author Asif Mohammed
 */
public class AddDatasetActionBean extends BaseActionBean{
    private  static final String view="/WEB-INF/jsp/add_dataset.jsp";

    @DefaultHandler
    public Resolution defaultView(){
       return new ForwardResolution(view);
    }
}
