package com.dianjing.service.impl;

import com.dianjing.common.BusinessException;
import com.dianjing.common.Constants;
import com.dianjing.dto.request.ArbitrationRequest;
import com.dianjing.dto.request.ComplaintCreateRequest;
import com.dianjing.entity.Complaint;
import com.dianjing.entity.Order;
import com.dianjing.mapper.ComplaintMapper;
import com.dianjing.mapper.OrderMapper;
import com.dianjing.service.ComplaintService;
import com.dianjing.service.MessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.List;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintMapper complaintMapper;
    private final OrderMapper orderMapper;
    private final MessageService messageService;

    public ComplaintServiceImpl(ComplaintMapper complaintMapper, OrderMapper orderMapper,
                               MessageService messageService) {
        this.complaintMapper = complaintMapper;
        this.orderMapper = orderMapper;
        this.messageService = messageService;
    }

    @Override
    @Transactional
    public Complaint create(Long userId, ComplaintCreateRequest request) {
        Order order = orderMapper.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        // 检查用户是否是订单相关方
        if (!order.getUserId().equals(userId) && !order.getProviderId().equals(userId)) {
            throw new BusinessException(403, "无权投诉此订单");
        }

        // 检查是否已有未处理的投诉
        List<Integer> resolvedStatuses = Arrays.asList(Constants.COMPLAINT_RESOLVED, Constants.COMPLAINT_REJECTED);
        if (complaintMapper.existsPendingComplaint(request.getOrderId(), resolvedStatuses)) {
            throw new BusinessException(400, "该订单已有未处理的投诉");
        }

        Complaint complaint = new Complaint();
        complaint.setOrderId(order.getId());
        complaint.setComplainantId(userId);
        complaint.setRespondentId(order.getUserId().equals(userId) ? order.getProviderId() : order.getUserId());
        complaint.setType(request.getType());
        complaint.setContent(request.getContent());
        complaint.setEvidenceImages(request.getEvidenceImages());
        complaint.setStatus(Constants.COMPLAINT_PENDING);

        return complaintMapper.save(complaint);
    }

    @Override
    public Complaint getById(Long id) {
        return complaintMapper.findById(id)
                .orElseThrow(() -> new BusinessException(404, "投诉不存在"));
    }

    @Override
    public Page<Complaint> getUserComplaints(Long userId, Pageable pageable) {
        // 获取用户作为投诉方或被投诉方的所有投诉
        Page<Complaint> complainantPage = complaintMapper.findByComplainantIdOrderByIdDesc(userId, pageable);
        Page<Complaint> respondentPage = complaintMapper.findByRespondentIdOrderByIdDesc(userId, pageable);
        
        // 为了简化，先返回作为投诉方的投诉
        return complainantPage;
    }

    @Override
    public Page<Complaint> getAllComplaints(Integer status, Pageable pageable) {
        if (status != null) {
            return complaintMapper.findByStatusOrderByIdDesc(status, pageable);
        }
        return complaintMapper.findAllByOrderByIdDesc(pageable);
    }

    @Override
    @Transactional
    public Complaint arbitrate(Long arbitratorId, ArbitrationRequest request) {
        Complaint complaint = complaintMapper.findById(request.getComplaintId())
                .orElseThrow(() -> new BusinessException(404, "投诉不存在"));
        
        if (complaint.getStatus() == Constants.COMPLAINT_RESOLVED || complaint.getStatus() == Constants.COMPLAINT_REJECTED) {
            throw new BusinessException(400, "该投诉已处理完毕");
        }

        complaint.setArbitratorId(arbitratorId);
        complaint.setArbitrationResult(request.getArbitrationResult());
        complaint.setStatus(Constants.COMPLAINT_RESOLVED);
        complaint.setArbitratedAt(java.time.LocalDateTime.now());

        Complaint saved = complaintMapper.save(complaint);
        
        // 给投诉方发送仲裁结果消息
        messageService.sendSystemMessage(
                complaint.getComplainantId(),
                "您的投诉（订单ID:" + complaint.getOrderId() + "）已收到仲裁结果：" + request.getArbitrationResult(),
                complaint.getId(),
                "complaint"
        );
        
        // 给被投诉方发送仲裁结果消息
        messageService.sendSystemMessage(
                complaint.getRespondentId(),
                "针对您的投诉（订单ID:" + complaint.getOrderId() + "）已收到仲裁结果：" + request.getArbitrationResult(),
                complaint.getId(),
                "complaint"
        );

        return saved;
    }
}
