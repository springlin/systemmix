/**
 * 
 */
package com.myun.spring.airplay;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

/**
 * @author Administrator
 * 
 */
public class AirplayUtils {

	/**
	 * 将字节数组转换为ImageView可调用的Bitmap对象
	 * 
	 * @param bytes
	 * @param opts
	 * @return
	 */
	public static Bitmap getPicFromBytes(byte[] bytes, BitmapFactory.Options opts) {
		if (bytes != null) {

			if (opts != null) {
				return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
			} else {

				return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
			}
		}
		return null;
	}

	
	/**
	 * 把字节数组保存为�?��文件  
	 * @param b
	 * @param outputFile
	 * @return
	 */
	public static File getFileFromBytes(final byte[] b, final int off, final int len, final String outputFile) {
		BufferedOutputStream stream = null;
		File file = null;
		try {
			file = new File(outputFile);
			
			FileOutputStream fstream = new FileOutputStream(file);
			stream = new BufferedOutputStream(fstream);
			stream.write(b, off, len);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return file;
	}
	
	
	public static String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
			return sdDir.toString();
		} else {
			return "/mnt/sdcard";
		}

	}
	
	public static String getMediaNoticeValue(String common, String key) {
		try {
			common = common.substring(common.indexOf(key) + key.length(),
					common.indexOf("\n", common.indexOf(key)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return common;
	}
	

}
