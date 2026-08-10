package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.Direction;

/**
 * 考核范围值对象。
 * <p>
 * 封装考核的 {@code direction} 与 {@code epoch} 组合语义，显式区分全局最终考核、方向考核等概念。
 * </p>
 *
 * @param direction
 *            用户或考核所属技术方向
 * @param epoch
 *            考核批次或轮次编号
 */
public record AssessmentScope(Direction direction, Integer epoch) {

    /**
     * 是否为全局最终考核（direction=null 且 epoch=0）。
     *
     * @return 当前范围是否为全局最终考核
     */
    public boolean isGlobalFinal() {
        return this.direction == null
                && this.epoch != null
                && this.epoch == 0;
    }

    /**
     * 是否为方向考核（direction 非空）。
     * <p>
     * 只要具有方向即视为方向相关考核；轮次是否有效由 {@link #isValidDirectionalEpoch()} 单独判断。
     * </p>
     *
     * @return 当前范围是否为方向考核
     */
    public boolean isDirectional() {
        return this.direction != null;
    }

    /**
     * 判断当前范围是否对目标范围构成淘汰限制。
     * <p>
     * 规则：
     * <ul>
     * <li>目标为全局最终考核时，方向考核且轮次有效（epoch > 0）的淘汰决策限制它；</li>
     * <li>目标为方向考核时，仅同方向的方向考核淘汰决策限制它；</li>
     * <li>其他情况不构成限制。</li>
     * </ul>
     *
     * @param target
     *            目标考核范围
     * @return 当前范围是否限制目标范围
     */
    public boolean matches(AssessmentScope target) {
        if (target == null) {
            return false;
        }
        if (target.isGlobalFinal()) {
            return this.isDirectional() && this.isValidDirectionalEpoch();
        }
        if (target.isDirectional()) {
            return this.isDirectional() && this.direction.equals(target.direction);
        }
        return false;
    }

    /**
     * 是否为最终轮次（epoch 为 0）。
     * <p>
     * 最终轮次包括全局最终考核（direction=null, epoch=0），也包括某个方向的最终轮次（direction!=null, epoch=0）。
     * </p>
     *
     * @return 当前范围是否为最终轮次
     */
    public boolean isFinalRound() {
        return this.epoch != null && this.epoch == 0;
    }

    /**
     * 当前范围是否代表有效的方向考核轮次（epoch 为正整数）。
     *
     * @return 当前范围是否为有效的方向考核轮次
     */
    public boolean isValidDirectionalEpoch() {
        return this.epoch != null && this.epoch > 0;
    }
}
