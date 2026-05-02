package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.dto.TagDTO;
import com.cpt202.exception.NotFoundException;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.model.entity.Tag;
import com.cpt202.repository.TagRepository;
import com.cpt202.service.TagService;
import com.cpt202.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 标签管理服务实现类。
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
        List<Tag> tags = tagRepository.findAll();
        List<TagVO> tagVos = new ArrayList<>(tags.size());
        for (Tag tag : tags) {
            tagVos.add(toTagVO(tag));
        }
        return tagVos;
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
                .orElseThrow(() -> new NotFoundException(MessageConstants.TAG_NOT_FOUND));
        return toTagVO(tag);
    }

    /**
     * 新增标签。
     *
     * @param tagDTO 标签新增参数
     */
    @Override
    public void create(TagDTO tagDTO) {
        String tagName = tagDTO.getTagName().trim();
        if (tagRepository.existsByTagNameIgnoreCase(tagName)) {
            throw new RuleViolationException(MessageConstants.TAG_NAME_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();
        Tag tag = new Tag();
        BeanUtils.copyProperties(tagDTO, tag);
        tag.setTagName(tagName);
        tag.setCreatedAt(now);
        tag.setUpdatedAt(now);
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
                .orElseThrow(() -> new NotFoundException(MessageConstants.TAG_TO_UPDATE_NOT_FOUND));

        String tagName = tagDTO.getTagName().trim();
        if (tagRepository.existsByTagNameIgnoreCaseAndTagIdNot(tagName, tagId)) {
            throw new RuleViolationException(MessageConstants.TAG_NAME_EXISTS);
        }

        tag.setTagName(tagName);
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
            throw new NotFoundException(MessageConstants.TAG_TO_DELETE_NOT_FOUND);
        }
        tagRepository.deleteById(tagId);
    }

    private TagVO toTagVO(Tag tag) {
        TagVO tagVO = new TagVO();
        BeanUtils.copyProperties(tag, tagVO);
        return tagVO;
    }
}
