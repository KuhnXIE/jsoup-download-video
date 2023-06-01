package com.gict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gict.entity.FailedRecord;
import com.gict.entity.Video;
import com.gict.service.FailedRecordService;
import com.gict.mapper.FailedRecordMapper;
import com.gict.service.VideoService;
import com.gict.util.DownloadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author xie
* @description 针对表【failed_record(下载失败记录)】的数据库操作Service实现
* @createDate 2023-05-25 20:08:10
*/
@Service
public class FailedRecordServiceImpl extends ServiceImpl<FailedRecordMapper, FailedRecord>
    implements FailedRecordService{

    @Autowired
    private VideoService videoService;
    @Autowired
    private DownloadUtil downloadUtil;

    @Override
    public void downloadFailed(Integer id) {
        LambdaQueryWrapper<FailedRecord> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(FailedRecord::getVideoId, id);
        List<FailedRecord> list = list(wrapper);
        if (CollectionUtils.isNotEmpty(list)){
            Video video = videoService.getById(id);
            if (video != null){
                byte[] bytes = downloadUtil.keyFileDownloader("https://" + video.getBaseUrl() + video.getKeyUrl());

                for (FailedRecord failedRecord : list) {
                    try {
                        // 下载成功删除
                        downloadUtil.downloadSegment(failedRecord.getUrl(), bytes, failedRecord.getSaveUrl());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    boolean result = mergeSegmentsByFF(video);
                    if (result){
                        removeByIds(list.stream().map(FailedRecord::getId).collect(Collectors.toList()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                System.err.println("视频为空");
            }
        }
    }

    @Override
    public boolean mergeSegmentsByFF(Video video) throws IOException {
        return downloadUtil.mergeSegmentsByFF(video.getTempUrl(), video.getSavePath(), video);
    }
}




