package edu.utdallas.hadooprdf.data.preprocessing.predicateobjectsplit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import edu.utdallas.hadooprdf.conf.ConfigurationNotInitializedException;
import edu.utdallas.hadooprdf.data.SubjectObjectPair;
import edu.utdallas.hadooprdf.data.commons.Constants;
import edu.utdallas.hadooprdf.data.commons.Tags;
import edu.utdallas.hadooprdf.data.io.input.SOPInputFormat;
import edu.utdallas.hadooprdf.data.io.output.SOPOutputFormat;
import edu.utdallas.hadooprdf.data.metadata.DataFileExtensionNotSetException;
import edu.utdallas.hadooprdf.data.metadata.DataSet;
import edu.utdallas.hadooprdf.data.metadata.PredicateIdPairs;
import edu.utdallas.hadooprdf.data.metadata.StringIdPairsException;
import edu.utdallas.hadooprdf.data.preprocessing.lib.PreprocessorJobRunner;
import edu.utdallas.hadooprdf.lib.mapred.io.ByteLongLongWritable;
import edu.utdallas.hadooprdf.lib.util.PathFilterOnFilenameExtension;

/**
 * A class which runs a job to split PS files further according to the type of objects
 * @author Mohammad Farhan Husain
 *
 */
public class PredicateSplitterByObjectType extends PreprocessorJobRunner {
	public static final byte JOB1_TYPE_FLAG = 1;
	public static final byte JOB1_TRIPLE_FLAG = 2;
	private static final int BUFFER_SIZE = 1024 * 1024;
	
	private Path job1OutputDirectoryPath;
	private String rdfTypePredicate;
	private PredicateIdPairs predicateIdPairs;
	/**
	 * The class constructor
	 * @param dataSet the dataset to work on
	 * @throws DataFileExtensionNotSetException
	 * @throws StringIdPairsException 
	 */
	public PredicateSplitterByObjectType(DataSet dataSet)
			throws DataFileExtensionNotSetException, StringIdPairsException {
		super(dataSet);
		inputDirectoryPath = dataSet.getPathToPSData();
		job1OutputDirectoryPath = new Path(dataSet.getPathToTemp(), Constants.POS_EXTENSION + 1);
		outputDirectoryPath = dataSet.getPathToPOSData();
		inputFilesExtension = Constants.PS_EXTENSION;
		predicateIdPairs = new PredicateIdPairs(dataSet);
		rdfTypePredicate = "" + predicateIdPairs.getId(Constants.RDF_TYPE_URI_NTRIPLES_STRING);
	}
	/**
	 * The method which actually splits PS files according to their object type
	 * @throws ConfigurationNotInitializedException
	 * @throws PredicateSplitterByObjectTypeException
	 */
	public void splitPredicateByObjectType() throws ConfigurationNotInitializedException, PredicateSplitterByObjectTypeException {
		try {
			edu.utdallas.hadooprdf.conf.Configuration config =
				edu.utdallas.hadooprdf.conf.Configuration.getInstance();
			org.apache.hadoop.conf.Configuration hadoopConfiguration =
				new org.apache.hadoop.conf.Configuration(config.getHadoopConfiguration()); // Should create a clone so
			// that the original one does not get cluttered with job specific key-value pairs
			FileSystem fs = FileSystem.get(hadoopConfiguration);
			runJob1(config, hadoopConfiguration, fs);
			mergeFiles(fs);
			listTypes();
		} catch (IOException e) {
			throw new PredicateSplitterByObjectTypeException("IOException occurred:\n" + e.getMessage());
		}
	}
	
