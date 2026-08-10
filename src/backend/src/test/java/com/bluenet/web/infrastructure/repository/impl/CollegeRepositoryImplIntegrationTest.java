package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.infrastructure.repository.dataobject.CollegeDO;
import com.bluenet.web.infrastructure.repository.mapper.CollegeMapper;
import com.bluenet.web.testsupport.fixture.CollegeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CollegeRepositoryImpl 集成测试。
 */
@DisplayName("CollegeRepositoryImpl 集成测试")
class CollegeRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private CollegeMapper collegeMapper;

    @Test
    @DisplayName("save: 新学院应插入并回写ID")
    void save_newCollege_shouldInsertAndReturnId() {
        College college = CollegeFixture.createCollege("测试学院");

        collegeRepository.save(college);

        assertThat(college.getId()).isNotNull();
        CollegeDO dataObject = collegeMapper.selectById(college.getId());
        assertThat(dataObject).isNotNull();
        assertThat(dataObject.getName()).isEqualTo("测试学院");
    }

    @Test
    @DisplayName("save: 已有学院应更新名称")
    void save_existingCollege_shouldUpdateName() {
        College college = CollegeFixture.saveDefaultCollege(collegeRepository);
        college = College.reconstruct(college.getId(), "更新后的学院");

        collegeRepository.save(college);

        CollegeDO updated = collegeMapper.selectById(college.getId());
        assertThat(updated.getName()).isEqualTo("更新后的学院");
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        College college = CollegeFixture.saveDefaultCollege(collegeRepository);

        Optional<College> found = collegeRepository.findById(college.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(college.getName());

        Optional<College> notFound = collegeRepository.findById(-1L);
        assertThat(notFound).isEmpty();
    }

    @Test
    @DisplayName("findByName: 应按名称查询")
    void findByName_shouldReturnCollege() {
        College college = CollegeFixture.saveCollege(collegeRepository, "按名查询学院");

        Optional<College> found = collegeRepository.findByName("按名查询学院");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(college.getId());

        assertThat(collegeRepository.findByName("不存在的学院")).isEmpty();
    }

    @Test
    @DisplayName("findAll: 应返回所有学院")
    void findAll_shouldReturnAllColleges() {
        College college1 = CollegeFixture.saveCollege(collegeRepository, "学院A");
        College college2 = CollegeFixture.saveCollege(collegeRepository, "学院B");

        List<College> colleges = collegeRepository.findAll();

        assertThat(colleges)
                .extracting(College::getId)
                .contains(college1.getId(), college2.getId());
    }

    @Test
    @DisplayName("existsById: 应正确判断学院是否存在")
    void existsById_shouldWork() {
        College college = CollegeFixture.saveDefaultCollege(collegeRepository);

        assertThat(collegeRepository.existsById(college.getId())).isTrue();
        assertThat(collegeRepository.existsById(-1L)).isFalse();
    }

    @Test
    @DisplayName("existsByName: 应正确判断名称是否已存在")
    void existsByName_shouldWork() {
        CollegeFixture.saveCollege(collegeRepository, "唯一名称学院");

        assertThat(collegeRepository.existsByName("唯一名称学院")).isTrue();
        assertThat(collegeRepository.existsByName("不存在名称")).isFalse();
    }

    @Test
    @DisplayName("existsByNameAndIdNot: 排除自身后应正确判断名称冲突")
    void existsByNameAndIdNot_shouldExcludeSelf() {
        College college = CollegeFixture.saveCollege(collegeRepository, "排除自身学院");
        CollegeFixture.saveCollege(collegeRepository, "另一所学院");

        assertThat(collegeRepository.existsByNameAndIdNot("排除自身学院", college.getId())).isFalse();
        assertThat(collegeRepository.existsByNameAndIdNot("另一所学院", college.getId())).isTrue();
    }

    @Test
    @DisplayName("deleteById: 应删除学院")
    void deleteById_shouldRemoveCollege() {
        College college = CollegeFixture.saveDefaultCollege(collegeRepository);
        Long collegeId = college.getId();

        collegeRepository.deleteById(collegeId);

        assertThat(collegeMapper.selectById(collegeId)).isNull();
    }
}
