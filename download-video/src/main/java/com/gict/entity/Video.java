package com.gict.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 视频表
 * @TableName video
 */
@TableName(value ="video")
@Data
public class Video implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 视频标题
     */
    private String title;

    /**
     * 网站的相对路径-真实路劲
     */
    private String filePath;

    /**
     * 保存在磁盘的路径
     */
    private String savePath;

    /**
     * 保存在磁盘的路径
     */
    private String baseUrl;

    /**
     * m3u8的真实路径
     */
    private String m3u8Path;

    /**
     * m3u8的真实路径
     */
    private String keyUrl;

    /**
     * 临时文件
     */
    private String tempUrl;

    /**
     * 分类名
     */
    private String classify;

    /**
     * 是否删除：0-未删除，1-已删除。默认0
     */
    private Integer isDel;

    /**
     * 是否成功 1-成功，2-失败
     */
    private Integer success;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}