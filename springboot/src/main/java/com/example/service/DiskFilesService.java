package com.example.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.Constants;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.DiskFiles;
import com.example.entity.Share;
import com.example.entity.Trash;
import com.example.exception.CustomException;
import com.example.mapper.DiskFilesMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 网盘文件信息表业务处理
 **/
@Service
public class DiskFilesService {
    @Resource
    private DiskFilesMapper diskFilesMapper;
    @Resource
    TrashService trashService;
    @Resource
    ShareService shareService;

    private static final String filePath = System.getProperty("user.dir") + "/disk/";

    private static final Logger log = LoggerFactory.getLogger(DiskFilesService.class);

    @Value("${server.port:9090}")
    private String port;

    @Value("${ip:localhost}")
    private String ip;

    /**
     * 新增
     */
    public void add(MultipartFile file, String name, String folder, Integer folderId) {
        // 参数传  文件夹的名称 是否是文件夹  文件夹的ID
        DiskFiles diskFiles = new DiskFiles();
        String now = DateUtil.now();
        diskFiles.setCrateTime(now);
        diskFiles.setUpdateTime(now);
        diskFiles.setFolder(folder);  // 是否是目录
        diskFiles.setName(name);
        diskFiles.setType(Constants.FILE_TYPE_FOLDER);
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.USER.name().equals(currentUser.getRole())) {
            diskFiles.setUserId(currentUser.getId());
        }
        diskFiles.setFolderId(folderId); // 当前文件/文件夹 外传的目录ID
        if (Constants.NOT_FOLDER.equals(folder)) {   // 不是文件夹
            String originalFilename = file.getOriginalFilename();
            diskFiles.setName(originalFilename);
            String extName = FileUtil.extName(originalFilename);   // 获取文件的后缀
            diskFiles.setType(extName);
            long flag = System.currentTimeMillis();
            String fileName = flag + "-" + originalFilename;  // 文件存储在磁盘的文件名
            if (!FileUtil.exist(filePath)) {
                FileUtil.mkdir(filePath);
            }
            try { //计算大小
                byte[] bytes = file.getBytes();  // byte
                double size = BigDecimal.valueOf(bytes.length).divide(BigDecimal.valueOf(1024), 3, RoundingMode.HALF_UP).doubleValue();
                diskFiles.setSize(size);
                // 文件上传
                file.transferTo(new File(filePath + fileName));
            } catch (Exception e) {
                log.error("DiskFiles Upload Error:", e);
            }
            String url = "http://" + ip + ":" + port + "/diskFiles/download/" + fileName;
            diskFiles.setFile(url);
        }
        diskFilesMapper.insert(diskFiles);

