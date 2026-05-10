package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.model.enumerate.QrcodeType;
import com.bluenet.web.domain.model.vo.FileVO;

import java.util.List;

/**
 * 二维码领域服务接口
 */
public interface QrcodeDomainService {

    /**
     * 保存二维码
     *
     * @param fileVO
     *            文件VO
     * @param type
     *            二维码类型
     */
    void saveQrcode(FileVO fileVO, QrcodeType type);

    /**
     * 获取咨询群二维码列表（按ID升序）
     *
     * @return 咨询群二维码列表
     */
    List<Qrcode> getConsultationQrcodes();

    /**
     * 更新咨询群二维码
     *
     * @param id
     *            二维码ID
     * @param fileVO
     *            新的文件VO
     */
    void updateConsultationQrcode(Long id, FileVO fileVO);

    /**
     * 根据ID删除咨询群二维码（含关联文件删除）
     *
     * @param id
     *            二维码ID
     */
    void deleteConsultationQrcode(Long id);

    /**
     * 获取考核群二维码列表（支持筛选）
     *
     * @param direction
     *            方向（可选）
     * @param epoch
     *            考核轮次（可选）
     * @return 考核群二维码列表
     */
    List<Qrcode> getAssessmentQrcodes(String direction, Integer epoch);

    /**
     * 保存考核群二维码
     *
     * @param qrcode
     *            二维码实体
     */
    void saveAssessmentQrcode(Qrcode qrcode);

    /**
     * 更新考核群二维码
     *
     * @param id
     *            二维码ID
     * @param fileVO
     *            新的文件VO（可选）
     * @param direction
     *            方向（可选）
     * @param epoch
     *            考核轮次（可选）
     * @param isShared
     *            是否共用（可选）
     */
    void updateAssessmentQrcode(Long id, FileVO fileVO, String direction,
            Integer epoch, Boolean isShared);

    /**
     * 根据ID删除考核群二维码（含关联文件删除）
     *
     * @param id
     *            二维码ID
     */
    void deleteAssessmentQrcode(Long id);
}
