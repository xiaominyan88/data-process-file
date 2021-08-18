package com.hyit.zhny.coud.service;

import com.hyit.zhny.coud.entity.request.InputDataRequestEntity;
import com.hyit.zhny.coud.entity.response.OutputDataResponseEntity;
import com.hyit.zhny.coud.factory.DataAccessFactory;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class SteamDataAccessImple implements DataAccessFactory {

    @Override
    public void doDataMethod(List<InputDataRequestEntity> dataCollection, String timeType, ConcurrentMap<String,String> map, List<OutputDataResponseEntity> recieveCollection){

        for(InputDataRequestEntity in : dataCollection){

            OutputDataResponseEntity out = new OutputDataResponseEntity();

            out.setId(in.getId());

            out.setTime(in.getTime());

            out.setDataType(timeType);

            out.setTag(in.getTag());

            out.setValue(in.getValue());

            recieveCollection.add(out);
        }

    }
}
