package com.fyp.file_service.repository;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Repository
public class FileRepository {
    Cloudinary cloudinary; //private final
    public Map store(MultipartFile file, String folderName ) throws IOException {
        return cloudinary.uploader().upload(file.getBytes(),

                ObjectUtils.asMap("folder", folderName)
        );


    }

}
