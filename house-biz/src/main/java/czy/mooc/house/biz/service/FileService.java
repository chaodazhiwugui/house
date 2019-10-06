package czy.mooc.house.biz.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
public class FileService {

    @Value("${file.path}")
    private String filePath;


    /**
     * 获取头像路径
     *
     * @param files
     * @return
     */
    public List<String> getImgPaths(List<MultipartFile> files) {

        if (Strings.isNullOrEmpty(filePath)) {
            filePath = getResourcePath();
        }
        List<String> paths = Lists.newArrayList();
        files.forEach(file -> {
            File localFile;
            if (!file.isEmpty()) {
                try {
                    localFile = saveToLocal(file, filePath);
                    String fp = new File(filePath).getAbsolutePath();
                    //localFile.getAbsolutePath()————>D:\IntelliJ IDEA 2019.1.2\project\house\house-web\src\main\resources\static\static\imgs\1569855809\nav_icon.png
                    String path = StringUtils.substringAfterLast(localFile.getAbsolutePath(), fp);
                    paths.add(path);
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
        return paths;
    }

    //D:\IntelliJ IDEA 2019.1.2\project\house\.
    public static String getResourcePath() {
        File file = new File(".");
        String absolutePath = file.getAbsolutePath();
        return absolutePath;
    }

    //保存头像到本地
    private File saveToLocal(MultipartFile file, String filePath2) throws IOException {
        File newFile = new File(filePath + "\\" + Instant.now().getEpochSecond() + "\\" + file.getOriginalFilename());
        if (!newFile.exists()) {
            newFile.getParentFile().mkdirs();
            newFile.createNewFile();
        }
        Files.write(file.getBytes(), newFile);//写到本地
        return newFile;
    }

    public static void main(String[] args) {
//        File file = new File("D:\\IntelliJ IDEA 2019.1.2\\project\\house\\house-web\\src\\main\\resources\\static\\static\\imgs\\1569854741\\nav_icon.png");
//        String s = StringUtils.substringAfterLast(file.getAbsolutePath(), "D:/IntelliJ IDEA 2019.1.2/project/house/house-web/src/main/resources/static/static/imgs");
//        System.out.println(s);
//        System.out.println(StringUtils.substringAfterLast("/user/images", "/user"));
//        System.out.println(new File("/user").getAbsolutePath());
//        System.out.println(new File("/user").getAbsoluteFile());
//        System.out.println(new File("/user"));

//        String file = "/static/imgs";
//        File f = new File(file + "/" + "dog.png");
//        System.out.println(f.getAbsolutePath());

//        String file = "/static/imgs";
//        File f = new File(file);
//        System.out.println(f);
//        System.out.println(f.getAbsolutePath());

        File file = new File("/user/imgs");
        System.out.println(file);

    }


}
