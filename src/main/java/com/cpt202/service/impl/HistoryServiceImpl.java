package com.cpt202.service.impl;

import com.cpt202.exception.BusinessException;
import com.cpt202.exception.NotFoundException;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.RequestStatusHistory;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.repository.RequestStatusHistoryRepository;
import com.cpt202.service.HistoryService;
import com.cpt202.vo.RequestStatusHistoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 历史记录服务实现类。
 * 负责申请状态历史的读取与转换。
 */
@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final ProjectRequestRepository projectRequestRepository;
    private final RequestStatusHistoryRepository requestStatusHistoryRepository;

    /**
     * 查询申请状态历史。
     *
     * @param requestId 申请主键
     * @return 申请状态历史列表
     */
    @Override
    public List<RequestStatusHistoryVO> getRequestHistory(Long requestId, Long studentId) {
        ProjectRequest request = projectRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("申请记录不存在。"));
        if (request.getStudent() == null || !studentId.equals(request.getStudent().getStudentId())) {
            throw new BusinessException("不能查看其他学生的申请历史。");
        }

        List<RequestStatusHistory> histories = requestStatusHistoryRepository.findByRequest_RequestIdOrderByChangedAtAsc(requestId);
        List<RequestStatusHistoryVO> historyVos = new ArrayList<>(histories.size());
        for (RequestStatusHistory history : histories) {
            historyVos.add(RequestStatusHistoryVO.builder()
                    .historyId(history.getHistoryId())
                    .requestId(requestId)
                    .oldStatus(history.getOldStatus())
                    .newStatus(history.getNewStatus())
                    .changedByStudentId(history.getChangedBy() == null ? null : history.getChangedBy().getStudentId())
                    .changedByStudentName(history.getChangedBy() == null || history.getChangedBy().getUser() == null
                            ? null : history.getChangedBy().getUser().getFullName())
                    .remark(history.getRemark())
                    .changedAt(history.getChangedAt())
                    .build());
        }
        return historyVos;
    }

}
