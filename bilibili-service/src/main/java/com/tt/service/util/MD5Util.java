package com.tt.service.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * MD5加密
 * 单向加密算法
 * 特点：加密速度快，不需要秘钥，但是安全性不高，需要搭配随机盐值使用
 */
public class MD5Util {

    public static String sign(String content, String salt, String charset) {
        content = content + salt;
        return DigestUtils.md5Hex(getContentBytes(content, charset));
    }

    public static boolean verify(String content, String sign, String salt, String charset) {
        content = content + salt;
        String mysign = DigestUtils.md5Hex(getContentBytes(content, charset));
        return mysign.equals(sign);
    }

    private static byte[] getContentBytes(String content, String charset) {
        if (!"".equals(charset)) {
            try {
                return content.getBytes(charset);
            } catch (UnsupportedEncodingException var3) {
                throw new RuntimeException("MD5签名过程中出现错误,指定的编码集错误");
            }
        } else {
            return content.getBytes();
        }
    }

    //获取文件md5加密后的字符串
    public static String getFileMD5(MultipartFile file) throws Exception {
        //JAVA输入流:将数据从内存硬盘网络等地方输入到java程序中的流对象;
        //JAVA输出流:将数据从程序中输出到内存硬盘网络等地方的流对象
        InputStream fis = file.getInputStream(); //文件内容输入到程序的输入流对象中
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); //可转换为内存中的字节数组的输出流对象
        byte[] buffer = new byte[1024];
        int byteRead = 0;
        //将程序中的输入流信息在while循环中一点点的读进buffer数组里(从网络中传输的file文件输入到程序内存中的byte数组里，这是输入流的作用)
        while ((byteRead = fis.read(buffer)) > 0) {
            //baos是输出流对象，它又把程序内存中的buffer数组里的内容读进了非程序内存中的baos对象里，与输入流是逆过程
            baos.write(buffer, 0, byteRead);
        }
        //目的只是把file的内容读进byte数组里以供加密而已
        fis.close();
        return DigestUtils.md5Hex(baos.toByteArray());
    }
}