	private void listTypes() throws PredicateSplitterByObjectTypeException {
		edu.utdallas.hadooprdf.conf.Configuration config;
		try {
			config = edu.utdallas.hadooprdf.conf.Configuration.getInstance();
			org.apache.hadoop.conf.Configuration hadoopConfiguration =
				new org.apache.hadoop.conf.Configuration(config.getHadoopConfiguration()); // Should create a clone so
			// that the original one does not get cluttered with job specific key-value pairs
			// Must set all the job parameters before creating the job
			hadoopConfiguration.set(Tags.PATH_TO_DICTIONARY, dataSet.getPathToDictionary().toString());
			FileSystem fs;
			fs = FileSystem.get(hadoopConfiguration);
			// Create the job
			String sJobName = "Listing types for " + outputDirectoryPath.toString();
			Job job = new Job(hadoopConfiguration, sJobName);
			// Specify input parameters
			job.setInputFormatClass(TextInputFormat.class);
			Set<String> types = new HashSet<String> ();
			int tailLength = Constants.POS_EXTENSION.length() + 1;
			for (Long predicateId : predicateIdPairs.getPredicateIds()) {
				String predicateIdString = predicateId.toString();
				final String regex = predicateIdString + Constants.PREDICATE_OBJECT_TYPE_SEPARATOR + ".+\\." + Constants.POS_EXTENSION;
				FileStatus [] fstatus = fs.listStatus(outputDirectoryPath, new PathFilter() {
					@Override
					public boolean accept(Path path) {
						return path.getName().matches(regex);
					}
				});
				for (FileStatus f : fstatus) {
					String fileName = f.getPath().getName();
					int index = fileName.indexOf(Constants.PREDICATE_OBJECT_TYPE_SEPARATOR);
					String type = fileName.substring(index + 1, fileName.length() - tailLength);
					types.add(type);
				}
			}
			Path inputPath = new Path(dataSet.getPathToTemp(), "typeInput");
			fs.delete(inputPath, true);
			DataOutputStream dos = fs.create(new Path(inputPath, "types"), true);
			for (String type : types) {
				dos.writeBytes(type + '\n');
			}
			dos.close();
			FileInputFormat.addInputPath(job, inputPath);
			// Get input file names from dictionary directory
			boolean bInputPathEmpty = true;
			FileStatus [] fstatus = fs.listStatus(dataSet.getPathToDictionary());
			for (int i = 0; i < fstatus.length; i++) {
				if (!fstatus[i].isDir()) {
					FileInputFormat.addInputPath(job, fstatus[i].getPath());
					bInputPathEmpty = false;
				}
			}
			if (bInputPathEmpty)
				throw new PredicateSplitterByObjectTypeException("No dictionary file to use for listing types!");
			// Specify output parameters
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);
			job.setMapOutputKeyClass(Text.class);
			job.setMapOutputValueClass(Text.class);
			// Delete output directory
			Path tmpOutputPath = new Path(dataSet.getPathToTemp(), "typesOutput");
			fs.delete(tmpOutputPath, true);
			FileOutputFormat.setOutputPath(job, tmpOutputPath);
			// Set the mapper and reducer classes
			job.setMapperClass(edu.utdallas.hadooprdf.data.preprocessing.lib.IdListerMapper.class);
			job.setReducerClass(edu.utdallas.hadooprdf.data.preprocessing.lib.IdListerReducer.class);
			// Set the number of reducers
			job.setNumReduceTasks(1);
			// Set the jar file
			job.setJarByClass(this.getClass());
			// Run the job
			if (job.waitForCompletion(true)) {
				fs.delete(inputPath, true);
				fs.delete(dataSet.getPathToTypeList(), false);
				fs.rename(new Path(tmpOutputPath, "part-r-00000"), dataSet.getPathToTypeList());
				fs.delete(tmpOutputPath, true);
			} else
				throw new PredicateSplitterByObjectTypeException("Job1 of PredicateSplitterByObjectType failed");
		} catch (IOException e) {
			throw new PredicateSplitterByObjectTypeException("IOException occurred:\n" + e.getMessage());
		} catch (InterruptedException e) {
			throw new PredicateSplitterByObjectTypeException("InterruptedException occurred:\n" + e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new PredicateSplitterByObjectTypeException("ClassNotFoundException occurred:\n" + e.getMessage());
		} catch (ConfigurationNotInitializedException e) {
			throw new PredicateSplitterByObjectTypeException("ConfigurationNotInitializedException occurred:\n" + e.getMessage());
		}
	}
	
