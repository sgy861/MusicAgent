package com.easymusic.entity.po;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 * 用户偏好画像
 */
public class UserPreferenceProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户偏好描述
     */
    private String preferenceText;

    /**
     * 用户偏好向量 (BGE 512维)
     */
    private byte[] preferenceVector;

    /**
     * 最后动作时间
     */
    private Date lastBehaviorTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 最后处理的动作ID
     */
    private Integer lastActionId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPreferenceText() {
        return preferenceText;
    }

    public void setPreferenceText(String preferenceText) {
        this.preferenceText = preferenceText;
    }

    public byte[] getPreferenceVector() {
        return preferenceVector;
    }

    public void setPreferenceVector(byte[] preferenceVector) {
        this.preferenceVector = preferenceVector;
    }

    public Date getLastBehaviorTime() {
        return lastBehaviorTime;
    }

    public void setLastBehaviorTime(Date lastBehaviorTime) {
        this.lastBehaviorTime = lastBehaviorTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getLastActionId() {
        return lastActionId;
    }

    public void setLastActionId(Integer lastActionId) {
        this.lastActionId = lastActionId;
    }

    // --- Helper conversions between float[] and byte[] ---

    public float[] getVectorAsFloatArray() {
        if (preferenceVector == null || preferenceVector.length == 0) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(preferenceVector);
        float[] floats = new float[preferenceVector.length / 4];
        for (int i = 0; i < floats.length; i++) {
            floats[i] = buffer.getFloat();
        }
        return floats;
    }

    public void setVectorFromFloatArray(float[] floats) {
        if (floats == null) {
            this.preferenceVector = null;
            return;
        }
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4);
        for (float f : floats) {
            buffer.putFloat(f);
        }
        this.preferenceVector = buffer.array();
    }
}
