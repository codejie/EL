package jie.android.el.database;

import java.util.ArrayList;
import java.util.List;


import android.os.Parcel;
import android.os.Parcelable;

public class Word {
	
	public static final class Info implements Parcelable {
		
	    public static final Parcelable.Creator<Info> CREATOR = new Parcelable.Creator<Info>() {

			@Override
			public Info createFromParcel(Parcel source) {
				Info data = new Info();
				data.setIndex(source.readInt());
				data.setText(source.readString());
				return data;
			}

			@Override
			public Info[] newArray(int size) {
				return new Info[size];
			}
	    };	
		

		private int index = -1;
		private String text = null;
		
		public Info() {			
		}
		
		public Info(int id, final String word) {
			this.index = id;
			this.text = word;
		}
		
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flag) {
			dest.writeInt(index);
			dest.writeString(text);
		}

		@Override
		public String toString() {
			return super.toString();
		}

		public int getIndex() {
			return index;
		}

		protected void setIndex(int value) {
			index = value;
		}

		public final String getText() {
			return text;
		}
		
		protected void setText(final String value) {
			text = value;
		}
	}
	
	public static class XmlResult implements Parcelable {

		public static final Parcelable.Creator<XmlResult> CREATOR = new Parcelable.Creator<XmlResult>() {

			@Override
			public XmlResult createFromParcel(Parcel source) {
				
				XmlResult result = new XmlResult();
				
				while (source.dataAvail() > 0) {
					int id = source.readInt();
					List<String> xml = new ArrayList<String>();
					source.readStringList(xml);
					result.addXmlData(id, xml);
				}
				
				return result;
			}

			@Override
			public XmlResult[] newArray(int size) {
				return new XmlResult[size];
			}
			
		};
		
		public static final class XmlData {
			private int dict = -1;
			private List<String> xml = null;
			
			public XmlData(int dict, final List<String> xml) {
				this.dict = dict;
				this.xml = xml;
			}

			public int getDictIndex() {
				return dict;
			}

			public final List<String> getXml() {
				return xml;
			}
		}
		
		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flag) {
			//dest.writeInt(xmlData.size());
			for (XmlData data : xmlData) {
				dest.writeInt(data.getDictIndex());
				dest.writeStringList(data.getXml());
			}
		}
		
		private List<XmlData> xmlData = new ArrayList<XmlData>();
		
		public void addXmlData(int dictid, final List<String> res) {
			xmlData.add(new XmlData(dictid, res));
		}
		
		public final List<XmlData> getXmlData() {
			return xmlData;
		}			
	}
}