	private void mergeFiles(FileSystem fs) throws IOException {
		fs.delete(outputDirectoryPath, true);
		byte [] buffer = new byte[BUFFER_SIZE];
		for (Long predicateId : predicateIdPairs.getPredicateIds()) {
			String predicateIdString = predicateId.toString();
			final String regex = ".+" + Constants.PREDICATE_OBJECT_TYPE_SEPARATOR + predicateIdString
				+ "(" + Constants.PREDICATE_OBJECT_TYPE_SEPARATOR + ".+)?\\." + Constants.POS_EXTENSION;
			System.out.println("Regex: " + regex);
			FileStatus [] fstatus = fs.listStatus(job1OutputDirectoryPath, new PathFilter() {
				@Override
				public boolean accept(Path path) {
					return path.getName().matches(regex);
				}
			});
			Map<String, List<FileStatus>> targetSourceMap = new HashMap<String, List<FileStatus>> ();
			for (FileStatus f : fstatus) {
				String fileName = f.getPath().getName();
				System.out.println("Got: " + fileName);
				int index = fileName.indexOf(Constants.PREDICATE_OBJECT_TYPE_SEPARATOR);
				String targetName = fileName.substring(index + 1);
				List<FileStatus> fileList;
				if (null == (fileList = targetSourceMap.get(targetName))) {
					fileList = new LinkedList<FileStatus> ();
					targetSourceMap.put(targetName, fileList);
				}
				fileList.add(f);
			}
			for (String targetName : targetSourceMap.keySet()) {
				System.out.println("Got target: " + targetName);
				Path newFile = new Path(outputDirectoryPath, targetName);
				DataOutputStream dos = fs.create(newFile, true);
				for (FileStatus sourceFile : targetSourceMap.get(targetName)) {
					System.out.println("\tGot source: " + sourceFile.getPath().getName());
					DataInputStream dis = fs.open(sourceFile.getPath());
					int bytesRead;
					while (-1 != (bytesRead = dis.read(buffer))) {
						dos.write(buffer, 0, bytesRead);
					}
					dis.close();
				}
				dos.close();
			}
		}
	}
	/**
	 * Runs job 1 of the process
	 * @param config the HadoopRDF configuration
	 * @param hadoopConfiguration the Hadoop configuration
	 * @param fs the file system
	 * @throws ConfigurationNotInitializedException
	 * @throws PredicateSplitterByObjectTypeException
	 */
	private void runJob1(edu.utdallas.hadooprdf.conf.Configuration config,
			org.apache.hadoop.conf.Configuration hadoopConfiguration,
			FileSystem fs) throws ConfigurationNotInitializedException, PredicateSplitterByObjectTypeException {
		try {
			// Delete output directory
			fs.delete(job1OutputDirectoryPath, true);
			// Must set all the job parameters before creating the job
			hadoopConfiguration.set(Tags.RDF_TYPE_PREDICATE, rdfTypePredicate);
			// Create the job
			String sJobName = "PS file splitter by Object Types Job1 for " + inputDirectoryPath;
			Job job = new Job(hadoopConfiguration, sJobName);
			// Specify input parameters
			job.setInputFormatClass(SOPInputFormat.class);
			boolean bInputPathEmpty = true;
			// Get input file names
			FileStatus [] fstatus = fs.listStatus(inputDirectoryPath, new PathFilterOnFilenameExtension(inputFilesExtension));
			for (int i = 0; i < fstatus.length; i++) {
				if (!fstatus[i].isDir()) {
					FileInputFormat.addInputPath(job, fstatus[i].getPath());
					bInputPathEmpty = false;
				}
			}
			if (bInputPathEmpty)
				throw new PredicateSplitterByObjectTypeException("No PS file to split by object type!");
			// Specify output parameters
			job.setMapOutputKeyClass(LongWritable.class);
			job.setMapOutputValueClass(ByteLongLongWritable.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(SubjectObjectPair.class);
			job.setOutputFormatClass(SOPOutputFormat.class);
			FileOutputFormat.setOutputPath(job, job1OutputDirectoryPath);
			// Set the mapper and reducer classes
			job.setMapperClass(edu.utdallas.hadooprdf.data.preprocessing.predicateobjectsplit.mapred.PredicateSplitterByObjectTypeJob1Mapper.class);
			job.setReducerClass(edu.utdallas.hadooprdf.data.preprocessing.predicateobjectsplit.mapred.PredicateSplitterByObjectTypeJob1Reducer.class);
			// Set the number of reducers
			if (-1 != getNumberOfReducers())	// Use the number set by the client, if any
				job.setNumReduceTasks(getNumberOfReducers());
			else if (-1 != config.getNumberOfTaskTrackersInCluster())	// Use one reducer per TastTracker, if the number of TaskTrackers is available
				job.setNumReduceTasks(config.getNumberOfTaskTrackersInCluster());
			// Set the jar file
			job.setJarByClass(this.getClass());
			// Run the job
			if (job.waitForCompletion(true))
				;//fs.delete(inputDirectoryPath, true);	// Delete input data i.e. PS data
			else
				throw new PredicateSplitterByObjectTypeException("Job1 of PredicateSplitterByObjectType failed");
		} catch (IOException e) {
			throw new PredicateSplitterByObjectTypeException("IOException occurred:\n" + e.getMessage());
		} catch (InterruptedException e) {
			throw new PredicateSplitterByObjectTypeException("InterruptedException occurred:\n" + e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new PredicateSplitterByObjectTypeException("ClassNotFoundException occurred:\n" + e.getMessage());
		}
	}
}
