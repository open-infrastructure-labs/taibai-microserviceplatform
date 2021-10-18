package com.fitmgr.meterage.utils;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.List;

/**
 * @author zhangxiaokang
 * @date 2020/10/30 14:45
 */
public class JaxbUtil {
    /**
     * JavaBean转换成xml 默认编码UTF-8
     * @param obj
     * @return
     */
    public static String convertToXml(Object obj) {
        return convertToXml(obj, "UTF-8");
    }

    /**
     * JavaBean转换成xml
     * @param obj
     * @param encoding
     * @return
     */
    public static String convertToXml(Object obj, String encoding) {
        StringBuffer result = new StringBuffer();
        try {
            if(obj instanceof List){
                @SuppressWarnings("rawtypes")
                List list = (List)obj;
                for(Object o:list){
                    result.append(toConvertToXml(o, encoding));
                }
            }else{
                result.append(toConvertToXml(obj, encoding));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public static String toConvertToXml(Object obj, String encoding) {
        String result = "";
        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            //决定是否在转换成xml时同时进行格式化（即按标签自动换行，否则即是一行的xml）
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            //xml的编码方式
            marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);

            StringWriter writer = new StringWriter();
            marshaller.marshal(obj, writer);
            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * xml转换成JavaBean
     * @param xml
     * @param c
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T converyToJavaBean(String xml, Class<T> c) {
        T t = null;
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            t = (T) unmarshaller.unmarshal(new StringReader(xml));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }
    /**
     * 根据流读取
     * @param inputStream
     * @param c
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T converyToJavaBean(InputStream inputStream, Class<T> c) {
        T t = null;
        try {
            JAXBContext context = JAXBContext.newInstance(c);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            t = (T) unmarshaller.unmarshal(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    public static String xmlToStr(String url) {
        Document document = null;
        try {
            SAXBuilder reader = new SAXBuilder();
            document = reader.build(new File(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Format format = Format.getPrettyFormat();
        // 设置编码格式
        format.setEncoding("UTF-8");
        // 输出对象
        StringWriter out = new StringWriter();
        XMLOutputter outputter = new XMLOutputter();
        try {
            outputter.output(document, out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out.toString();
    }
}
