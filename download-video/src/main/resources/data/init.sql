CREATE TABLE `failed_record` (
                                 `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
                                 `save_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '保存路径',
                                 `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '下载路径',
                                 `video_id` int DEFAULT NULL COMMENT '视频id',
                                 `is_del` tinyint(1) DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除。默认0',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='下载失败记录';

CREATE TABLE `video` (
                         `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
                         `title` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '视频标题',
                         `file_Path` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '网站的相对路径-真实路劲',
                         `save_path` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '保存在磁盘的路径',
                         `m3u8_path` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'm3u8的真实路径',
                         `success` tinyint(1) DEFAULT NULL COMMENT '是否成功 1-成功，2-失败',
                         `classify` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分类名',
                         `is_del` tinyint(1) DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除。默认0',
                         `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         `base_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '下载路径',
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='视频表';