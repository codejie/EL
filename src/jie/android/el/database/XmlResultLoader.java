package jie.android.el.database;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class XmlResultLoader {

	public enum DecoderType {
		
		UTF_8(0, "UTF-8"), UTF_16LE(1, "UTF-16LE"), UTF_16BE(2, "UTF-16BE"), EUC_JP(3, "EUC-JP");
		
		private final int index;
		private final String name;

		private DecoderType(int index, final String name) {
			this.index = index;
			this.name = name;
		}

		public final int getIndex() {
			return index;
		}
		
		public final String getName() {
			return name;
		}
		
	}
	
	private static class Decoder {
	
		private int index = -1;
		private Charset charset = null;
		private CharsetDecoder decoder = null;
		private int charsPerByte = -1;
		
		public Decoder(int index) {
			set(index);
		}
		
		private void set(int index) throws ArrayIndexOutOfBoundsException {
			for(DecoderType dt : DecoderType.values()) {
				if(index == dt.getIndex()) {
					initDecoder(dt.getIndex(), dt.getName());
					return;
				}
			}
			throw new ArrayIndexOutOfBoundsException();
		}
		
		private int initDecoder(int index, final String name) {
			charset = Charset.forName(name);
			decoder = charset.newDecoder();
			charsPerByte = (int) decoder.maxCharsPerByte();
			
			this.index = index;
			
			return 0;
		}
		
		public int getIndex() {
			return index;
		}

		public final char[] decode(final ByteBuffer in, final int length) {
			
			if(decoder == null) 
				return null;
			
			decoder.reset();
			
			char[] ret = new char[length * charsPerByte];
			final CharBuffer out = CharBuffer.wrap(ret);
			
			decoder.decode(in, out, true);
			decoder.flush(out);
			
			int size = out.position();			
			if(ret.length != size) {
				ret = Arrays.copyOf(ret, size);
			}
			
			return ret;
		}		
	}
	
	private static int currentDict	= -1;
	private static int currentIndex = -1;
	private static int currentStart = -1;
//	private Decoder wordDecoder = null;
	private static Decoder xmlDecoder = null;

	private static final byte[] blockCache = new byte[2 * 16 * 1024];
	
	public static final int setBlockCache(int dictid, final RandomAccessFile file, int index, int start, int offset, int size) {
		if(currentDict == dictid && currentIndex == index) {
			return 0;
		}

		final ByteBuffer in = ByteBuffer.allocateDirect(size);
		try {
			if(file.getChannel().read(in, offset) != size) {
				return -1;
			}
		} catch (IOException e) {
			return -1;
		}
		
		if(decompressBlock(in, size, blockCache) != 0)
			return -1;

		currentDict = dictid;
		currentIndex = index;
		currentStart = start;
		
		return 0;
	}
	
	private static int decompressBlock(ByteBuffer in, int size, byte[] out) {
		final Inflater inflater = new Inflater();
		final InflaterInputStream stream = new InflaterInputStream(new ByteArrayInputStream(in.array(), 0, size), inflater, size);
		
		try {
			while(stream.read(out) > 0);
		} catch (IOException e) {
			return -1;
		}
		inflater.end();
		return 0;
	}

	public static final String getXml(int decoder, int offset, int length) {
		if(xmlDecoder == null || xmlDecoder.getIndex() != decoder) {
			try {
				xmlDecoder = new Decoder(decoder);
			} catch (ArrayIndexOutOfBoundsException e) {
				return null;
			}
		}
		
		final ByteBuffer in = ByteBuffer.wrap(blockCache, offset - currentStart, length);
		char[] ret = xmlDecoder.decode(in, length);
		String xml = new String(ret);//, 0, length);
		return xml;
	}
			
}
