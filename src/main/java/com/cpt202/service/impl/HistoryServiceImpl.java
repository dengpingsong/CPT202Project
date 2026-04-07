package com.cpt202.service.impl;

import com.cpt202.service.HistoryService;
import com.cpt202.vo.RequestStatusHistoryVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 历史记录服务实现类。
 * 负责申请状态历史的读取与转换。
 */
@Service
public class HistoryServiceImpl implements HistoryService {

    /**
     * 查询申请状态历史。
     *
     * @param requestId 申请主键
     * @return 申请状态历史列表
     */
    @Override
    public List<RequestStatusHistoryVO> getRequestHistory(Long requestId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
