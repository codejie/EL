package jie.android.el.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Dictionary {
	
	private static final String Tag = Dictionary.class.getSimpleName();

	//Info
	public final class Info {
		public int index = -1;
		public String title = null;
		public String file = null;
		public int offset = -1;
		public int d_decoder = -1;
		public int x_decoder = -1;
		
		public Info(int index, final String title, final String file, int offset, int d_decoder, int x_decoder) {
			this.index = index;
			this.title = title;
			this.file = file;
			this.offset = offset;
			this.d_decoder = d_decoder;
			this.x_decoder = x_decoder;
		}
	}
	
	//XmlIndex
	public final class XmlIndex {
		public int offset = -1;
		public int length = -1;
		public int block1 = -1;
		public int block2 = -1;
		
		public XmlIndex() {			
		}
		
		public XmlIndex(int offset, int length, int block1) {
			this.offset = offset;
			this.length = length;
			if (block1 >= 0) {
				this.block1 = block1;
				this.block2 = -1;
			} else {
				this.block1 = -block1;
				this.block2 = this.block1 + 1;
			}
		}
	}
	
	//entity
	public final class Entity { 
		public final class BlockData {
			public int offset = 0;
			public int length = 0;
			public int start = 0;
			public int end = 0;					
		}

		private Info info = null;
		private RandomAccessFile fileAccess = null;
		
		private final ArrayList<BlockData> blockData = new ArrayList<BlockData>();
		
		public Entity(final Info info) {
			this.info = info;
		}

		private void init(final LACDBAccess dbAccess, final String dataPath) {
			try {
				fileAccess = new RandomAccessFile(dataPath + File.separator + info.file, "r");
								loadBlockData(dbAccess);
				
			} catch (FileNotFoundException e) {
				Log.e(Tag, "init() failed - " + e.getMessage());
			}
		}
		
		private void close(){
			if (fileAccess != null) {
				try {
					fileAccess.close();
				} catch (IOException e) {
					Log.e(Tag, "close() failed - " + e.getMessage());
				}
			}
		}
		
		private boolean loadBlockData(final LACDBAccess dbAccess) {
			Cursor cursor = dbAccess.queryBlockData(info.index);
			if(cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						BlockData block = new BlockData();
						block.offset = cursor.getInt(0);
						block.length = cursor.getInt(1);
						block.start = cursor.getInt(2);
						block.end = cursor.getInt(3);
						
						blockData.add(block);
					} while (cursor.moveToNext());
				}
				cursor.close();
			} else {
				return false;
			}
			return true;
		}
		
		public final List<String> getWordXmlResult(final LACDBAccess dbAccess, int index) {
			ArrayList<String> result = new ArrayList<String>();
			//get self xml
			getWordSelfXmlResult(dbAccess, index, result);
			//get ref xml
			//getWordRefXmlResult(dbAccess, index);
			if (result.size() > 0) {
				return result;
			} else {
				return null;
			}			
		}
		
		private void getWordSelfXmlResult(final LACDBAccess dbAccess, int index, List<String> result) {
			Cursor cursor = dbAccess.queryWordXmlIndex(info.index, index);
			if (cursor != null) {
				try {
					if (cursor.moveToFirst()) {
						do {
							XmlIndex xmlIndex = new XmlIndex(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2));
							String xml = getXmlResult(xmlIndex);
							if (xml != null) {
								result.add(xml);
							}
							
						} while (cursor.moveToNext());
					}
				} finally {
					cursor.close();
				}
			}
		}

		private String getXmlResult(XmlIndex xmlIndex) {
			if(xmlIndex.block1 > blockData.size())
				return null;
			final BlockData block = blockData.get(xmlIndex.block1);
			
			int start = block.start;
			int offset = block.offset;
			int size = block.length;
			if(xmlIndex.block2 != -1) {
				size += blockData.get(xmlIndex.block2).length;
			}			
			
			if(XmlResultLoader.setBlockCache(info.index, fileAccess, xmlIndex.block1, start, offset, size) != 0)
				return null;
			
			return XmlResultLoader.getXml(info.x_decoder, info.offset + xmlIndex.offset, xmlIndex.length);
		}

		public final Info getInfo() {
			return info;
		}
	}	
	
	//dictionay
	private LACDBAccess dbAccess = null;
	private String dataPath = null;
	private HashMap<Integer, Entity> mapEntity = new HashMap<Integer, Entity>();

	public Dictionary(LACDBAccess dbAccess, final String dataPath) {
		this.dbAccess = dbAccess;
		this.dataPath = dataPath;
	}
	
	public boolean load() {
		Cursor cursor = dbAccess.queryDictionaryInfo();
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					do {
						Info info = new Info(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5));
						Entity entity = new Entity(info);
						entity.init(dbAccess, dataPath);
						mapEntity.put(cursor.getInt(0), entity);
					} while (cursor.moveToNext());
				}
			} finally {
				cursor.close();
			}
		}		
		return true;
	}

	public void close() {
		for(final Entity entity : mapEntity.values()) {
			entity.close();
		}
	}
		
	public Word.XmlResult getWordXmlResult(int index) {
		Word.XmlResult result = new Word.XmlResult();		
		for (final Entity entity : mapEntity.values()) {
			List<String> res = entity.getWordXmlResult(dbAccess, index);
			if (res != null) {
				result.addXmlData(entity.info.index, res);
			}
		}
		return result;
	}
}
