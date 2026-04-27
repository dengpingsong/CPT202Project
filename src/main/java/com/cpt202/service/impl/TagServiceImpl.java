package com.cpt202.service.impl;

import com.cpt202.dto.TagDTO;
import com.cpt202.model.entity.Tag;
import com.cpt202.repository.TagRepository;
import com.cpt202.service.TagService;
import com.cpt202.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        return new TagVO(tag.getTagId(), tag.getTagName(), tag.getDescription());
    }

    /**
     * 新增标签。
     *
     * @param tagDTO 标签新增参数
     */
    @Override
    public void create(TagDTO tagDTO) {
        Tag tag = Tag.builder()
                .tagName(tagDTO.getTagName())
                .description(tagDTO.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        tagRepository.save(tag);
    }

    /**
     * 修改标签。
     *
     * @param tagId 标签主键
     * @param tagDTO 标签更新参数
     */
    @Override
    public void update(Long tagId, TagDTO tagDTO) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("要修改的标签不存在"));

        tag.setTagName(tagDTO.getTagName());
        tag.setDescription(tagDTO.getDescription());
        tag.setUpdatedAt(LocalDateTime.now());

        tagRepository.save(tag);
    }

    /**
     * 删除标签。
     *
     * @param tagId 标签主键
     */
    @Override
    public void delete(Long tagId) {
        if (!tagRepository.existsById(tagId)) {
            throw new RuntimeException("要删除的标签不存在");
        }
        tagRepository.deleteById(tagId);
    }
}
