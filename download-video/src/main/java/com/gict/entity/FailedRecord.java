package com.gict.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 下载失败记录
 * @TableName failed_record
 */
@TableName(value ="failed_record")
@Data
public class FailedRecord implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 保存路径
     */
    private String saveUrl;

    /**
     * 下载路径
     */
    private String url;

    /**
     * 视频id
     */
    private Integer videoId;

    /**
     * 是否删除：0-未删除，1-已删除。默认0
     */
    private Integer isDel;

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