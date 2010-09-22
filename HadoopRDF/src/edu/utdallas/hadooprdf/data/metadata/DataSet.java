package edu.utdallas.hadooprdf.data.metadata;

import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import edu.utdallas.hadooprdf.conf.ConfigurationNotInitializedException;
import edu.utdallas.hadooprdf.data.commons.Constants;
import edu.utdallas.hadooprdf.lib.util.PathFilterOnFilenameExtension;
import edu.utdallas.hadooprdf.lib.util.Utility;

/**
 * This class contains metadata about a dataset
 * @author Mohammad Farhan Husain
 *
 */
public class DataSet {
	/**
	 * Extension of original data files
	 */
	private String m_sOriginalDataFilesExtension;
	/**
	 * The root path of the data set
	 */
	private Path m_DataSetRoot;
	/**
	 * Path to the data directory
	 */
	private Path m_PathToDataDirectory;
	/**
	 * Path to the original data, which can be in any format
	 */
	private Path m_PathToOriginalData;
	/**
	 * Path to data in NTriples format and later using namespaces
	 */
	private Path m_PathToNTriplesData;
	/**
	 * Path to encoded data
	 */
	private Path m_PathToEncodedData;
	/**
	 * Path to PS data
	 */
	private Path m_PathToPSData;
	/**
	 * Path to POS data
	 */
	private Path m_PathToPOSData;
	/**
	 * Path to metadata of the data set
	 */
	private Path m_PathToMetaData;
	/**
	 * Path to predicate list
	 */
	private Path m_PathToPredicateList;
	/**
	 * Path to Dictionary
	 */
	private Path m_PathToDictionary;
	/**
	 * Path to temporary directory
	 */
	private Path m_PathToTemp;
	/**
	 * The predicate collection of the data set
	 */
	private Collection<String> m_PredicateCollection;
	/**
	 * The Hadoop Configuration
	 */
	private org.apache.hadoop.conf.Configuration m_HadoopConfiguration;
	/**
	 * Class constructor
	 * @param sDataSetRoot the root path of the data set as a String
	 * @param sOriginalDataFilesExtension the extension of original data files in lower case
	 * @throws ConfigurationNotInitializedException
	 * @throws IOException 
	 */
	public DataSet(String sDataSetRoot) throws IOException, ConfigurationNotInitializedException {
		this(new Path(sDataSetRoot));
	}
	/**
	 * Class constructor
	 * @param dataSetRoot the root path of the data set
	 * @throws ConfigurationNotInitializedException
	 * @throws IOException 
	 */
	public DataSet(Path dataSetRoot) throws IOException, ConfigurationNotInitializedException {
		this(dataSetRoot, edu.utdallas.hadooprdf.conf.Configuration.getInstance().getHadoopConfiguration());
	}
	/**
	 * Class constructor
	 * @param dataSetRoot the root path of the data set
	 * @throws ConfigurationNotInitializedException
	 * @throws IOException 
	 */
	public DataSet(Path dataSetRoot, org.apache.hadoop.conf.Configuration hadoopConfiguration) throws IOException {
		m_HadoopConfiguration = hadoopConfiguration;
		m_sOriginalDataFilesExtension = null;
		m_DataSetRoot = dataSetRoot;
		m_PathToDataDirectory = new Path(m_DataSetRoot, "data");
		m_PathToOriginalData = new Path(m_PathToDataDirectory, "Original");
		m_PathToNTriplesData = new Path(m_PathToDataDirectory, "NTriples");
		m_PathToEncodedData = new Path(m_PathToDataDirectory, "Encoded");
		m_PathToPSData = new Path(m_PathToDataDirectory, "PS");
		m_PathToPOSData = new Path(m_PathToDataDirectory, "POS");
		m_PathToMetaData = new Path(m_DataSetRoot, "metadata");
		Utility.createDirectory(hadoopConfiguration, m_PathToMetaData);
		m_PathToPredicateList = new Path(m_PathToMetaData, "predicates");
		m_PathToDictionary = new Path(m_PathToMetaData, "dictionary");
		m_PathToTemp = new Path(m_DataSetRoot, "tmp");
		m_PredicateCollection = createPredicateCollection(hadoopConfiguration);
	}
	/**
	 * creates the predicate collection
	 * @param hadoopConfiguration the hadoop cluster configuration
	 * @return the predicate collection
	 * @throws IOException
	 */
	private Collection<String> createPredicateCollection(org.apache.hadoop.conf.Configuration hadoopConfiguration) throws IOException {
		Collection<String> predicateCollection = new TreeSet<String> ();
		FileSystem fs = FileSystem.get(hadoopConfiguration);
		FileStatus [] files = fs.listStatus(m_PathToPOSData, new PathFilterOnFilenameExtension(Constants.POS_EXTENSION));
		for (int i = 0; i < files.length; i++) {
			String sFilename = files[i].getPath().getName();
			int indexOfFirstNamespaceDelimiter = sFilename.indexOf(Constants.NAMESPACE_DELIMITER);
			int secondIndex = sFilename.indexOf(Constants.PREDICATE_OBJECT_TYPE_SEPARATOR, indexOfFirstNamespaceDelimiter + 1);
			if (-1 == secondIndex)
				secondIndex = sFilename.indexOf('.');
			if (secondIndex == indexOfFirstNamespaceDelimiter + 1) // rdf:type
				predicateCollection.add(sFilename.substring(0, indexOfFirstNamespaceDelimiter));
			else
				predicateCollection.add(sFilename.substring(0, secondIndex));
		}
		return predicateCollection;
	}
	/**
	 * @return the m_DataSetRoot
	 */
	public Path getDataSetRoot() {
		return m_DataSetRoot;
	}
	/**
	 * @return the m_PathToOriginalData
	 * @throws IOException 
	 */
	public Path getPathToOriginalData() throws IOException {
		Utility.createDirectoryIfNotExists(m_HadoopConfiguration, m_PathToOriginalData);
		return m_PathToOriginalData;
	}
	/**
	 * @return the m_PathToNTriplesData
	 */
	public Path getPathToNTriplesData() {
		return m_PathToNTriplesData;
	}
	/**
	 * @return the m_PathToPSData
	 */
	public Path getPathToPSData() {
		return m_PathToPSData;
	}
	/**
	 * @return the m_PathToPOSData
	 */
	public Path getPathToPOSData() {
		return m_PathToPOSData;
	}
	/**
	 * @return the m_PathToMetaData
	 */
	public Path getPathToMetaData() {
		return m_PathToMetaData;
	}
	/**
	 * @return the m_PathToTemp
	 */
	public Path getPathToTemp() {
		return m_PathToTemp;
	}
	/**
	 * @return the m_PathToDictionary
	 */
	public Path getPathToDictionary() {
		return m_PathToDictionary;
	}
	/**
	 * @param sDataFilesExtension the extension of the original data files
	 */
	public void setOriginalDataFilesExtension(String sDataFilesExtension) {
		m_sOriginalDataFilesExtension = sDataFilesExtension;
	}
	/**
	 * @return the m_sOriginalDataFilesExtension
	 * @throws DataFileExtensionNotSetException 
	 */
	public String getOriginalDataFilesExtension() throws DataFileExtensionNotSetException {
		if (null == m_sOriginalDataFilesExtension) throw new DataFileExtensionNotSetException("Extension of original data files is not set");
		return m_sOriginalDataFilesExtension;
	}
	/**
	 * @return the m_PredicateCollection
	 * @throws DataSetException 
	 */
	public Collection<String> getPredicateCollection() throws DataSetException {
		try {
			if (null == m_PredicateCollection && FileSystem.get(m_HadoopConfiguration).exists(m_PathToPOSData))
				m_PredicateCollection = createPredicateCollection(m_HadoopConfiguration);
		} catch (IOException e) {
			throw new DataSetException("PredicateCollection could not be built because\n" + e.getMessage());
		}
		return m_PredicateCollection;
	}
	/**
	 * @return the m_PathToEncodedData
	 */
	public Path getPathToEncodedData() {
		return m_PathToEncodedData;
	}
	/**
	 * @param mPathToEncodedData the m_PathToEncodedData to set
	 */
	public void setPathToEncodedData(Path mPathToEncodedData) {
		m_PathToEncodedData = mPathToEncodedData;
	}
	/**
	 * @return the m_PathToPredicateList
	 */
	public Path getPathToPredicateList() {
		return m_PathToPredicateList;
	}
	/**
	 * @param mPathToPredicateList the m_PathToPredicateList to set
	 */
	public void setPathToPredicateList(Path mPathToPredicateList) {
		m_PathToPredicateList = mPathToPredicateList;
	}
	/**
	 * @return the m_HadoopConfiguration
	 */
	public org.apache.hadoop.conf.Configuration getHadoopConfiguration() {
		return m_HadoopConfiguration;
	}
}
