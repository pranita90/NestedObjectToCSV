
package com.globalpayments.dims.reportdata.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.globalpayments.dims.common.exception.DimsApiException;
import com.globalpayments.dims.common.exception.DimsServiceErrorTypes;
import com.globalpayments.dims.reportdata.model.RptCase;

import lombok.extern.log4j.Log4j2;

/**
 * This material is the property of Global Payments and is intended solely for
 * its use. This material is proprietary to Global Payments and has been
 * furnished on a confidential and restricted basis. Global Payments expressly
 * reserves all rights, without waiver, election, or other limitation to the
 * full extent permitted by law, in and to this material and this information
 * contained therein. Any reproduction, use or display or other disclosure or
 * dissemination, by a method now known or later deveOloped, of this material or
 * the information contained therein, in whole or in art, without prior written
 * consent of Global Payments is strictly prohibited. Copyright � 2014-2018
 * 
 * @author Bitwise
 */

@Log4j2
public class CSVUtils {

	private static final String DEFAULT_SEPARATOR = ",";
	private static final String EMPTY_STRING = "";
	
	private CSVUtils() {
	}

	/**
	 * This method generates the CSV file
	 * 
	 * @param tempDataFile
	 * @param reportData
	 * @param columnDetails
	 * @throws DimsApiException
	 * @throws IOException
	 */
	
	public static void generateCSVFile(File tempDataFile, List<List<String>> reportData, List<String> columnDetails)
			throws DimsApiException, IOException {

		log.debug("column details : {}", columnDetails.toString());
		  try (FileWriter fileWriter = new FileWriter(tempDataFile)){
			writeData(fileWriter, columnDetails);
			for (List<String> data : reportData) {
				writeData(fileWriter, data);
			}
			fileWriter.flush();
		  }
		 catch (IOException ioException) {
			log.error("Erorr occured while generating csv file, {}", ioException.getMessage());
			throw new DimsApiException(DimsServiceErrorTypes.INTERNAL_SERVER_ERROR,
					"Erorr occured while generating csv file");
		}

	}

	/**
	 * This method write the data in csv file
	 * 
	 * @param writer
	 * @param dataList
	 * @throws IOException
	 */
	public static void writeData(Writer writer, List<String> dataList) throws IOException {
		boolean first = true;
		StringBuilder formattedData = new StringBuilder();
		for (String data : dataList) {
			if (!first) {
				formattedData.append(DEFAULT_SEPARATOR);
			}
			formattedData.append(followCSVformat(data));
			first = false;
		}
		formattedData.append("\n");
		writer.append(followCSVformatforData(formattedData.toString()));

	}

	/**
	 * @param data
	 * @return
	 */
	
	private static String followCSVformatforData(String data) {
		String result = data;
		if (null != data && result.contains("–")) {
			result = result.replace("–", "-");
		} 
		return result;
	}
	
	private static String followCSVformat(String data) {
		String result = data;

		if (null != data && result.contains("\"")) {
			result = result.replace("\"", "\"\"");
		} else if (null != data && data.contains(DEFAULT_SEPARATOR)) {
			result = "\"" + result + "\"";
		} 
		else if (null == data) {
			result = EMPTY_STRING;
		}
		return result;
	}

	/**
	 * This method is used for downloading different files ex: test.csv,test.xlsx
	 * etc
	 * 
	 * @param file
	 * @param fileName
	 * @param fileType
	 * @return
	 * @throws IOException
	 */
	public static ResponseEntity<ByteArrayResource> download(File file, String fileName, String fileType)
			throws IOException {

		HttpHeaders header = new HttpHeaders();
		header.add(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + fileName + "." + fileType.toLowerCase() + "\"");
		header.add("Cache-Control", "no-cache, no-store, must-revalidate");
		header.add("Pragma", "no-cache");
		header.add("Expires", "0");
		header.add("content-type","application/json;charset=UTF-8");

		Path path = Paths.get(file.getAbsolutePath());
		ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

		return ResponseEntity.ok().headers(header).contentLength(file.length())
				.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
	}

