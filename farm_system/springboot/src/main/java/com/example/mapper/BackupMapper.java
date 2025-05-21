package com.example.mapper;

import com.example.entity.Backup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BackupMapper {

    List<Backup> selectAll();

    @Select("SELECT * FROM backup WHERE backup_id = #{id}")
    Backup selectById(@Param("id") Integer id);
    List<Backup> selectByCondition(@Param("operatorId") Integer operatorId);

    void insert(Backup backup);

    void deleteById(Integer id);

    // 根据备份ID获取文件路径
    String getFilePath(Integer backupId);
    // 获取过期文件列表
    List<String> getExpiredFiles(LocalDateTime expirationTime);
    // 根据备份文件路径获取备份时间
    LocalDateTime getBackupTime(String FilePath);
    //根据文件路径删除记录
    void deleteByFilePath(String filePath);
    // 根据备份时间获取文件
    List<Backup> selectByTime(LocalDateTime firstTime);
    //  根据时间间隔获取文件
    List<Backup> selectByInterval(LocalDateTime firstTime, LocalDateTime secondBackupTime);
}
