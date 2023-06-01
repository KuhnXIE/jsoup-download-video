package com.gict.util;

import com.alibaba.fastjson.JSONObject;
import com.gict.entity.Video;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DownloadVideo {

    /**
     * 爬虫抓取链接超时时间，单位：毫秒
     */
    public static final int SPIDER_TIMEOUT = 10000;
    /**
     * 爬虫默认的userAgent
     */
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36 Edg/113.0.1774.35";

//    private static String url = "https://2103--0718ue.aug-aiy.unasdwarfs.com:21805/vodsearch/白虎----------28---.html";
//    private static String url1 = "https://2103--0718ue.aug-aiy.unasdwarfs.com:21805/vodplay/72436-1-1.html";

    private static Map<String, String> map = new HashMap<>();

    static {
        map.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36 Edg/113.0.1774.35");
        map.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
    }

    public static String baseUrl = "https://2103--0718ue.aug-aiy.unasdwarfs.com:21805";

    public static String segmentPath = "D:\\video\\爬虫\\temp\\";

    private static String videoPath = "D:\\video\\爬虫\\video\\";

    private static String classify = "白虎";

    public static void main(String[] args) throws IOException {
        String videoSaveDir = videoPath + classify;
        File file = new File(videoSaveDir);
        if (!file.exists()) {
            file.mkdir();
        }

        List<Video> urlList = getUrl(classify, "");

        urlList.forEach(System.out::println);

        DownloadUtil downloadUtil = new DownloadUtil();

        for (Video video : urlList) {
            // 获取真实路径
            String m3U8Path = getM3U8Path(video.getFilePath());
            String baseUrl = m3U8Path.split("//")[1].split("/")[0];
            m3U8Path = downloadUtil.getRealPath(m3U8Path);
            System.out.println("获取真实路径：" + m3U8Path);
            System.out.println("获取真实路径域名：" + baseUrl);
            video.setM3u8Path(m3U8Path);
            // 根据真实路径去下载视频分片
            String tempDirName = UUID.randomUUID().toString().replaceAll("-", "");
            System.out.println("开始下载分片！");
            downloadUtil.downloadM3U8Video(m3U8Path, segmentPath, tempDirName, "https://" + baseUrl, video);
            System.out.println("下载分片完成！");
            // 将所有分片合并, 已经把代码合到下载处
//            DownloadUtil.mergeSegments(segmentPath + tempDirName, videoSaveDir + "\\" + video.getTitle() + ".mp4");
//            System.out.println(video.getTitle() + "合并完成,保存路径：" + videoSaveDir + "\\" + video.getTitle() + ".mp4");
        }
    }

    public static String getM3U8Path(String path) {
        String url = baseUrl + path;
        // 发送请求获取页面对象
        try {
            Document document = Jsoup.connect(url).userAgent(USER_AGENT).ignoreContentType(true).headers(map).timeout(SPIDER_TIMEOUT).get();
            Elements elementsWithClass = document.getElementsByClass("video-js vjs-default-skin");

            for (Element element : elementsWithClass) {
                Elements scriptElements = element.select("script");
                for (Element scriptElement : scriptElements) {
                    String scriptContent = scriptElement.html();
                    if (scriptContent.contains("player_data")) {
                        String[] split = scriptContent.split("=");
                        JSONObject jsonObject = JSONObject.parseObject(split[1]);
                        return jsonObject.getString("url");
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<Video> getAllUrl(String classify, Integer start, Integer end) throws IOException {
        List<Video> videos = new ArrayList<>();
        if (end > 0) {
            // 必须从2开始
            if (start < 2){
                videos.addAll(getUrl(classify, ""));
                start = 2;
            }
            for (Integer i = start; i <= end; i++) {
                videos.addAll(getUrl(classify, i));
            }
        }
        return videos;
    }

    /**
     * 发送请求获取url
     *
     * @throws IOException
     */
    public static List<Video> getUrl(String classify, Object page) throws IOException {
        String url = baseUrl + "/vodsearch/" + classify + "----------" + page + "---.html";

        // 发送请求获取页面对象
        Document document = Jsoup.connect(url).userAgent(USER_AGENT).ignoreContentType(true).headers(map).timeout(SPIDER_TIMEOUT).get();

        // 创建一个集合存所有的真实路径
        List<Video> hrefList = new ArrayList<>();

        System.out.println(document);
        // 获取列表里面的a标签,由于class名是唯一的，这里直接获取所有
        Elements elements = document.getElementsByClass("thumbnail");
        // 不为空的话
        if (!elements.isEmpty()) {
            // 获取所有的最终获取播放地址的页面路径
            for (int i = 1; i < elements.size(); i++) {
                try {
                    // 将参数保存到集合
                    Element element = elements.get(i);
                    Elements href = element.getElementsByAttribute("href");
                    String hrefStr = href.get(0).attr("href");
                    Elements title = element.getElementsByAttribute("title");
                    String titleStr = title.get(0).attr("title");
                    Video video = new Video();
                    video.setFilePath(hrefStr);
                    video.setTitle(titleStr);
                    video.setClassify(classify);
                    hrefList.add(video);
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("已经无数据");
                }
            }
        }

        return hrefList;
    }

}
