package com.gict.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gict.entity.FailedRecord;
import com.gict.entity.Video;
import com.gict.service.FailedRecordService;
import com.gict.service.VideoService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DownloadUtil {
    /**
     * 最终下载的域名
     */
//    public static String baseUrl = "https://vip6.3sybf.com";
//    public static String baseUrl = "https://dadi-bo.com";

    private static final int MAX_RETRIES = 3; // 最大重试次数
    private static final int RETRY_INTERVAL_MS = 3000; // 重试间隔时间（毫秒）

    private static final int CONNECTION_TIMEOUT = 5000; // 连接超时时间（毫秒）
    private static final int READ_TIMEOUT = 10000; // 读取超时时间（毫秒）

    private static final String fileSavePath = "D:\\video\\爬虫\\video\\白虎\\白虎1.mp4";

    @Autowired
    private FailedRecordService failedRecordService;
    @Autowired
    private VideoService videoService;


    public static void main(String[] args) {

        String segmentPath = "D:\\video\\爬虫\\temp\\b352e73fa9764fcaace352e58553292c";

        try {
            mergeSegments(segmentPath, "D:\\video\\爬虫\\video\\白虎\\(草莓熊)白虎巨乳主播周末回馈粉丝激情做爱.mp4");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取真实的下载路径
     *
     * @param m3u8Url
     * @return
     * @throws IOException
     */
    public String getRealPath(String m3u8Url) throws IOException {
        // 请求路径获取真实的下载地址
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(m3u8Url);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECTION_TIMEOUT)
                .setSocketTimeout(CONNECTION_TIMEOUT)
                .build();
        httpGet.setConfig(requestConfig);

        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();

        String m3u8Content = IOUtils.toString(entity.getContent(), "UTF-8");
        String[] lines = m3u8Content.split("\\R");

        // 找到包含视频片段的行
        for (String line : lines) {
            if (!line.startsWith("#")) {
                return line;
            }
        }

        httpClient.close();
        return null;
    }

    /**
     * @param m3u8Url
     * @param outputPath 输出的路径前缀
     * @param dirName    文件夹名称
     * @param baseUrl    真实路径的域名
     * @param video
     * @throws IOException
     */
    public List<FailedRecord> downloadM3U8Video(String m3u8Url, String outputPath, String dirName, String baseUrl, Video video) throws IOException {
        List<FailedRecord> failedRecords = new ArrayList<>();
        String tempUrl = outputPath + dirName;
        File file = new File(tempUrl);
        // 创建文件夹
        if (!file.exists()) {
            file.mkdir();
        }

        // 请求路径获取真实的下载地址
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(baseUrl + m3u8Url);
        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();

        String m3u8Content = IOUtils.toString(entity.getContent(), "UTF-8");
        String[] lines = m3u8Content.split("\\R");

        AtomicInteger count = new AtomicInteger(10000);

        String keyUrl = "";
        boolean isKey = false;

        // 创建一个数组存所有的分片路径
        List<String> urlList = new ArrayList<>();

        // 找到包含视频片段的行
        for (String line : lines) {
            if (line.endsWith(".ts")) {
                String videoUrl = "";

                if (line.startsWith("http")) {
                    // 这些都是网站的广告直接跳过
//                    videoUrl = line;
                    continue;
                } else {
                    videoUrl = baseUrl + line;
                }
                urlList.add(videoUrl);
            }

            // fixme 上面代码有跳过，目前分析出来的广告视频都是没加密的，真实视频内容是加密的，
            //  跳过广告判断有加密则需要对真实内容解密，但是有些视频又是没有加密的

            // 判断是否是加密的
            if (line.contains("#EXT-X-KEY:METHOD")) {
                String[] split = line.split("=");
                if (split[1].equals("NONE")) {
                    // 不需要解密
                } else {
                    // 获取算法与key
//                    String arithmetic = split[1].split(",")[0];
                    keyUrl = split[2].replaceAll("\"", "");
                    isKey = true;
                }
            }
        }
        byte[] bytes = null;
        if (isKey) {
            video.setKeyUrl(keyUrl);
            // 获取加密的key
            bytes = keyFileDownloader(baseUrl + keyUrl);
        }

        video.setTempUrl(tempUrl);
        videoService.updateById(video);

        // 创建线程池
        DownloadManager downloadManager = new DownloadManager();

        // 开始时间
        long startTime = System.currentTimeMillis();

        // 创建一个线程对下载过程监视
        new Thread(() -> {
            int consume = 0;
            //轮询是否下载成功
            while (!downloadManager.getExecutor().isTerminated()) {

                File[] files = file.listFiles();
                if (files != null) {
                    consume = files.length;
                } else {
                    consume = 0;
                }

                if (consume >= urlList.size()) {
                    downloadManager.shutdown();
                }

                // 过一秒查询一次
                try {
                    Thread.sleep(1000); // 休眠1秒
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 网速没有计算了，想计算的可以使用java.lang.management包中的OperatingSystemMXBean来获取网络流量信息
                System.out.println("已用时" + ((System.currentTimeMillis() - startTime) / 1000) + "秒！\t下载速度：" + "/s");
                System.out.println("\t已完成" + consume + "个，还剩" + (urlList.size() - consume) + "个！");
            }
            // 下载完成之后合并
            try {
                mergeSegmentsByFF(tempUrl, video.getSavePath(), video);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("合并失败");
            }
        }).start();

        // 采用多线程下载视频分片
        for (String segmentUrl : urlList) {
            byte[] finalBytes = bytes;
            downloadManager.getExecutor().execute(() -> {
                try {
                    downloadWithRetry(segmentUrl, tempUrl, count.getAndIncrement(), finalBytes, video);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        httpClient.close();
        return failedRecords;
    }

    /**
     * 请求获取key的字节数组
     *
     * @param keyUrl 下载的路径
     * @return key的字节数组
     */
    public byte[] keyFileDownloader(String keyUrl) {
        byte[] keyBytes = new byte[0];
        try {
            // Key 文件的 URL

            // 创建 HttpClient 对象
            CloseableHttpClient httpClient = HttpClients.createDefault();

            // 创建 HTTP GET 请求
            HttpGet httpGet = new HttpGet(keyUrl);

            // 发送请求并获取响应
            HttpResponse response = httpClient.execute(httpGet);

            // 检查响应状态码
            if (response.getStatusLine().getStatusCode() == 200) {
                // 获取响应体的字节数组
                keyBytes = EntityUtils.toByteArray(response.getEntity());

                // 输出字节数组长度
                System.out.println("Key file size: " + keyBytes.length + " bytes");
            } else {
                System.out.println("Failed to download key file. Response code: " + response.getStatusLine().getStatusCode());
            }

            // 关闭 HttpClient
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keyBytes;
    }

    /**
     * 重试
     *
     * @param fileUrl
     * @param savePath
     * @param count
     * @param bytes
     * @param video
     * @return
     */
    public void downloadWithRetry(String fileUrl, String savePath, int count, byte[] bytes, Video video) {
        int retryCount = 0;
        boolean success = false;

        while (retryCount < MAX_RETRIES && !success) {
            try {
                success = downloadSegment(fileUrl, savePath, count, bytes);
            } catch (IOException e) {
                retryCount++;
                System.out.println("重试第" + retryCount + "次");
                System.out.println("Download failed. Retrying in " + RETRY_INTERVAL_MS + "ms...");
                sleep(RETRY_INTERVAL_MS);
            }
        }
        if (!success) {
            FailedRecord failedRecord = new FailedRecord();
            failedRecord.setSaveUrl(savePath + "\\" + count + ".ts");
            failedRecord.setUrl(fileUrl);
            failedRecord.setVideoId(video.getId());

            if (failedRecordService != null){
                // 判断是否已经保存过
                LambdaQueryWrapper<FailedRecord> wrapper = Wrappers.lambdaQuery();
                wrapper.eq(FailedRecord::getVideoId, video.getId());
                wrapper.eq(FailedRecord::getUrl, failedRecord.getUrl());
                FailedRecord one = failedRecordService.getOne(wrapper);
                if (one ==null){
                    failedRecordService.save(failedRecord);
                }
            }
        }

        System.out.println("下载分片成功" + fileUrl);
    }

    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean downloadSegment(String segmentUrl, String outputDirectory, int count, byte[] bytes) throws IOException {
        String outputFilePath = outputDirectory + "\\" + count + ".ts";

        return downloadSegment(segmentUrl, bytes, outputFilePath);
    }

    /**
     * 下载分片
     * @param segmentUrl 分片远程路径
     * @param bytes 密钥
     * @param outputFilePath 输出路径
     * @return
     * @throws IOException
     */
    public boolean downloadSegment(String segmentUrl, byte[] bytes, String outputFilePath) throws IOException {
        URL url = new URL(segmentUrl);
//        System.out.println("获取连接...");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
//            System.out.println("连接成功...");

            BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFilePath));
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();

            // 需要解密
            if (bytes != null) {
                // 获取解密后的字节数组
                byte[] decrypt = M3U8DecryptUtil.decrypt(bytes, outputFilePath);
                // 覆盖原始的 TS 分片文件
                overwriteFile(outputFilePath, decrypt);
            }

            return true;
        }
        System.out.println("连接失败...");
        return false;
    }

    /**
     * 覆盖原文件
     *
     * @param filePath
     * @param data
     * @throws IOException
     */
    private static void overwriteFile(String filePath, byte[] data) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, data);
    }

    /**
     * 分片内容不会很大，直接一次性读完
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    private static byte[] readChunkFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }

    /**
     * @param outputFilePath   输出的文件名称
     * @param segmentDirectory 获取分片视频片段的目录路径
     * @throws IOException
     */
    public static void mergeSegments(String segmentDirectory, String outputFilePath) throws IOException {
        File directory = new File(segmentDirectory);
        File[] segmentFiles = directory.listFiles();

        if (segmentFiles != null && segmentFiles.length > 0) {
            // 按照名称排序
            Arrays.sort(segmentFiles, new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    return file1.getName().compareTo(file2.getName());
                }
            });
            Arrays.stream(segmentFiles).forEach(System.out::println);
            try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFilePath))) {
                for (File segmentFile : segmentFiles) {
                    if (segmentFile.isFile() && segmentFile.getName().endsWith(".ts")) {
                        try (InputStream input = new FileInputStream(segmentFile)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = input.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean mergeSegmentsByFF(String segmentDirectory, String outputFilePath, Video video) throws IOException {

        outputFilePath = outputFilePath.replaceAll(" ", "");

        try {
            // 构建 FFmpeg 命令
            String command = "ffmpeg -f concat -safe 0 -i \"" + segmentDirectory + "/filelist.txt\" -c copy " + outputFilePath;

            // 创建文件列表
            String fileListPath = segmentDirectory + "\\filelist.txt";
            File file = new File(fileListPath);
            if (!file.exists()) {
                file.createNewFile();
            }
            createFileList(segmentDirectory, fileListPath);

            // 执行 FFmpeg 命令
            Process process = Runtime.getRuntime().exec(command);

            // 读取命令执行结果
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 等待进程执行完成
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                // 正确
//                InputStream stdout = process.getInputStream();
//                readStream(stdout);
                // 获取标准输出
                System.out.println("合并完成：" + outputFilePath);
                if (videoService != null){
                    video.setSuccess(1);
                    videoService.updateById(video);
                }
                // 合并成功以后删除分片
                File fileDir = new File(segmentDirectory);
                FileUtils.cleanDirectory(fileDir);
                if (fileDir.exists()){
                    fileDir.delete();
                }
                return true;
            } else {
                // 获取错误输出
//                InputStream stderr = process.getErrorStream();
//                readStream(stderr);
                System.out.println("FFmpeg process exited with code: " + exitCode);
                if (videoService != null){
                    video.setSuccess(2);
                    videoService.updateById(video);
                }
                System.out.println("合并失败：" + outputFilePath);
                return false;
            }

            // 删除临时的文件列表文件
//            File fileListFile = new File(fileListPath);
//            fileListFile.delete();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 读取流的方法
    private static String readStream(InputStream stream) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append(System.lineSeparator());
                System.out.println(line);
            }
        }
        return result.toString();
    }

    private static void createFileList(String folderPath, String fileListPath) throws IOException {
        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".ts")); // 获取所有以 .ts 结尾的文件

        // 创建文件列表文件
        FileWriter writer = new FileWriter(fileListPath);
        for (File file : files) {
            writer.write("file '" + file.getAbsolutePath() + "'\n");
        }
        writer.close();
    }
}
