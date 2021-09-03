package com.zyb.functions;

import com.zyb.functions.utils.FileWrapper;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * 从csv/txt中返回制定行列的数据
 */
public class CSVRandomRead extends AbstractFunction {
    private static final Logger log = LoggerFactory.getLogger(CSVRandomRead.class);
    private static final String KEY = "__CSVRandomRead";
    private static final List<String> desc =new LinkedList<>();
    private Object[] values; // Parameter list

    public static void main(String[] args) throws InvalidVariableException {
        new CSVRandomRead().execute(null,null);
    }

    static {
        desc.add("请填写csv格式的文件路径"); //$NON-NLS-1$
        desc.add("请输入行的值，第一行是0，第二行是1...,不填写则返回随机列");
        desc.add("请输入列的值，第一列是0，第二列是1...");
    }

    @Override
    public String execute(SampleResult sampleResult, Sampler sampler) throws InvalidVariableException {
        String myValue = ""; //$NON-NLS-1$

        String fileName = ((org.apache.jmeter.engine.util.CompoundVariable) values[0]).execute();
        String columnOrNext = ((org.apache.jmeter.engine.util.CompoundVariable) values[1]).execute();
        String targerRow = ((org.apache.jmeter.engine.util.CompoundVariable) values[2]).execute();

//        String fileName = "C:\\Users\\jueying\\Desktop\\testCSVRead.csv";
//        String targerRow = null;
//        String columnOrNext = "0";

        log.debug("execute ({}, {},{})   ", fileName, columnOrNext,targerRow);

        // Process __CSVRead(filename,*ALIAS)
        if (columnOrNext.startsWith("*")) { //$NON-NLS-1$
            FileWrapper.open(fileName, columnOrNext);
            /*
             * All done, so return
             */
            return ""; //$NON-NLS-1$
        }

        // if argument is 'next' - go to the next line
        if (columnOrNext.equals("next()") || columnOrNext.equals("next")) { //$NON-NLS-1$ //$NON-NLS-2$
            FileWrapper.endRow(fileName);

            /*
             * All done now, so return the empty string - this allows the caller
             * to append __CSVRead(file,next) to the last instance of
             * __CSVRead(file,col)
             *
             * N.B. It is important not to read any further lines at this point,
             * otherwise the wrong line can be retrieved when using multiple
             * threads.
             */
            return ""; //$NON-NLS-1$
        }

        //若列不是整数或者未填写的话，随机返回一列
        if(null == targerRow || !isInteger(targerRow) || targerRow.isEmpty()){
            targerRow = String.valueOf(randValueFromFile(fileName));
        }

        try {
            int columnIndex = Integer.parseInt(columnOrNext); // what column
            int rowIndex = Integer.parseInt(targerRow);
            // is wanted?
            myValue = FileWrapper.getColumn(fileName, rowIndex,columnIndex);
            System.out.println(myValue);
        } catch (NumberFormatException e) {
            log.warn("{} - can't parse column number: {} {}",
                    Thread.currentThread().getName(), columnOrNext,
                    e.toString());
        } catch (IndexOutOfBoundsException e) {
            log.warn("{} - invalid column number: {} at row {} {}",
                    Thread.currentThread().getName(), columnOrNext,
                    FileWrapper.getCurrentRow(fileName), e.toString());
        }

        log.debug("execute value: {}", myValue);

        return myValue;
    }

    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        values = parameters.toArray();
        checkMinParameterCount(parameters,2);
        FileWrapper.clearAll();// TODO only clear the relevant entry - if possible...
    }

    @Override
    public String getReferenceKey() {
        return KEY;
    }

    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }

    /*方法二：推荐，速度最快
     * 判断是否为整数
     * @param str 传入的字符串
     * @return 是整数返回true,否则返回false
     */

    private static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    /**
     * 返回一个0到文件内最大行数的随机数字
     * @param file
     * @return
     */
    private static int randValueFromFile(String file){
        int randomValue = new Random().nextInt(fileLineNum(file));
        return randomValue;
    }
    /**
     * 获取文件总行数
     * @param file 文件完整路径
     * @return
     */
    private static int fileLineNum(String file){
        int lineNum = 0;
        BufferedReader bufferedReader = null;
        try {
            FileReader fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            while(bufferedReader.readLine() != null){
                lineNum++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return lineNum;
    }
}
