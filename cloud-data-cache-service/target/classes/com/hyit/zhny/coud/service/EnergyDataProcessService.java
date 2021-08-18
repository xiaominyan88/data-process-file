package com.hyit.zhny.coud.service;

import com.hyit.zhny.coud.entity.request.InputDataRequestEntity;
import com.hyit.zhny.coud.entity.request.InputParamRequestEntity;
import com.hyit.zhny.coud.entity.response.OutputDataResponseEntity;
import com.hyit.zhny.coud.factory.DataAccessFactory;
import com.hyit.zhny.coud.lock.ReadWriteLock;
import com.hyit.zhny.coud.lock.ReentrantReadWriteLock;
import com.hyit.zhny.coud.parser.XMLParserConfiguration;
import com.hyit.zhny.coud.predictions.Predictions;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

@Service
@Component
public class EnergyDataProcessService{

    private final ReadWriteLock closeLock = new ReentrantReadWriteLock();

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private enum LockMode{
        READ,WRITE
    }

    private void lockInterruptibly(LockMode mode) throws SQLException{

        if(Predictions.checkNotNull(mode)){

            switch(mode){

                case READ :

                    try{

                        closeLock.readLock().lockInterruptibly();

                    }catch(InterruptedException e){

                        Thread.currentThread().interrupt();

                        e.printStackTrace();
                    }

                    break;

                case WRITE :

                    try{

                        closeLock.writeLock().lockInterruptibly();

                    }catch (InterruptedException e){

                        Thread.currentThread().interrupt();

                        e.printStackTrace();
                    }
            }
        }else{

            throw new NullPointerException();

        }
    }

    private void unlock(LockMode mode){

        if(Predictions.checkNotNull(mode)){

            switch(mode){

                case READ :
                    closeLock.readLock().unlock();
                    break;
                case WRITE:
                    closeLock.writeLock().unlock();
            }

        }else{

            throw new NullPointerException();
        }
    }

    static{

        XMLParserConfiguration.init();

    }


    public List<OutputDataResponseEntity> energyDataProcessAndReturnService(InputParamRequestEntity in){

        ConcurrentMap<String,String> map = XMLParserConfiguration.map;

        String tagFlag = in.getTagFlag();

        String timeType = in.getTimeType();

        List<InputDataRequestEntity> dataList = in.getList();

        List<OutputDataResponseEntity> list = new ArrayList<>();

        DataAccessFactory dataAccessFactory = null;

        try{
            lockInterruptibly(LockMode.WRITE);

            if("TotalVaporFlow".equalsIgnoreCase(tagFlag)){

                dataAccessFactory = new SteamDataAccessImple();

                dataAccessFactory.doDataMethod(dataList, timeType, map, list);

            }else if("CurkWhRec".equalsIgnoreCase(tagFlag)){

                dataAccessFactory = new PowerDataAccessImple();

                dataAccessFactory.doDataMethod(dataList, timeType, map, list);

            }
        }catch(Exception e){

            e.printStackTrace();

        } finally {

            unlock(LockMode.WRITE);

        }
        return list;
    }

    public List<OutputDataResponseEntity> fileDataParsingAndProcessingService(MultipartFile file, String tag, String time){

        List<OutputDataResponseEntity> list = new ArrayList<>();

        try{

            InputStream inputStream = file.getInputStream();

            String[] fileNames = file.getOriginalFilename().split("\\.");

            int length = fileNames.length;

            String suffix = fileNames[length-1];

            if("xls".equals(suffix)){

                InputParamRequestEntity inputParam = doGetFormattedRequestParamFromHSS(inputStream,tag,time);

                if(inputParam.getList() != null){

                    list = energyDataProcessAndReturnService(inputParam);

                }

            }else if("xlsx".equals(suffix)){

                InputParamRequestEntity inputParam = doGetFormattedRequestParamFromXSS(inputStream,tag,time);

                if(inputParam.getList() != null){

                    list = energyDataProcessAndReturnService(inputParam);

                }

            }

        }catch (Exception e){

            e.printStackTrace();

        }

        return list;

    }


    private InputParamRequestEntity doGetFormattedRequestParamFromHSS(InputStream inputStream,String tag,String time) throws IOException {

        InputParamRequestEntity inputParam = new InputParamRequestEntity();

        POIFSFileSystem poifsFileSystem = new POIFSFileSystem(inputStream);

        HSSFWorkbook wb = new HSSFWorkbook(poifsFileSystem);

        HSSFSheet sheetAt = wb.getSheetAt(0);

        HSSFRow hssfRow = sheetAt.getRow(0);

        String id = hssfRow.getCell(0).getStringCellValue();

        String measureTag = hssfRow.getCell(1).getStringCellValue();

        String sampleTime = hssfRow.getCell(2).getStringCellValue();

        String value = hssfRow.getCell(3).getStringCellValue();

        if(id != null && measureTag != null && sampleTime != null && value != null){

            if("id".equalsIgnoreCase(id) && "measureTag".equalsIgnoreCase(measureTag)
                    && "sampleTime".equalsIgnoreCase(sampleTime) && "value".equalsIgnoreCase(value)){

                inputParam.setTagFlag(tag);

                inputParam.setTimeType(time);

                List<InputDataRequestEntity> list = new ArrayList<>();

                for(Row row : sheetAt){

                    if("id".equalsIgnoreCase(row.getCell(0).getStringCellValue())){

                        continue;

                    }

                    if(row.getCell(0) != null){

                        InputDataRequestEntity inData = new InputDataRequestEntity();

                        inData.setId(row.getCell(0).getStringCellValue());

                        inData.setTag(row.getCell(1).getStringCellValue());

                        inData.setTime(sdf.format(row.getCell(2).getDateCellValue()));

                        inData.setValue(row.getCell(3).getNumericCellValue());

                        list.add(inData);

                    }

                }

                inputParam.setList(list);

            }else{

                return inputParam;

            }

        }else{

            return inputParam;

        }

        return inputParam;

    }

    private InputParamRequestEntity doGetFormattedRequestParamFromXSS(InputStream inputStream,String tag,String time) throws IOException{

        InputParamRequestEntity inputParam = new InputParamRequestEntity();

        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheetAt(0);

        Row row = sheet.getRow(0);

        String id = row.getCell(0).getStringCellValue();

        String measureTag = row.getCell(1).getStringCellValue();

        String sampleTime = row.getCell(2).getStringCellValue();

        String value = row.getCell(3).getStringCellValue();

        if(id != null && measureTag != null && sampleTime != null && value != null){

            if("id".equalsIgnoreCase(id) && "measureTag".equalsIgnoreCase(measureTag)
                    && "sampleTime".equalsIgnoreCase(sampleTime) && "value".equalsIgnoreCase(value)){

                inputParam.setTagFlag(tag);

                inputParam.setTimeType(time);

                List<InputDataRequestEntity> list = new ArrayList<>();

                for(Row row1 : sheet){

                    if("id".equalsIgnoreCase(row1.getCell(0).getStringCellValue())){

                        continue;

                    }

                    if(row1.getCell(0) != null){

                        InputDataRequestEntity inData = new InputDataRequestEntity();

                        inData.setId(row1.getCell(0).getStringCellValue());

                        inData.setTag(row1.getCell(1).getStringCellValue());

                        inData.setTime(row1.getCell(2).getStringCellValue());

                        inData.setValue(row1.getCell(3).getNumericCellValue());

                        list.add(inData);

                    }

                }

                inputParam.setList(list);

            }else{

                return inputParam;

            }

        }else{

            return inputParam;

        }

        return inputParam;

    }
}
