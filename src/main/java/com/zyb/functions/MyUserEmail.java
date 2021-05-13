package com.zyb.functions;

import com.zyb.functions.utils.Person;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * 随机生成邮箱
 */
public class MyUserEmail extends AbstractFunction {
    private static final List<String> desc = new LinkedList<>();
    private static final String KEY = "__generateUserEmail";

    @Override
    public String execute(SampleResult sampleResult, Sampler sampler) throws InvalidVariableException {
        Person person = new Person();
        return person.generateUserEmail();
    }

    @Override
    public void setParameters(Collection<CompoundVariable> collection) throws InvalidVariableException {

    }

    @Override
    public String getReferenceKey() {
        return "__generateUserEmail";
    }

    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }

    public static void main(String[] args) throws InvalidVariableException {
        for(int i=0;i<5;i++){
            System.out.println(new MyUserEmail().execute(null, null));
        }
    }
}
