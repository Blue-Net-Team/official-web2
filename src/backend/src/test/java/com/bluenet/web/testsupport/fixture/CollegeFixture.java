package com.bluenet.web.testsupport.fixture;

import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.repository.CollegeRepository;

/**
 * 学院测试夹具。
 */
public final class CollegeFixture {

    private static final String DEFAULT_COLLEGE_NAME = "计算机学院";

    private CollegeFixture() {
    }

    /**
     * 创建默认学院（不持久化）。
     */
    public static College createDefaultCollege() {
        return College.create(DEFAULT_COLLEGE_NAME);
    }

    /**
     * 创建指定名称的学院（不持久化）。
     */
    public static College createCollege(String name) {
        return College.create(name);
    }

    /**
     * 保存默认学院并返回。
     */
    public static College saveDefaultCollege(CollegeRepository collegeRepository) {
        return saveCollege(collegeRepository, DEFAULT_COLLEGE_NAME);
    }

    /**
     * 保存指定名称学院并返回。
     */
    public static College saveCollege(CollegeRepository collegeRepository, String name) {
        College college = College.create(name);
        collegeRepository.save(college);
        return college;
    }
}
