package jie.android.el.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import android.util.Log;

public class XmlTranslator {

/**
 * 
<C> = <DIV style="MARGIN: 5px 0px">
<F> = <DIV style="WIDTH: 100%">
<L> = <B>
<H> = <SPAN style="LINE-HEIGHT: normal; COLOR: #000000; FONT-SIZE: 10.5pt">
<I> = <DIV style="MARGIN: 0px 0px 5px">
<N> = <DIV style="MARGIN: 4px 0px">
<U> = <FONT color=#c00000>
<M> = <SPAN style="LINE-HEIGHT: normal; FONT-FAMILY: 'Lingoes Unicode'; FONT-SIZE: 10.5pt">[<FONT color=#009900>

<x> = <FONT color=#009900>
<h> = <I>
	
 */
	
/*
word = increased
self(2879901) xml = <C><F><H /><I><N><U>adj.</U>  ���ӵ�; ��ǿ��</N></I></F></C>
self(2879709) xml = <C><E>increases|increased|increasing</E><F><H><M>in��crease || ?n'kr??s</M></H><I><N><U>n.</U>  ����, ����, ����</N></I><I><N><U>v.</U>  ����; ��ֳ; ����; �Ӵ�</N></I></F></C>
 */
	
	private static Transformer transformer = null;
	
	public static void init(final InputStream xslFile) {
		Source xslt = new StreamSource(xslFile);
		try {
			transformer = TransformerFactory.newInstance().newTransformer(xslt);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}
	}
	
	public static final String trans(final String xml) {
		Source input = new StreamSource(new StringReader(xml));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        try {
			transformer.transform(input, new StreamResult(output));
		} catch (TransformerException e) {
			return null;
		}
        
        return output.toString();
	}
	
	public static void test(final InputStream xmlFile, final InputStream xsltFile) {
		
		Source xml = new StreamSource(xmlFile);
		Source xslt = new StreamSource(xsltFile);
		
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(output);		
		
		TransformerFactory transFact = TransformerFactory.newInstance();
		try {
			Transformer trans = transFact.newTransformer(xslt);
			trans.transform(xml, result);
			String str = output.toString("UTF-8");
			Log.d("=====", "html=" + str);
			
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
		
}
