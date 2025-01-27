package com.example.huiyan.huiyan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.huiyan.huiyan.entity.table.SrcLangSeg;
import com.example.huiyan.huiyan.entity.table.TextCompare;
import com.example.huiyan.huiyan.service.CompareService;
import com.example.huiyan.huiyan.service.CutWavService;
import com.example.huiyan.huiyan.service.DownloadFileService;
import com.example.huiyan.huiyan.service.HuiyanAsrService;
import com.example.huiyan.huiyan.service.SrcLangSegService;
import com.example.huiyan.huiyan.service.TextCompareService;
import com.example.huiyan.huiyan.utils.CsvTools;
import com.example.huiyan.huiyan.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author wyq
 * @date 2024/7/12
 * @desc
 */

@Slf4j
@Service
public class CompareServiceImpl implements CompareService {
    @Resource
    private SrcLangSegService srcLangSegService;
    @Resource
    private TextCompareService textCompareService;
    @Resource
    private CutWavService cutWavService;
    @Resource
    private HuiyanAsrService huiyanAsrService;
    @Resource
    private DownloadFileService downloadFileService;


    @Override
    public String selectSegSave(String jobId) {
        List<SrcLangSeg> segList = srcLangSegService.list(new LambdaQueryWrapper<SrcLangSeg>()
                .eq(SrcLangSeg::getJobId, jobId).orderByAsc(SrcLangSeg::getStartTimecode));
        if (CollectionUtils.isEmpty(segList)) {
            return "没有查询到句段";
        }
        List<TextCompare> textCompareList = new ArrayList<>();
        for (SrcLangSeg seg : segList) {
            TextCompare textCompare = new TextCompare();
            textCompare.setId(seg.getId());
            textCompare.setJobId(jobId);
            textCompare.setStartTimecode(seg.getStartTimecode());
            textCompare.setEndTimecode(seg.getEndTimecode());
            textCompare.setMicroSrcText(StringTools.formatSrt(seg.getTranscribedSrcText()));
            textCompareList.add(textCompare);
        }
        textCompareService.saveOrUpdateBatch(textCompareList);
        return "操作成功";
    }

    @Override
    public String cutAudioSlice(String jobId) {
        List<TextCompare> segList = textCompareService.list(new LambdaQueryWrapper<TextCompare>()
                .eq(TextCompare::getJobId, jobId).orderByAsc(TextCompare::getStartTimecode));
        if (CollectionUtils.isEmpty(segList)) {
            return "句段不存在";
        }
        String srcWavPath = "D:\\ru_compare\\" + jobId + ".wav";
        if (!new File(srcWavPath).exists()) {
            log.info("源文件不存在:{}", srcWavPath);
        }
        for (TextCompare seg : segList) {
            cutWavService.cutWav(jobId, srcWavPath, seg);
        }
        return "操作成功";
    }

    @Override
    public String huiyanAsr(String jobId) {
        List<TextCompare> compareList = textCompareService.list(new LambdaQueryWrapper<TextCompare>()
                .eq(TextCompare::getJobId, jobId).orderByAsc(TextCompare::getStartTimecode));
        if (CollectionUtils.isEmpty(compareList)) {
            return "没有查询到句段";
        }

        for (TextCompare compare : compareList) {
            if (StringUtils.isNotBlank(compare.getMicroSrcText()) && StringUtils.isNotBlank(compare.getHuiyanSrcText())) {
                continue;
            }
            String wavPath = "D:\\ru_compare\\" + jobId + "\\" + compare.getId() + ".wav";
            textCompareService.saveAsrResult(wavPath, compare);
        }

        return "操作成功";
    }

    @Override
    public void exportSeg(HttpServletResponse response, String jobId) {
        String outFilePath = "D:\\ru_compare\\" + jobId + ".csv";
        List<TextCompare> compareList = textCompareService.list(new LambdaQueryWrapper<TextCompare>()
                .eq(TextCompare::getJobId, jobId).orderByAsc(TextCompare::getStartTimecode));
        if (CollectionUtils.isEmpty(compareList)) {
            log.info("没有数据可下载");
            return;
        }
        List<List<String>> dataList = new ArrayList<>();
        for (TextCompare compare : compareList) {
            List<String> data = new ArrayList<>();
            data.add(compare.getId());
            data.add(compare.getJobId());
            data.add(String.valueOf(compare.getStartTimecode()));
            data.add(String.valueOf(compare.getEndTimecode()));
            data.add(compare.getMicroSrcText());
            data.add(compare.getHuiyanSrcText());
            data.add(String.valueOf((Objects.isNull(compare.getSame()) || compare.getSame()) ? 1 : 0));
            dataList.add(data);
        }
        CsvTools.create(outFilePath, "\t", dataList);
        downloadFileService.fileDownload(response, outFilePath);
    }
}
