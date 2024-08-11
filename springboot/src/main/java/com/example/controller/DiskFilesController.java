package com.example.controller;

import cn.hutool.core.lang.Dict;
import com.example.common.Result;
import com.example.entity.DiskFiles;
import com.example.entity.Share;
import com.example.entity.Trash;
import com.example.service.DiskFilesService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 网盘文件前端操作接口
 **/
@RestController
@RequestMapping("/diskFiles")
public class DiskFilesController {
    @Resource
    private DiskFilesService diskFilesService;

    /**增加*/
    @PostMapping("/add")
    public Result add(MultipartFile file, String name, String folder, Integer folderId) {
        diskFilesService.add(file, name, folder, folderId);
        return Result.success();
    }

    /**复制*/
    @PostMapping("/copy/{id}")
    public Result copy(@PathVariable Integer id, Integer folderId) {
        diskFilesService.copy(id,null);
        return Result.success();
    }

    /**分享*/
    @PostMapping("/share")
    public Result share(@RequestBody DiskFiles diskFiles) {
        Share share = diskFilesService.share(diskFiles);
        return Result.success(share);
    }

    /**下载*/
    @GetMapping("/download/{flag}")
    public void download(@PathVariable String flag, HttpServletResponse response) {
        diskFilesService.download(flag, response);
    }

    /**预览*/
    @GetMapping("/preview/{id}")
    public void preview(@PathVariable Integer id, HttpServletResponse response) {
        diskFilesService.preview(id, response);
    }

    /**删除*/
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id){
        diskFilesService.deleteById(id);
        return Result.success();
    }

    /**批量删除*/
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids){
        diskFilesService.deleteBatch(ids);
        return Result.success();
    }

    /**移入垃圾箱*/
    @DeleteMapping("/trash/{id}")
    public Result trashById(@PathVariable Integer id){
        diskFilesService.trashById(id);
        return Result.success();
    }

    /**批量删除到回收站*/
    @DeleteMapping("/trash/batch")
    public Result trashBatch(@RequestBody List<Integer> ids){
        diskFilesService.trashBatch(ids);
        return Result.success();
    }

    /**修改*/
    @PutMapping("/update")
    public  Result updateById(@RequestBody DiskFiles diskFiles){
        diskFilesService.updateById(diskFiles);
        return Result.success();
    }

    /**根据id查询*/
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id){
        DiskFiles diskFiles = diskFilesService.selectById(id);
        return Result.success(diskFiles);
    }

    /**查询所有*/
    @GetMapping("/selectAll")
    public Result selectAll(DiskFiles diskFiles){
        List<DiskFiles> list = diskFilesService.selectAll(diskFiles);
        return Result.success(list);
    }

    /**分页查询*/
    @GetMapping("/selectPage")
    public Result selectPage(DiskFiles diskFiles,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<DiskFiles> pageInfo = diskFilesService.selectPage(diskFiles, pageNum, pageSize);
        return Result.success(pageInfo);

    }

    /**查询所有父级目录*/
    @GetMapping("/selectFolders")
    public Result selectFolders(Integer folderId){
        List<DiskFiles> list = new ArrayList<> ();
        if(folderId == null){
            return Result.success(list);
        }
        diskFilesService.selectFolders(folderId, list);
        Collections.reverse(list);
        return Result.success(list);
    }

    /**查询回收站所有*/
    @GetMapping("/selectTrash")
    public Result selectTrash(){
        List<Trash> list = diskFilesService.selectTrash();
        return Result.success(list);
    }

    /**恢复文件*/
    @PutMapping("/restore/{id}")
    public  Result restore(@PathVariable Integer id){
        diskFilesService.restore(id);
        return Result.success();
    }

    /**
     * 查询分享的数据
     */
    @GetMapping("/selectShare")
    public Result selectShare(Integer shareId, Integer folderId) {
        List<DiskFiles> list = diskFilesService.selectShare(shareId, folderId);
        return Result.success(list);
    }

    //文件上传统计
    @GetMapping("/count")
    public Result count(Integer days){
        List<Dict> list = diskFilesService.count(days);
        return Result.success(list);
    }
}
