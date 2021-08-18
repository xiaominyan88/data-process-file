package com.hyit.zhny.coud.factory;

import com.hyit.zhny.coud.entity.request.InputDataRequestEntity;
import com.hyit.zhny.coud.entity.response.OutputDataResponseEntity;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

public interface DataAccessFactory {

    /**
     * 用于接收数据并根据数据的tag做出相应的处理
     * @param dataCollection 从前端页面接收的原始数据
     * @param timeType 数据的时间类型，决定处理后的数据是写入phoenix的那张表，天表还是月表
     * @param map 站点和户号的换算映射关系，保存在xml文件中，项目启动时一次性加载
     * @param recieveCollection 处理后数据
     */
    public void doDataMethod(List<InputDataRequestEntity> dataCollection, String timeType, ConcurrentMap<String,String> map, List<OutputDataResponseEntity> recieveCollection);
}
