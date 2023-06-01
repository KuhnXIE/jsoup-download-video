package com.gict.service;

import com.gict.entity.FailedRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gict.entity.Video;

import java.io.IOException;

/**
* @author xie
* @description 针对表【failed_record(下载失败记录)】的数据库操作Service
* @createDate 2023-05-25 20:08:10
*/
public interface FailedRecordService extends IService<FailedRecord> {

    void downloadFailed(Integer id);

    boolean mergeSegmentsByFF(Video video) throws IOException;
}
