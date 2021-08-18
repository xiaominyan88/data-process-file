package com.hyit.zhny.coud.service;

import com.hyit.zhny.coud.entity.request.InputDataRequestEntity;
import com.hyit.zhny.coud.entity.response.OutputDataResponseEntity;
import com.hyit.zhny.coud.factory.DataAccessFactory;
import com.hyit.zhny.coud.predictions.Predictions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class PowerDataAccessImple implements DataAccessFactory {

    @Override
    public void doDataMethod(List<InputDataRequestEntity> dataCollection, String timeType, ConcurrentMap<String,String> map, List<OutputDataResponseEntity> recieveCollection){

        Map<String,List<InputDataRequestEntity>> id_key_map = dataCollection.stream().collect(Collectors.groupingBy(InputDataRequestEntity::getId));

        for(Map.Entry entry : id_key_map.entrySet()){

            //在以每个Id为分组的Map中遍历循环，对Map的value继续以时间分组
            List<InputDataRequestEntity> station_key_list = (List<InputDataRequestEntity>)entry.getValue();

            Map<String,List<InputDataRequestEntity>> idAndTime_key_map = station_key_list.stream().collect(Collectors.groupingBy(InputDataRequestEntity::getTime));

            for(Map.Entry entry1 : idAndTime_key_map.entrySet()){

                OutputDataResponseEntity out_data = new OutputDataResponseEntity();

                List<InputDataRequestEntity> idAndTime_key_List = (List<InputDataRequestEntity>)entry1.getValue();

                String market_count_id = idAndTime_key_List.get(0).getId();

                if(Predictions.checkNotNull(map.get(market_count_id)))

                    out_data.setId(map.get(market_count_id));

                out_data.setTime((String)entry1.getKey());

                out_data.setTag(idAndTime_key_List.get(0).getTag());

                out_data.setDataType(timeType);

                if(idAndTime_key_List.size() > 1){

                    double sum = 0;

                    for(InputDataRequestEntity inner : idAndTime_key_List){

                        sum += inner.getValue();

                    }

                    out_data.setValue(sum);

                    recieveCollection.add(out_data);

                }else if(idAndTime_key_List.size() == 1){

                    out_data.setValue(idAndTime_key_List.get(0).getValue());

                    recieveCollection.add(out_data);

                }

            }

        }



    }

}
