package com.library.controller.common;
import com.library.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/common")
public class FileController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".") ? original.substring(original.lastIndexOf(".")) : "";
        String name = UUID.randomUUID().toString().replace("-","") + ext;
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        file.transferTo(new File(dir, name));
        return Result.ok("/uploads/" + name);
    }
}
