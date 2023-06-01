package com.gict.service;

import com.gict.entity.Video;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author XIE
* @description 针对表【video(视频表)】的数据库操作Service
* @createDate 2023-05-24 17:53:27
*/
public interface VideoService extends IService<Video> {

    void download(String classify, Integer start, Integer end);

    void downloadVideo(Integer id);

    void downloadFailed();

}
