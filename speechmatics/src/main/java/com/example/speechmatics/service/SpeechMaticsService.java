package com.example.speechmatics.service;

import java.util.List;
import java.util.Objects;

/**
 * @author wyq
 * @date 2023/10/19
 * @desc
 */


public interface SpeechMaticsService {

    String createNewJob(String filePath,String langCode);

    List<String> jobIdList();

    void jobProgress(String taskId);

    Objects getSubtitles(String taskId);
}
