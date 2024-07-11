package com.example.huiyan.huiyan.service.path;

public interface FilePathService {

    /**
     * 创建临时文件路径
     */
    String getTempPath(String fileName);

    String getFilePath(String extension);

    String assTempPath();
}