        if (folderId != null) {  // 外层有目录
            DiskFiles parentFolder = this.selectById(folderId);  // 获取到外层的目录
            Integer rootFolderId = parentFolder.getRootFolderId();
            diskFiles.setRootFolderId(rootFolderId);
        } else {
            if (Constants.IS_FOLDER.equals(folder)) {  // 当前是新建目录操作
                Integer diskFilesId = diskFiles.getId();  // 刚才插入到数据库的文件的ID
                diskFiles.setRootFolderId(diskFilesId);
            }
        }
        this.updateById(diskFiles);  // 更新 root_folder_id 字段的值
    }

    /**
     * 复制
     */
    public void copy(Integer id, Integer folderId) {
        DiskFiles diskFiles = this.selectById(id);
        if (diskFiles == null) {
            return;
        }
        // 新建一个对象
        diskFiles.setId(null);
        String now = DateUtil.now();
        diskFiles.setCrateTime(now);
        diskFiles.setUpdateTime(now);
        if(Constants.NOT_FOLDER.equals(diskFiles.getFolder())){
            String mainName =  FileUtil.mainName(diskFiles.getName());
            String extName = FileUtil.extName(diskFiles.getName());
            diskFiles.setName(mainName + "-copy." +extName);  // 复制的文件
        }else {
            diskFiles.setName(diskFiles.getName() + "-copy" );
        }
        diskFiles.setFolderId(folderId);  // 把拷贝后的文件夹下的所有子节点的folderId设置成当前的文件夹的ID
        diskFilesMapper.insert(diskFiles);
        if (diskFiles.getFolder().equals(Constants.IS_FOLDER)) {  // 是目录的  进行递归复制子文件
            List<DiskFiles> children = diskFilesMapper.selectByFolderId(id); // 排除被删除的文件或者文件夹
            if (CollUtil.isEmpty(children)) {
                return;
            }
            for (DiskFiles child : children) {
                this.copy(child.getId(), diskFiles.getId());  // 递归寻找子节点  复制
            }
        }
    }

    /**
     * 文件分享
     */
    public Share share(DiskFiles diskFiles) {
        Share share = new Share();
        share.setName(diskFiles.getName());
        share.setShareTime(DateUtil.now());
        share.setFileId(diskFiles.getId());
        Account currentUser = TokenUtils.getCurrentUser();
        if(RoleEnum.USER.name().equals(currentUser.getRole())){
            share.setUserId(currentUser.getId());
        }
        Integer days = diskFiles.getDays();
        DateTime dateTime = DateUtil.offsetDay(new Date(),days);
        String endTime = DateUtil.formatDateTime(dateTime);
        share.setEndTime(endTime);
        share.setCode(IdUtil.getSnowflakeNextIdStr()); //本次分享的验证码
        share.setType(diskFiles.getType());
        shareService.add(share);
        return share;
    }

    /**
     * 网盘文件下载
     */
    public void download(String flag, HttpServletResponse response) {
        OutputStream os;
        try {
            if (StrUtil.isNotEmpty(flag)) {
                response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(flag, "UTF-8"));
                response.setContentType("application/octet-stream");
                byte[] bytes = FileUtil.readBytes(filePath + flag);
                os = response.getOutputStream();
                os.write(bytes);
                os.flush();
                os.close();
            }
        } catch (Exception e) {
            System.out.println("DiskFiles download failed");
        }
    }

    /**
     * 文件预览
     */
    public void preview(Integer id, HttpServletResponse response) {
        DiskFiles diskFiles = this.selectById(id);
        if (diskFiles == null) {
            throw new CustomException(ResultCodeEnum.FILE_NOT_FOUND);
        }
        ArrayList<String> typeList = CollUtil.newArrayList("jpg", "jpeg", "png", "pdf", "gif");
        if (!typeList.contains(diskFiles.getType())) {
            throw new CustomException(ResultCodeEnum.TYPE_NOT_SUPPORT);
        }
        OutputStream os;
        try {
            String file = diskFiles.getFile();
            String flag = file.substring(file.lastIndexOf("/"));
            response.addHeader("Content-Disposition", "inline;filename=" + URLEncoder.encode(diskFiles.getName(), "UTF-8"));
            byte[] bytes = FileUtil.readBytes(filePath + flag);
            os = response.getOutputStream();
            os.write(bytes);
            os.flush();
            os.close();
        } catch (Exception e) {
            System.out.println("文件下载失败");
        }
    }

    //移入垃圾箱
    @Transactional
    public void trashById(Integer id) {
        DiskFiles diskFiles =  this.selectById(id);
        this.deepTrash(id);

        //移到垃圾箱
        Trash trash = new Trash();
        trash.setTime(DateUtil.now());
        trash.setFileId(id);
        trash.setName(diskFiles.getName());
        trash.setUserId(diskFiles.getUserId());
        trash.setSize(diskFiles.getSize());
        trashService.add(trash);
    }

    //递归删除到垃圾箱
    private void deepTrash(Integer id){
        DiskFiles diskFiles =  this.selectById(id);
        if(diskFiles == null){
            return;
        }
        diskFilesMapper.trashById(id); //删除当前文件记录
        if(Constants.NOT_FOLDER.equals(diskFiles.getFolder())){ //是文件
            return;
        }
        List<DiskFiles> children = diskFilesMapper.selectByFolderId(id);
        if(CollUtil.isEmpty(children)){
            //删除文件
            return;
        }
        for (DiskFiles child : children) {
            this.deepTrash(child.getId());
        }
    }


    /**批量删除到回收站*/
    public void trashBatch(List<Integer> ids) {
        for (Integer id: ids){
            trashById(id);
            diskFilesMapper.trashById(id);
        }
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        this.deepTrash(id);
        trashService.deleteByFileId(id);
        diskFilesMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for(Integer id: ids){
            this.deleteById(id);
        }
    }

    /**
     * 修改信息
     */
    public void updateById(DiskFiles diskFiles) {
        diskFilesMapper.updateById(diskFiles);
    }

    /**
     * 根据id查询
     */
    public DiskFiles selectById(Integer id) {
        return diskFilesMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<DiskFiles> selectAll(DiskFiles diskFiles) {
        Account currentUser = TokenUtils.getCurrentUser();
        if(RoleEnum.USER.name().equals(currentUser.getRole())){
            diskFiles.setUserId(currentUser.getId());
        }
        return diskFilesMapper.selectAll(diskFiles);
    }

    /**
     *分页查询
     */
    public PageInfo<DiskFiles> selectPage(DiskFiles diskFiles, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<DiskFiles> list = diskFilesMapper.selectAllData(diskFiles);
        return PageInfo.of(list);
    }

    /**查询所有父级目录*/
    public List<DiskFiles> selectFolders(Integer folderId,List<DiskFiles> list) {
        DiskFiles diskFiles = this.selectById(folderId);
        if(diskFiles == null){ //没有父级目录退出循环
            return list;
        }
        list.add(diskFiles);
        Integer parentFolderId = diskFiles.getFolderId(); // 父级id
        if(parentFolderId == null){ //当前外层没有目录就结束
            return list;
        }
        return this.selectFolders(parentFolderId, list);
    }

    /**查询所有回收站*/
    public List<Trash> selectTrash() {
        Integer userId = null;
        Account currentUser = TokenUtils.getCurrentUser();
        if(RoleEnum.USER.name().equals(currentUser.getRole())){
            userId = currentUser.getId();
        }
        return diskFilesMapper.selectTrash(userId);
    }

    /**
     * 还原文件
     */
    public void restore(Integer id) {
        this.deepRestore(id);//还原文件
        trashService.deleteByFileId(id);//删除回收站记录
    }
    //还原递归
    private void deepRestore(Integer id){
        DiskFiles diskFiles =  this.selectById(id);
        if(diskFiles == null){
            return;
        }
        diskFilesMapper.restoreById(id); //删除当前文件记录
        if(Constants.NOT_FOLDER.equals(diskFiles.getFolder())){ //是文件
            return;
        }
        List<DiskFiles> children = diskFilesMapper.selectAllByFolderId(id);
        if(CollUtil.isEmpty(children)){
            return;
        }
        for (DiskFiles child : children) {
            this.deepRestore(child.getId());
        }
    }

    /**
     * 查询分享的数据
     */
    public List<DiskFiles> selectShare(Integer shareId, Integer folderId) {
        if (folderId == null) {
            Share share = shareService.selectById(shareId);
            Integer fileId = share.getFileId();
            return CollUtil.newArrayList(this.selectById(fileId));
        } else {
            DiskFiles diskFiles = new DiskFiles();
            diskFiles.setFolderId(folderId);
            return this.selectAll(diskFiles);
        }
    }

    public List<Dict> count(Integer days) {
        List<Dict> list = new ArrayList<>();
        Date now = new Date();
        DateTime end = DateUtil.offsetDay(now, -1); //前一天
        DateTime start = DateUtil.offsetDay(now, -(days+1)); //起始日期
        List<DateTime> dateTimeList = DateUtil.rangeToList(start, end, DateField.DAY_OF_YEAR);
        List<String> dateList = dateTimeList.stream().map(DateUtil::formatDate).sorted(String::compareTo).collect(Collectors.toList());
        for (String date: dateList){
            Integer count = diskFilesMapper.selectByDate(date);
            Dict dict = Dict.create().set("date", date).set("count", count);
            list.add(dict);
        }
        return list;
    }
}
