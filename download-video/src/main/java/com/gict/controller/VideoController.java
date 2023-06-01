package com.gict.controller;

import com.gict.entity.Video;
import com.gict.service.FailedRecordService;
import com.gict.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping
public class VideoController {

    @Autowired
    private VideoService videoService;
    @Autowired
    private FailedRecordService failedRecordService;

    @GetMapping("/saveVideo")
    public void saveVideo(String classify, Integer start, Integer end) {
        if (start == null || end == null || start > end){
            System.out.println("参数错误");
            return;
        }
        videoService.download(classify, start, end);
    }

    @GetMapping("/downloadVideo")
    public void downloadVideo(Integer id){
        videoService.downloadVideo(id);
    }

    @GetMapping("/downloadFailed")
    public void downloadFailed(){
        videoService.downloadFailed();
    }

    /**
     * 下载视频分片合并
     * @param id
     */
    @GetMapping("/downloadFailedRecord")
    public void downloadFailedRecord(Integer id){
        failedRecordService.downloadFailed(id);
    }

    /**
     * 合并视频
     * @param id
     */
    @GetMapping("/mergeSegmentsByFF")
    public void mergeSegmentsByFF(Integer id){
        Video video = videoService.getById(id);
        try {
            failedRecordService.mergeSegmentsByFF(video);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
