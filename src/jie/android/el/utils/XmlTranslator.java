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
	
//	
//	
////	private static final String DEF_PART_1 = "<DIV style=\"PADDING-BOTTOM: 0px; LINE-HEIGHT: 1.2em; PADDING-LEFT: 10px; WIDTH: 100%; PADDING-RIGHT: 10px; FONT-FAMILY: 'Tahoma'; FONT-SIZE: 10.5pt; PADDING-TOP: 10px\"><TABLE border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><TBODY><TR><TD style=\"BORDER-BOTTOM: #92b0dd 1px solid; BORDER-LEFT: #92b0dd 1px solid; LINE-HEIGHT: 1em; BACKGROUND: #cfddf0; COLOR: #000080; FONT-SIZE: 9pt; BORDER-TOP: #92b0dd 1px solid; BORDER-RIGHT: #92b0dd 1px solid\" nowrap><SPAN style=\"PADDING-BOTTOM: 0px; PADDING-LEFT: 2px; PADDING-RIGHT: 4px; PADDING-TOP: 0px\">Vicon English-Chinese(S) Dictionary</SPAN></TD><TD style=\"BORDER-BOTTOM: #92b0dd 1px solid\"></TD><TD style=\"BORDER-BOTTOM: #92b0dd 1px solid\" width=\"100%\" align=\"right\"><DIV style=\"WIDTH: 11px; HEIGHT: 11px; MARGIN-RIGHT: 10px\"></DIV></TD><TD style=\"BORDER-BOTTOM: #92b0dd 1px solid\"><DIV style=\"WIDTH: 11px; HEIGHT: 11px; \"></DIV></TD></TR></TBODY></TABLE><DIV ><DIV style=\"MARGIN: 5px 0px\"><DIV style=\"WIDTH: 100%\"><DIV style=\"LINE-HEIGHT: normal; FLOAT: left\">&nbsp;</DIV><DIV style=\"OVERFLOW-X: hidden; WIDTH: 100%\"><DIV style=\"LINE-HEIGHT: normal; MARGIN: 0px 0px 5px; COLOR: #808080\"><SPAN style=\"LINE-HEIGHT: normal; COLOR: #000000; FONT-SIZE: 10.5pt\"><B>";
////	private static final String DEF_PART_2 = "</B></SPAN> &nbsp;<SPAN style=\"LINE-HEIGHT: normal;FONT-SIZE: 10.5pt\">[<FONT color=\"#009900\">";
////	private static final String DEF_PART_3 = "</FONT>]</SPAN></DIV><DIV style=\"MARGIN: 0px 0px 5px\">";
////	private static final String DEF_PART_4 = "</DIV></DIV></DIV></DIV></DIV></DIV>";
//	
//	private static final String DEF_C_START = "<DIV style=\"PADDING-BOTTOM: 0px; LINE-HEIGHT: 1.2em; PADDING-LEFT: 10px; WIDTH: 100%; PADDING-RIGHT: 10px; FONT-FAMILY: 'Tahoma'; FONT-SIZE: 14pt; PADDING-TOP: 10px\"><TABLE border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><TBODY><TR><TD style=\"BORDER-BOTTOM: #92b0dd 1px solid; BORDER-LEFT: #92b0dd 1px solid; LINE-HEIGHT: 1em; BACKGROUND: #cfddf0; COLOR: #000080; FONT-SIZE: 10pt; BORDER-TOP: #92b0dd 1px solid; BORDER-RIGHT: #92b0dd 1px solid\" nowrap><SPAN style=\"PADDING-BOTTOM: 0px; PADDING-LEFT: 2px; PADDING-RIGHT: 4px; PADDING-TOP: 0px\">Vicon English-Chinese(S) Dictionary</SPAN></TD><TD style=\"BORDER-BOTTOM: #92b0dd 1px solid\"></TD><TD style=\"BORDER-BOTTOM: #92b0dd 1px solid\" width=\"100%\" align=\"right\"><DIV style=\"WIDTH: 11px; HEIGHT: 11px; MARGIN-RIGHT: 10px\"></DIV></TD><TD style=\"BORDER-BOTTOM: #92b0dd 1px solid\"><DIV style=\"WIDTH: 11px; HEIGHT: 11px; \"></DIV></TD></TR></TBODY></TABLE><DIV style=\"MARGIN: 5px 0px\"><DIV style=\"WIDTH: 100%\"><DIV style=\"LINE-HEIGHT: normal; FLOAT: left\"></DIV><DIV style=\"OVERFLOW-X: hidden; WIDTH: 100%\">";
//	private static final String DEF_C_END = "</DIV></DIV></DIV></DIV>";
//	private static final String DEF_WORD_START = "<DIV style=\"LINE-HEIGHT: normal; MARGIN: 0px 0px 5px; COLOR: #808080\"><SPAN style=\"LINE-HEIGHT: 150%; COLOR: #000000; FONT-SIZE: 150%\"><B>";
//	private static final String DEF_WORD_END = "</B></SPAN>";
//	private static final String DEF_F_START = "</DIV><DIV style=\"MARGIN: 0px 0px 5px\">";
//	private static final String DEF_F_END = "</DIV>";
//	private static final String DEF_H_START = "<DIV style=\"MARGIN: 4px 0px\">";//"<DIV style=\"LINE-HEIGHT: normal; MARGIN: 0px 0px 5px; COLOR: #808080\"><SPAN style=\"LINE-HEIGHT: normal; COLOR: #000000; FONT-SIZE: 10.5pt\"><B>";
//	private static final String DEF_H_END = "</DIV>";//"</B></SPAN></DIV>";
//	private static final String DEF_M_START = "<SPAN style=\"LINE-HEIGHT: 150%;FONT-SIZE: 100%\">[<FONT color=\"#009900\">";
//	private static final String DEF_M_END = "</FONT>]</SPAN>";
//	
//	private static final String DEF_I_START = "<DIV style=\"MARGIN: 0px 0px 5px\">";
//	private static final String DEF_I_END = "</DIV>";	
//	private static final String DEF_N_START = "<DIV style=\"MARGIN: 4px 0px\">";
//	private static final String DEF_N_END = "</DIV>";
//	private static final String DEF_U_START = "<FONT color=\"#C00000\">";
//	private static final String DEF_U_END = "</FONT>";
//	private static final String DEF_L_START = "<SPAN style=\"LINE-HEIGHT: normal; COLOR: #000000; FONT-SIZE: 110%\"><B>";
//	private static final String DEF_L_END = "</B></SPAN>";
//	
//	private static final String DEF_EXTENSION_START = "<DIV style=\"COLOR: #4444EE\">Extension</DIV><DIV style=\"LINE-HEIGHT:150%;FONT-SIZE: 110%\">";
//	private static final String DEF_EXTENSION_END = "</DIV>";
//
//	
//	public static class XmlHandler extends DefaultHandler {
//
//		//<C><F><H><M>e?;?</M></H><I><N>×ÖÄ¸A</N></I></F></C>
//		private enum Tags {
//			UNKNOWN, C, F, H, L, N, I, E, U, M
//		}
//		
//		private HashMap<String, Tags> tagMap = null;
//		
//		private String word = null;
//		
//		private String ret = null;
//		private Tags tag = Tags.UNKNOWN;
//		
//		private Stack<Tags> tagStack = new Stack<Tags>();
//		
//		private String extension = null;
//		
//		public XmlHandler() {
//			initMap();
//		}
//		
//		private void initMap() {
//			tagMap = new HashMap<String, Tags>();
//			
//			tagMap.put("C", Tags.C);
//			tagMap.put("F", Tags.F);
//			tagMap.put("H", Tags.H);
//			tagMap.put("L", Tags.L);
//			tagMap.put("N", Tags.N);
//			tagMap.put("I", Tags.I);
//			tagMap.put("E", Tags.E);
//			tagMap.put("U", Tags.U);
//			tagMap.put("M", Tags.M);
//		}
//		
//		public void reset(final String word) {
//			this.word = word;
//			this.ret = null;
//			tagStack.clear();
//		}
//
//		public final String getRet() {
//			return this.ret;
//		}
//
//		@Override
//		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
////			Log.d("======", "startElement() - uri:" + uri + " localName :" + localName + " qName:" + qName);
//			
//			tag = tagMap.get(qName);
//			if(tag == null) {
//				Log.e("========", "Unknown tag - " + qName);
//			}
//			if(tag == null)
//				return;
//						
////			Log.d("=====", "start tag = " + tag);
//			switch(tag) {
//			case C:
//				ret = DEF_C_START;
//				ret += DEF_WORD_START;
//				ret += word;
//				break;
//			case H:
//				ret += DEF_H_START;
//				//ret += word;
//				break;
//			case F:
//				ret += DEF_WORD_END;
//				ret += DEF_F_START;
//				break;
//			case M:
//				ret += DEF_M_START;
//				break;
//			case I:
//				ret += DEF_I_START;
//				break;
//			case U:
//				ret += DEF_U_START;
//				break;
//			case N:
//				ret += DEF_N_START;
//				break;
//			case L:
//				ret += DEF_L_START;
//				break;
//				
//			}
//			
//			tagStack.push(tag);
//			
//		}
//		
//		@Override
//		public void characters(char[] ch, int start, int length) throws SAXException {
////			Log.d("======", "characters() - ch:" + (new String(ch)).trim() + " start = " + start + " length:" + length);// uri + " localName :" + localName + " qName:" + qName);
//			String str = (new String(ch, start, length)).trim();
//			Log.d("=====", "characters = " + str);
//			if(tag == null)
//				return;			
//			switch(tag) {
//			case M:
//				ret += str;
//				break;
//			case N:
//				ret += str;
//				break;
//			case E:
//				extension = str;
//				break;
//			case U:
//				ret += str;
//				break;
//			case L:
//				ret += str;
//				break;
//			}
//		}
//		
//
//		@Override
//		public void endElement(String uri, String localName, String qName) throws SAXException {
////			Log.d("======", "endElement() - uri:" + uri + " localName :" + localName + " qName:" + qName);
//			
//			tag = tagMap.get(qName);
//			
//			if(tag == null) {
//				Log.e("========", "Unknown tag - " + qName);
//			}
//			if(tag == null)
//				return;
////			Log.d("=====", "end tag = " + tag);
//			
//			switch(tag) {
//			case C:
//
//				if(extension != null) {
//					ret += DEF_EXTENSION_START;
//					ret += extension;
//					ret += DEF_EXTENSION_END;
//					extension = null;
//				}
//				ret += DEF_C_END;
//				break;
//			case F:
//				ret += DEF_F_END;
//				break;
//			case H:
//				ret += DEF_H_END;
//				break;
//			case M:
//				ret += DEF_M_END;
//				break;
//			case I:
//				ret += DEF_I_END;
//				break;
//			case U:
//				ret += DEF_U_END;
//				break;
//			case N:
//				ret += DEF_N_END;
//				break;
//			case L:
//				ret += DEF_L_END;
//				break;
//			}			
//			
//			tag = tagStack.pop();			
//		}
//
//		@Override
//		public void startDocument() throws SAXException {
//			super.startDocument();
//			
//			//ret = DEF_HEAD_START;
//		}
//
//		@Override
//		public void endDocument() throws SAXException {
//			super.endDocument();
//			
//			//ret += DEF_HEAD_END;
//		}
//		
//	}
//	
//	private static XmlHandler handler = new XmlHandler();
//	
//	public static final String translate(final String word, final String xml) {
//		String ret = "<HTML><HEAD><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/></HEAD><BODY>";
//		
////		for(final String x : xml) {
//			try {
//				handler.reset(word);
//				ret += procXml(xml);
//			} catch (ParserConfigurationException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (SAXException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
////		}
//		
//		ret += "</BODY></HTML>";
//		
//		return ret;
//	}
//	
//	private static final String procXml(final String xml) throws ParserConfigurationException, SAXException, IOException {
//		
//		Log.d("===", "xml = " + xml);
//		
//		SAXParserFactory factory = SAXParserFactory.newInstance();
//		SAXParser parser = factory.newSAXParser();
//		InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
//		
//		final String ret = null;
//	
//		parser.parse(is, handler);
//		
//		return handler.getRet();
//	}
//	
}
