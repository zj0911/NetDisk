package com.example.mapper;

import com.example.entity.DiskFiles;
import com.example.entity.Trash;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


public interface DiskFilesMapper {

    /**增加*/
    int insert(DiskFiles diskFiles);

    /**
     * 删除
     */
    int deleteById(Integer id);

    /**
     * 修改
     */
    int updateById(DiskFiles diskFiles);

    /**
     * 根据ID查询
     */
    DiskFiles selectById(Integer id);

    /**
     * 查询所有
     */
    List<DiskFiles> selectAll(DiskFiles diskFiles);

    /**
     * 查询所有后台数据
     */
    List<DiskFiles> selectAllData(DiskFiles diskFiles);

    /**删除到回收站*/
    @Update("update disk_files set `delete` = 1 where id = #{id}")
   void trashById(Integer id);

    /**根据外层文件id查找文件*/
    List<DiskFiles> selectByFolderId(Integer folderId);
    List<DiskFiles> selectAllByFolderId(Integer folderId);

    /**查询被删除文件*/
    List<Trash> selectTrash(Integer userId);

    /**还原文件*/
    @Update("update disk_files set `delete` = 0 where id = #{id}")
    void restoreById(Integer id);

    @Select("select  count(*) from disk_files where crate_time like concat('%', #{date}, '%')")
    Integer selectByDate(String date);
}
