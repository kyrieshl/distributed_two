package com.litemall.distributed_two.web;

import com.litemall.distributed_two.config.ObjectStorageConfig;
import com.litemall.distributed_two.service.FileSystemStorageService;
import com.litemall.distributed_two.service.StorageService;
import org.apache.commons.io.IOUtils;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.linlinjava.litemall.db.domain.LitemallStorage;
import org.linlinjava.litemall.db.service.LitemallStorageService;
import org.linlinjava.litemall.db.util.CharUtil;
import org.linlinjava.litemall.db.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/storage/storage")
public class StorageController {

    @Autowired
    private StorageService storageService;

    @Autowired
    private LitemallStorageService zmallStorageService;

    @Autowired
    private ObjectStorageConfig osConfig;

    private static String CONFIG_FILENAME = "templates/fdfs_client.conf";

    public static String PROTOCOL = "http://";

    public static String SEPARATOR = "/";

    public static String TRACKER_NGNIX_ADDR = "193.112.72.91";

    private static String path1;

    static {
        try {
            path1 = new File(FileSystemStorageService.class.getResource("/").getFile()).getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String fdfsClientConfigFilePath = path1 + File.separator + CONFIG_FILENAME;
    private static StorageClient1 storageClient1 = null;

    // 初始化FastDFS Client
    static {
        try {
            ClientGlobal.init(fdfsClientConfigFilePath);
            TrackerClient trackerClient = new TrackerClient(ClientGlobal.g_tracker_group);
            TrackerServer trackerServer = trackerClient.getConnection();
            if (trackerServer == null) {
                throw new IllegalStateException("getConnection return null");
            }

            StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
            if (storageServer == null) {
                throw new IllegalStateException("getStoreStorage return null");
            }

            storageClient1 = new StorageClient1(trackerServer,storageServer);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateUrl(String key){
        return "http://" + osConfig.getAddress() + ":" + osConfig.getPort() + "/storage/storage/fetch?key=" + key;
    }

    private final String generateKey(){
        String key = null;
        LitemallStorage storageInfo = null;

        do{
            key = CharUtil.getRandomString(20);
            storageInfo = zmallStorageService.findByKey(key);
        }
        while(storageInfo != null);

        return key;
    }

    @GetMapping("/list")
    public Object list(String key, String name,
                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "limit", defaultValue = "10") Integer limit,
                       String sort, String order){
        List<LitemallStorage> storageList = zmallStorageService.querySelective(key, name, page, limit, sort, order);
        int total = zmallStorageService.countSelective(key, name, page, limit, sort, order);
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("items", storageList);

        return ResponseUtil.ok(data);
    }

    @PostMapping("/create")
    public Object upload(@RequestParam("file") MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();
        try {
            byte[] buff = IOUtils.toByteArray(multipartFile.getInputStream());
            NameValuePair[] nameValuePairs = null;
            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
            String adress = PROTOCOL +TRACKER_NGNIX_ADDR + SEPARATOR + storageClient1.upload_file1(buff, suffix , nameValuePairs);
            String url = adress;
            LitemallStorage storageInfo = new LitemallStorage();
            String key = generateKey();
            storageInfo.setName(fileName);
            storageInfo.setSize((int)multipartFile.getSize());
            storageInfo.setType(multipartFile.getContentType());
            storageInfo.setAddTime(LocalDateTime.now());
            storageInfo.setModified(LocalDateTime.now());
            storageInfo.setKey(key);
            storageInfo.setUrl(url);
            storageInfo.setKey(key);
            storageInfo.setUrl(url);
            zmallStorageService.add(storageInfo);
            return adress;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /*@PostMapping("/create")
    public Object create(@RequestParam("file") MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseUtil.badArgumentValue();
        }
        String key = generateKey();
        storageService.store(inputStream, key);

        String url = generateUrl(key);
        LitemallStorage storageInfo = new LitemallStorage();
        storageInfo.setName(originalFilename);
        storageInfo.setSize((int)file.getSize());
        storageInfo.setType(file.getContentType());
        storageInfo.setAddTime(LocalDateTime.now());
        storageInfo.setModified(LocalDateTime.now());
        storageInfo.setKey(key);
        storageInfo.setUrl(url);
        zmallStorageService.add(storageInfo);
        return ResponseUtil.ok(storageInfo);
    }*/

    @PostMapping("/read")
    public Object read(Integer id) {
        if(id == null){
            return ResponseUtil.badArgument();
        }
        LitemallStorage storageInfo = zmallStorageService.findById(id);
        if(storageInfo == null){
            return ResponseUtil.badArgumentValue();
        }
        return ResponseUtil.ok(storageInfo);
    }

    @PostMapping("/update")
    public Object update(@RequestBody LitemallStorage litemallStorage) {

        zmallStorageService.update(litemallStorage);
        return ResponseUtil.ok(litemallStorage);
    }

    @PostMapping("/delete")
    public Object delete(@RequestBody LitemallStorage litemallStorage) throws IOException, MyException {
        String fileId = litemallStorage.getUrl();
        String result = "";
        int j = 0, startIndex = 0, endIndex = 0;
        for (int i = 0; i < fileId.length(); i++) {
            if (fileId.charAt(i) == '/') {
                j++;
                if (j == 2)
                    startIndex = i;
                else if (j == 3)
                    endIndex = i;
            }
        }
        result = fileId.substring(endIndex+1 ,fileId.length() );

            storageClient1.delete_file1(result);
        zmallStorageService.deleteByKey(litemallStorage.getKey());
        return ResponseUtil.ok();
    }

    @GetMapping("/fetch")
    public ResponseEntity<Resource> fetch(String key) {

        Resource file = storageService.loadAsResource(key);

        if(file == null) {
            ResponseEntity.notFound();
        }
        return ResponseEntity.ok().body(file);
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(String key) {

        Resource file = storageService.loadAsResource(key);
        if(file == null) {
            ResponseEntity.notFound();
        }
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    //获取文件元数据
    @GetMapping("/getMap")
    public static Map<String,String> getFileMetadata(String fileId) {
        try {
            NameValuePair[] metaList = storageClient1.get_metadata1(fileId);
            if (metaList != null) {
                HashMap<String,String> map = new HashMap<String, String>();
                for (NameValuePair metaItem : metaList) {
                    map.put(metaItem.getName(),metaItem.getValue());
                }
                return map;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