	/**
	 * This method create the file at temporary location.
	 * 
	 * @param fileName
	 * @param fileExtension
	 * @return
	 * @throws DimsApiException
	 */
	public static File getTempFile(String fileName, String fileExtension) throws DimsApiException {

		try {
			File tempDataFile = File.createTempFile(fileName, fileExtension);
			log.debug("Temp file location : {}", tempDataFile.getAbsolutePath());
			return tempDataFile;

		} catch (IOException ioe) {
			log.error("Error occured while creating temporary file", ioe.getMessage());
			throw new DimsApiException(ioe, DimsServiceErrorTypes.INTERNAL_SERVER_ERROR);
		}
	}
	/**
	 * This method is responsible for creating a CSV file to export.
	 * 
	 * @param columnHeaders
	 * @param caseDataList
	 * @param fileType
	 * @return
	 * @throws DimsApiException
	 */
	public static File generateFile(List<String> columnHeaders, List<List<String>> caseDataList, String fileType)
			throws DimsApiException {
		File tempFile = CSVUtils.getTempFile(AppConstants.TEMP_FILE_NAME, fileType);
		try {
			CSVUtils.generateCSVFile(tempFile, caseDataList, columnHeaders);
		} catch (IOException e) {
			log.error("IOException occured while CSV file for case data export", e);
			throw new DimsApiException(DimsServiceErrorTypes.INTERNAL_SERVER_ERROR, AppConstants.INVALID_FILE);
		}
		return tempFile;
	} 
    /**
     * @return
     */
    /**get all headers/columns from Pojo class
     * @return
     */
    public static List<String> getColumnNames() { 
    	List<String> resultHeader = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        itarateAllDeclaredFields(RptCase.class, RptCase.class.getPackage().getName(), "",resultHeader);
		return resultHeader;              
    }
    
    /**
     * @param caseDetailslist
     * @return get all values from Object
     */
    public static List<List<String>> getCaseDeatilsList(List<RptCase> caseDetailslist) {
    	List<List<String>> finalreportData = new ArrayList<List<String>>();
    	if(!caseDetailslist.isEmpty()) {
    		List<String> resultData = new ArrayList<String>();        	
        	 for (RptCase rptCase : caseDetailslist) {
             	try {
             		iterateDeclaredFieldValues(RptCase.class, RptCase.class.getPackage().getName(), rptCase, resultData); 				
     				finalreportData.add(resultData);
     				resultData=new ArrayList<String>();
     			} catch (IllegalAccessException e) {
     				log.error("unable to retrieve values from RPT data while generating csv file",e);
     			}
     		}
    	}
    	
		return finalreportData; 
    	
    }
    /**
     *
     * @param c The class to get fields from
     * @param rootPackage The root package to compare(we only get fields from the same package to exclude other java class)
     * @param parentName The parent class name so that we could combine all its path.
     * @param sb the string builder to append values with comma delimited
     * <p/>
     * This function will search recursively and append all the fields
     * MyObject{int fileld1, OtherObject, other}, OtherObject{int filed2, String filed3}  --> filed1,other_filed2,otherfield3
     */
    static void itarateAllDeclaredFields(Class c, String rootPackage, String parentName,List<String> resultHeader)
    {
        Field[] fields = c.getDeclaredFields();
        for (Field field : fields)
        {
            Class filedClass = field.getType();
            String fieldName = field.getName();
            //if we have declaredfileds and the filed is in in the same package  as root and filed is not Enum, we continue search
            if (filedClass.getDeclaredFields().length > 0 && filedClass.getPackage().getName().contains(rootPackage) && !filedClass.isEnum())
            {
            	itarateAllDeclaredFields(filedClass, rootPackage, getCombinedName(parentName, fieldName,resultHeader),resultHeader);
            }
            //If it is plain fields like String/int/bigDecimal, we append the filed name.
            else
            {
	            if(!(field.getName().equals("serialVersionUID"))){
	               resultHeader.add(fieldName);
	            }
            }
        }
    }
     
    private static String getCombinedName(String parentName, String fieldName,List<String> resultHeader)
    {
        return "".equals(parentName) ? fieldName : parentName + "_" + fieldName;
    }
    
    public static void iterateDeclaredFieldValues(Class c, String rootPackage, Object target, List<String> resultData) throws IllegalAccessException
    {
        Field[] fields = c.getDeclaredFields();
        for (Field field : fields)
        {
            Class filedClass = field.getType();
            field.setAccessible(true);
            Object childObject = null;
            try
            {
                //try to get the object value from the 'target' Object
                childObject = field.get(target);
            }
            catch (Exception e)
            {
                //do nothing, just a try to get value, exception is expected with empty columns
            }
            //if we have declaredfileds and the filed is in in the same package  as root and filed is not Enum, we continue search
            if (filedClass.getDeclaredFields().length > 0 && filedClass.getPackage().getName().contains(rootPackage) && !filedClass.isEnum())
            {
            	iterateDeclaredFieldValues(filedClass, rootPackage, childObject, resultData);
            }
            //If it is plain fields like String/int/bigDecimal, we append the filed value.
            else
            {
                //Since this is served as CSV, we do not want the object value contains comma which would break the formatting.
               // sb.append(",").append(String.valueOf(childObject).replaceAll(",", "").replaceAll("(\r\n|\n)", ""));
            	if(!(field.getName().equals("serialVersionUID"))){
            		 resultData.add(String.valueOf(childObject).replaceAll(",", "").replaceAll("(\r\n|\n)", ""));
            	}               
            }     
        }
    }
}
