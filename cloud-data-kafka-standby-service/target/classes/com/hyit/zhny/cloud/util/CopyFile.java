package com.hyit.zhny.cloud.util;

import org.springframework.core.io.ClassPathResource;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;

public class CopyFile {

    public static void copyFiles(@NotNull String inputFile,@NotNull String newFilePath) throws IOException{

        ClassPathResource resource = new ClassPathResource(inputFile);

        InputStream files = resource.getInputStream();

        File newFile = new File(newFilePath);

        if(!newFile.exists()){

            newFile.createNewFile();

        }else{

            if(!newFile.delete()){
                throw new FileAlreadyExistsException(inputFile);
            }

        }

        FileOutputStream fileOutputStream = new FileOutputStream(newFile);

        byte[] bytes = new byte[1024];

        int index;

        while((index = files.read(bytes)) != -1){

            fileOutputStream.write(bytes,0,index);
            fileOutputStream.flush();
        }
        files.close();
        fileOutputStream.close();
    }
}
