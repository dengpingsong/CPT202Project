package com.cpt202.service.impl;

import com.cpt202.dto.TagDTO;
import com.cpt202.repository.TagRepository;
import com.cpt202.service.TagService;
import com.cpt202.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签管理服务实现类。
 * <p>
 * 当前仅保留方法定义，后续由仓储层完成真实持久化实现。
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    /**
     * 查询全部标签列表。
     *
     * @return 标签展示对象列表
     */
    @Override
    public List<TagVO> listAll() {
        return tagRepository.findAll()
                .stream()
                .map(tag -> new TagVO(tag.getTagId(),tag.getTagName(),tag.getDescription()))
                .collect(Collectors.toList());
    }

    /**
     * 根据标签主键查询标签详情。
     *
     * @param tagId 标签主键
     * @return 标签展示对象
     */
    @Override
    public TagVO getById(Long tagId) {
        return tagRepository.findById(tagId)
                .map(tag -> new TagVO(tag.getTagId(), tag.getTagName(), tag.getDescription()))
                .orElseThrow(() -> new com.cpt202.exception.NotFoundException("标签不存在。"));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void create(TagDTO tagDTO) {
        com.cpt202.model.entity.Tag tag = com.cpt202.model.entity.Tag.builder()
                .tagName(tagDTO.getTagName())
                .description(tagDTO.getDescription())
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
        tagRepository.save(tag);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void update(Long tagId, TagDTO tagDTO) {
        com.cpt202.model.entity.Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new com.cpt202.exception.NotFoundException("标签不存在。"));
        tag.setTagName(tagDTO.getTagName());
        tag.setDescription(tagDTO.getDescription());
        tag.setUpdatedAt(java.time.LocalDateTime.now());
        tagRepository.save(tag);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void delete(Long tagId) {
        if (!tagRepository.existsById(tagId)) {
            throw new com.cpt202.exception.NotFoundException("标签不存在。");
        }
        tagRepository.deleteById(tagId);
    }
}
