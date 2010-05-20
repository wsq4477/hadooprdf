package com.edu.utdallas.hadooprdf.pages;

import java.util.Date;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.OnEvent;


/**
 * Start page of application webhadooprdf.
 */
public class Index
{
        private String Username;
        private String Password;
        @InjectPage
        private BrowseDataSet datasetpage;

        public Date getCurrentTime()
	{ 
		return new Date(); 
	}

        public String getUsername(){
            return Username;
        }
        public void setUsername(String uname){
            Username=uname;
        }
        public String getPassword(){
            return Password;
        }
        public void setPassword(String pwd){
            Password=pwd;
        }

        @OnEvent(value="submit", component="userInputForm")
        Object onFormSubmit()
        {
          System.out.println("Handling form submission!");
          //datasetpage.setPassedMessage(message);
          return datasetpage;
        }
}
