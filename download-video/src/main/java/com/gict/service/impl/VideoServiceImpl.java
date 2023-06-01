package com.gict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gict.entity.Video;
import com.gict.service.VideoService;
import com.gict.mapper.VideoMapper;
import com.gict.util.DownloadUtil;
import com.gict.util.DownloadVideo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * @author XIE
 * @description 针对表【video(视频表)】的数据库操作Service实现
 * @createDate 2023-05-24 17:53:27
 */
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video>
        implements VideoService {

    @Value("${videoPath}")
    private String videoPath;
    @Value("${segmentPath}")
    private String segmentPath;
    @Autowired
    private DownloadUtil downloadUtil;

    @Override
    public void download(String classify, Integer start, Integer end) {
        String videoSaveDir = videoPath + classify;
        File file = new File(videoSaveDir);
        if (!file.exists()) {
            file.mkdir();
        }

        List<Video> urlList = null;
        try {
            // 爬取路径获取真实内容
            urlList = DownloadVideo.getAllUrl(classify, start, end);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (CollectionUtils.isEmpty(urlList)) {
            return;
        }

        urlList.forEach(System.out::println);

        for (Video video : urlList) {
            // 获取真实路径
            String m3U8Path;
            String baseUrl;
            try {
                m3U8Path = DownloadVideo.getM3U8Path(video.getFilePath());
                baseUrl = m3U8Path.split("//")[1].split("/")[0];
            } catch (Exception e) {
                continue;
            }
            try {
                m3U8Path = downloadUtil.getRealPath(m3U8Path);
            } catch (IOException e) {
                System.out.println("超时" + video.getTitle());
            }
            System.out.println("获取真实路径：" + m3U8Path);
            System.out.println("获取真实路径域名：" + baseUrl);
            video.setM3u8Path(m3U8Path);
            video.setSavePath(videoSaveDir + "\\" + video.getTitle() + ".mp4");
            video.setBaseUrl(baseUrl);
            video.setSuccess(2);

            LambdaQueryWrapper<Video> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(Video::getM3u8Path, video.getM3u8Path());
            Video exists = getOne(wrapper);
            if (exists != null) {
                continue;
            }
            save(video);
//                downloadReal(m3U8Path, baseUrl, video);
            // 将所有分片合并, 已经把代码合到下载处
//            DownloadUtil.mergeSegments(segmentPath + tempDirName, videoSaveDir + "\\" + video.getTitle() + ".mp4");
//            System.out.println(video.getTitle() + "合并完成,保存路径：" + videoSaveDir + "\\" + video.getTitle() + ".mp4");
        }
    }

    private void downloadReal(String m3U8Path, String baseUrl, Video video) throws IOException {
        // 根据真实路径去下载视频分片
        String tempDirName = UUID.randomUUID().toString().replaceAll("-", "");
        System.out.println("开始下载分片！");
        downloadUtil.downloadM3U8Video(m3U8Path, segmentPath, tempDirName, "https://" + baseUrl, video);
        System.out.println("下载分片完成！");
    }

    @Override
    public void downloadVideo(Integer id) {
        Video video = getById(id);

        downloadVideo(video);
    }

    private void downloadVideo(Video video) {
        if (video != null) {
            try {
                downloadReal(video.getM3u8Path(), video.getBaseUrl(), video);
            } catch (IOException e) {
                System.out.println("下载失败");
            }
        }
    }

    @Override
    public void downloadFailed() {
        LambdaQueryWrapper<Video> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Video::getSuccess, 2);
        wrapper.last("limit 5");
        List<Video> list = list(wrapper);

        if (!CollectionUtils.isEmpty(list)){
            for (Video video : list) {
                downloadVideo(video);
            }
        }
    }
}




