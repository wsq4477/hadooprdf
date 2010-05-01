package edu.utdallas.hadooprdf.jobrunner;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * A class that implements a generic map-reduce job runner given a query plan
 * @author vaibhav
 *
 */
public class GenericJobRunner 
{
	private String joinVariable = "subject";
	private int numOfVariables = 0;
	
	/**
	 * The generic mapper class for a SPARQL query
	 * @author vaibhav
	 *
	 */
	public class GenericMapper extends Mapper<Text, Text, Text, Text>
	{
		/**
		 * The setup method for this mapper
		 * @param context - the context 
		 */
		protected void setup( Context context )
		{
			
		}
		
		/**
		 * The cleanup method for this mapper
		 * @param context - the context
		 */
		protected void cleanup( Context context )
		{
			
		}
		
		/**
		 * The map method
		 * @param key - the input key
		 * @param value - the input value
		 * @param context - the context
		 */
		protected void map( Text key, Text value, Context context ) throws IOException, InterruptedException
		{
			//Tokenize the value
			StringTokenizer st = new StringTokenizer( value.toString(), "\t" ); 
			
			//First check if a key exists, if it does we are looking at a file
			//Else this maybe a second job
			String sPredicate = key.toString();
			if( sPredicate != null )
			{
				//Get the subject
				String sSubject = st.nextToken();
				
				//If file is of a standard predicate such as rdf, rdfs etc, we need to output only the subject since this is all the file contains
				//Else depending on the number of variables in the triple pattern we output subject-subject, subject-object, object-subject or object-object 
				if( sPredicate.contains( "type" ) )
				{
					//TODO: Don't know if there is a use to append the subject after the prefix created
					context.write( new Text( sSubject ), new Text( sPredicate.substring( 5, 6 ) + "#" + sSubject ) );
				}
				else
				{
					//TODO: How to generate a unique prefix ??
					//TODO: How to get the join variable and the number of variables in a triple pattern
					//If join is on subject and the number of variables in the triple pattern is 2 output ( subject, object )
					if( joinVariable.equalsIgnoreCase( "subject" ) )
					{
						if( numOfVariables == 2 )
							context.write( new Text( sSubject ), new Text( sPredicate.substring( 5, 6 ) + "#" + st.nextToken() ) );
						else
							context.write( new Text( sSubject ), new Text( sPredicate.substring( 5, 6 ) + "#" + sSubject ) );
					}
					else
						if( joinVariable.equalsIgnoreCase( "object" ) )
						{
							if( numOfVariables == 2 )
								context.write( new Text( st.nextToken() ), new Text( sPredicate.substring( 5, 6 ) + "#" + sSubject ) );
							else
							{
								String sObject = st.nextToken();
								context.write( new Text( sObject ), new Text( sPredicate.substring( 5, 6 ) + "#" + sObject ) );
							}
						}
				}
			}
			else
			{
				
			}
		}
	}

	/**
	 * The generic reducer class for a SPARQL query
	 * @author vaibhav
	 *
	 */
	public class GenericReducer extends Reducer<Text, Text, Text, Text>
	{
		/**
		 * The setup method for this reducer
		 * @param context - the context 
		 */
		protected void setup( Context context )
		{
			
		}

		/**
		 * The cleanup method for this reducer
		 * @param context - the context
		 */
		protected void cleanup( Context context )
		{
			
		}

		/**
		 * The reduce method
		 * @param key - the input key
		 * @param value - the input value
		 * @param context - the context
		 */
		protected void reduce( Text key, Text value, Context context )
		{
			
		}
	}
}