package com.zhuanbo.core.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class SerializeUtil {

	// 序列化
	public static byte[] serialize(Object object) {
		if (null == object) {
			return null;
		}

		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;

		try {

			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			byte[] bytes = baos.toByteArray();

			return bytes;
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			try {
				if (null != baos) {
					baos.close();
					baos = null;
				}
				if (null != oos) {
					oos.close();
					oos = null;
				}
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}

		return null;
	}

	// 反序列化
	public static Object unserialize(byte[] bytes) {
		if (null == bytes) {
			return null;
		}

		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;
		try {
			bais = new ByteArrayInputStream(bytes);
			ois = new ObjectInputStream(bais);
			return ois.readObject();
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			try {
				if (null != ois) {
					ois.close();
					ois = null;
				}
				if (null != bais) {
					bais.close();
					bais = null;
				}
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}

		return null;
	}

}
